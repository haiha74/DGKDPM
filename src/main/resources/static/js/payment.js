let cart = null;
let appliedVoucher = null;
let discountAmount = 0;
let availableVouchers = [];

function userAuthHeaders(){
  const user = getUser();

  return user?.token
    ? { "Authorization": "Bearer " + user.token }
    : {};
}

function userJsonHeaders(){
  return {
    "Content-Type": "application/json",
    ...userAuthHeaders()
  };
}

function money(v){
  return Number(v || 0).toLocaleString("vi-VN") + "đ";
}

function productImg(p){
  if(p?.imageUrl) return p.imageUrl;
  if(p?.images && p.images.length > 0) return p.images[0].imageUrl;
  return "https://images.unsplash.com/photo-1539008835657-9e8e9680c956?q=80&w=900&auto=format&fit=crop";
}

async function loadCart(){
  const user = getUser();

  if(!user){
    showToast("Lỗi", "Vui lòng đăng nhập", "error");
    location.href = "/auth";
    return;
  }

  const res = await fetch(`${API_BASE}/cart/${user.userId}`, {
    headers: userAuthHeaders()
  });
  cart = await res.json();

  if(!res.ok){
    showToast("Lỗi", cart.message || "Không tải được giỏ hàng", "error");
    location.href = "/cart";
    return;
  }

  if(!cart.items || cart.items.length === 0){
    showToast("Thông báo", "Giỏ hàng trống", "error");
    location.href = "/cart";
    return;
  }

  await loadAvailableVouchers();
  renderPayment();
}

async function loadAvailableVouchers(){
  try{
    const res = await fetch(`${API_BASE}/vouchers`);
    const data = await res.json();

    const subtotal = (cart.items || []).reduce((sum,item)=>{
      const v = item.variant;
      const p = v.product;
      const price = Number(v.price || p.basePrice || 0);
      return sum + price * item.quantity;
    },0);

    availableVouchers = data.filter(v => {
      const active = v.status === "ACTIVE";
      const notExpired = !v.endDate || new Date(v.endDate + "T23:59:59") >= new Date();
      const enoughOrder = !v.minOrderValue || subtotal >= v.minOrderValue;

      return active && notExpired && enoughOrder;
    });

    availableVouchers.sort((a,b) => {
      const discountA =
        a.discountType === "PERCENT"
          ? subtotal * Number(a.discountValue) / 100
          : Number(a.discountValue);

      const discountB =
        b.discountType === "PERCENT"
          ? subtotal * Number(b.discountValue) / 100
          : Number(b.discountValue);

      return discountB - discountA;
    });

  }catch(e){
    availableVouchers = [];
  }
}

function renderPayment(){
  const user = getUser();
  const items = cart.items || [];
  let subtotal = 0;

  const itemHtml = items.map(item => {
    const v = item.variant;
    const p = v.product;
    const price = Number(v.price || p.basePrice || 0);
    const line = price * item.quantity;
    subtotal += line;

    return `
      <div class="flex gap-4 border-b pb-4">
        <img src="${productImg(p)}" class="w-20 h-24 object-cover rounded-xl">

        <div class="flex-1">
          <b>${p.productName}</b>
          <p class="text-sm text-neutral-500">
            Size: ${v.size} · Màu: ${v.color}
          </p>
          <p>Số lượng: ${item.quantity}</p>
        </div>

        <b class="text-red-800">${money(line)}</b>
      </div>
    `;
  }).join("");

    const shipping = subtotal >= 999000 ? 0 : 30000;
    const total = Math.max(subtotal + shipping - discountAmount, 0);

  const qrContent = `U${user.userId}_${total}`;
  
  document.getElementById("app").innerHTML = `
    ${header()}

      <main class="payment-page max-w-6xl mx-auto px-6 py-10 w-full">
  
      <h1 class="text-5xl font-serif mb-8">
        Thanh toán
      </h1>

      <div class="grid lg:grid-cols-[1fr_420px] gap-8">

        <div class="payment-panel bg-white rounded-3xl p-8 shadow">
          <h2 class="text-2xl font-bold mb-5">
            Thông tin nhận hàng
          </h2>

          <div class="space-y-4">

            <input
              id="fullname"
              class="w-full border rounded-2xl px-5 py-4"
              placeholder="Họ và tên"
              value="${user.fullname || ""}"
            >

            <input
              id="phone"
              class="w-full border rounded-2xl px-5 py-4"
              placeholder="Số điện thoại"
              value="${user.phone || ""}"
            >

            <textarea
              id="address"
              class="w-full border rounded-2xl px-5 py-4 h-32"
              placeholder="Địa chỉ nhận hàng"
            >${user.address || ""}</textarea>

            <div>
              <h3 class="font-bold text-xl pt-4">
                Mã giảm giá
              </h3>

              <div class="flex gap-3 mt-3">
                <input
                  id="voucherCode"
                  class="flex-1 border rounded-2xl px-5 py-4"
                  placeholder="Nhập mã voucher"
                  value="${appliedVoucher?.code || ""}"
                >

                <button
                  onclick="applyVoucher()"
                  class="bg-black text-white rounded-2xl px-5 font-bold">
                  Áp dụng
                </button>
              </div>

              <div class="payment-voucher-list mt-4 border rounded-2xl p-3 max-h-48 overflow-y-auto space-y-2">
                ${
                  availableVouchers.length
                  ? availableVouchers.map(v => `
                    <button
                      onclick="selectVoucher('${v.code}')"
                      class="w-full border rounded-xl px-4 py-3 text-left hover:bg-red-50
                      ${appliedVoucher?.code === v.code ? "border-red-800 bg-red-50" : ""}">
                      <b>${v.code}</b>
                      <p class="text-sm text-neutral-500">
                        Giảm ${
                          v.discountType === "PERCENT"
                            ? v.discountValue + "%"
                            : money(v.discountValue)
                        }
                      </p>
                    </button>
                  `).join("")
                  : `<p class="text-neutral-500">Không có voucher khả dụng</p>`
                }
              </div>

            <p id="voucherInfo" class="mt-2 text-sm text-green-700 font-semibold">
              ${appliedVoucher ? `Đã áp dụng mã ${appliedVoucher.code} - Giảm ${money(discountAmount)}` : ""}
            </p>
            </div>

            <h3 class="font-bold text-xl pt-4">
              Phương thức thanh toán
            </h3>

            <label class="block border rounded-2xl p-4 cursor-pointer">
              <input type="radio" name="paymentMethod" value="CASH" checked>
              Thanh toán khi nhận hàng
            </label>

            <label class="block border rounded-2xl p-4 cursor-pointer">
              <input type="radio" name="paymentMethod" value="QR">
              Thanh toán PayOS QR
            </label>

            <div id="qrBox"
                class="hidden border rounded-2xl p-5 bg-neutral-50 text-center">

              <p class="font-bold text-lg mb-3">
                Bấm "Xác nhận đặt hàng" để chuyển sang cổng thanh toán PayOS
              </p>

              <p class="text-neutral-500">
                Bạn sẽ được chuyển tới trang thanh toán bảo mật của PayOS.
              </p>

            </div>

          </div>
        </div>

        <div class="payment-summary bg-white rounded-3xl p-8 shadow h-fit">

          <h2 class="text-2xl font-bold mb-5">
            Đơn hàng
          </h2>

          <div class="space-y-4 mb-6">
            ${itemHtml}
          </div>

          <div class="border-t pt-5 space-y-3">

            <div class="flex justify-between">
              <span>Tạm tính</span>
              <b>${money(subtotal)}</b>
            </div>

            <div class="flex justify-between">
              <span>Phí vận chuyển</span>
              <b>${shipping === 0 ? "Miễn phí" : money(shipping)}</b>
            </div>

            <div class="flex justify-between">
              <span>Giảm giá</span>
              <b class="text-green-700">- ${money(discountAmount)}</b>
            </div>

            <div class="flex justify-between text-xl">
              <span class="font-bold">Tổng cộng</span>
              <b class="text-red-800">${money(total)}</b>
            </div>

          </div>

          <button
            onclick="submitPayment()"
            class="mt-7 w-full bg-red-800 text-white rounded-full py-4 font-bold"
          >
            Xác nhận đặt hàng
          </button>

          <a
            href="/cart"
            class="block text-center mt-4 border rounded-full py-3 font-bold"
          >
            Quay lại giỏ hàng
          </a>

        </div>
      </div>
        </main>

        ${footer()}
      `;

  if(window.lucide){
    lucide.createIcons();
  }

  document.addEventListener("change", e => {
    if(e.target.name === "paymentMethod"){
      document
        .getElementById("qrBox")
        .classList
        .toggle("hidden", e.target.value !== "QR");
    }
  });
}

async function applyVoucher(){
  const code = document.getElementById("voucherCode").value.trim();

  if(!code){
    showToast("Thiếu mã", "Vui lòng nhập mã voucher", "error");
    return;
  }

  const res = await fetch(`${API_BASE}/vouchers`);
  const vouchers = await res.json();

  const subtotal = (cart.items || []).reduce((sum,item)=>{
    const v = item.variant;
    const p = v.product;
    const price = Number(v.price || p.basePrice || 0);
    return sum + price * item.quantity;
  },0);

  const voucher = vouchers.find(v =>
    v.code.toUpperCase() === code.toUpperCase()
  );

  if(!voucher){
    showToast("Không hợp lệ", "Voucher không tồn tại", "error");
    return;
  }

  if(voucher.status !== "ACTIVE"){
    showToast("Không hợp lệ", "Voucher đã bị khóa", "error");
    return;
  }

  if(voucher.endDate && new Date(voucher.endDate + "T23:59:59") < new Date()){
    showToast("Không hợp lệ", "Voucher đã hết hạn", "error");
    return;
  }

  if(voucher.minOrderValue && subtotal < voucher.minOrderValue){
    showToast("Không đủ điều kiện", "Đơn hàng chưa đạt giá trị tối thiểu", "error");
    return;
  }

  appliedVoucher = voucher;

  if(voucher.discountType === "PERCENT"){
    discountAmount = subtotal * Number(voucher.discountValue) / 100;
  }else{
    discountAmount = Number(voucher.discountValue);
  }

  document.getElementById("voucherInfo").innerText =
    `Đã áp dụng mã ${voucher.code} - Giảm ${money(discountAmount)}`;

  showToast("Thành công", "Áp dụng voucher thành công", "success");

  renderPayment();
}

function selectVoucher(code){
  const input = document.getElementById("voucherCode");

  if(input){
    input.value = code;
  }

  applyVoucher();
}

async function submitPayment(){
  const user = getUser();

  const fullname = document.getElementById("fullname").value.trim();
  const phone = document.getElementById("phone").value.trim();
  const address = document.getElementById("address").value.trim();

  const paymentMethod =
    document.querySelector("input[name='paymentMethod']:checked").value;

  if(!fullname || !phone || !address){
    showToast("Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin", "error");
    return;
  }

  const res = await fetch(`${API_BASE}/orders/checkout`, {
    method: "POST",
    headers: userJsonHeaders(),
    body: JSON.stringify({
      userId: user.userId,
      fullname,
      phone,
      address,
      paymentMethod,
      voucherCode: appliedVoucher?.code || null
    })
  });

  const data = await res.json();

  if(!res.ok){
    showToast("Lỗi", data.message || "Đặt hàng thất bại", "error");
    return;
  }

  if(paymentMethod === "QR"){

  const total = Number(data.finalAmount || data.totalAmount || 0);
  const orderId = data.orderId || data.id;

  if(!orderId){
    showToast("Lỗi", "Không lấy được mã đơn hàng", "error");
    console.log("ORDER DATA:", data);
    return;
  }

  const payosRes = await fetch(`${API_BASE}/payments/payos/create`, {
    method:"POST",
    headers: userJsonHeaders(),
    body: JSON.stringify({
      orderId: orderId,
      amount: total,
      description: `DH${orderId}`
    })
  });

  const payosData = await payosRes.json();
  console.log("PAYOS DATA:", payosData);

  const checkoutUrl =
    payosData.checkoutUrl ||
    payosData.data?.checkoutUrl;

  if(checkoutUrl){
    window.location.href = checkoutUrl;
    return;
  }

  showToast(
    "Lỗi",
    payosData.error || payosData.message || "Không tạo được PayOS",
    "error"
  );

  return;
}else{
    showToast(
        "Đặt hàng thành công",
        "Thanh toán khi nhận hàng",
        "success"
        );
  }

  setTimeout(()=>{
  location.href = "/orders";
    },1500);
    }

loadCart();



