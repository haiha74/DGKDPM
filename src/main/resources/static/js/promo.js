let vouchers = [];

function promo(){
  return header()+`
    <main class="wrap py-14">

      <section class="rounded-3xl bg-gradient-to-r from-red-900 to-red-600 text-white p-12">
        <p class="uppercase tracking-widest">Ưu đãi tháng này</p>
        <h1 class="serif text-6xl mt-3">Khuyến mãi hot</h1>
        <p class="text-xl mt-4">Săn sale cho các sản phẩm thời trang nữ mới nhất.</p>
        <button onclick="go('shop')" class="mt-7 bg-white text-red-800 rounded-full px-8 py-4 font-bold">
          Mua ngay
        </button>
      </section>

      ${voucherSection()}

      <h2 class="serif text-4xl mt-12 mb-6">Sản phẩm đang bán</h2>
      ${productGrid(products.slice(0,8))}

    </main>
  `+footer();
}

async function loadPromoPage(){
  try{

    const productData = await fetchJson(`${API_BASE}/products`);

    allProducts = productData.filter(
      p => p.status === "ACTIVE"
    );

    products = allProducts;

    try{
      vouchers = await fetchJson(`${API_BASE}/vouchers`);
    }catch(e){
      vouchers = [];
    }

    window.soldCounts = {};

    await Promise.all(
      products.map(async p => {
        try{
          const sold = await fetchJson(
            `${API_BASE}/products/${p.productId}/sold-count`
          );

          window.soldCounts[p.productId] = Number(sold || 0);

        }catch(e){

          window.soldCounts[p.productId] = 0;

        }
      })
    );

    renderApp(promo());

  }catch(err){

    console.error(err);

    renderApp(
      header()+
      `<main class="wrap py-20">Không tải được dữ liệu.</main>`+
      footer()
    );

  }
}

function voucherSection(){
  const today = new Date();
  today.setHours(0,0,0,0);

  const activeVouchers = vouchers.filter(v => {

    const isActive = v.status === "ACTIVE";

    if(!isActive) return false;

    if(!v.endDate) return true;

    const end = new Date(v.endDate);
    end.setHours(0,0,0,0);

    return end >= today;
  });

  if(activeVouchers.length === 0){
    return "";
  }

  return `
    <section class="mt-12">
      <div class="flex items-end justify-between mb-6">
        <div>
          <p class="text-red-800 tracking-widest uppercase font-bold">Voucher</p>
          <h2 class="serif text-4xl mt-2">Mã giảm giá đang có</h2>
        </div>
      </div>

      <div class="grid md:grid-cols-3 gap-5">
        ${activeVouchers.map(v => `
          <div class="soft-card p-6 border-l-8 border-red-800">
            <p class="text-neutral-500 text-sm">Mã voucher</p>

            <h3 class="text-3xl font-bold text-red-800 mt-2">
              ${v.code}
            </h3>

            <p class="mt-4 font-semibold">
              Giảm ${
                v.discountType === "PERCENT"
                ? v.discountValue + "%"
                : formatPrice(v.discountValue)
              }
            </p>

            <p class="text-sm text-neutral-500 mt-2">
              Đơn tối thiểu: ${formatPrice(v.minOrderValue)}
            </p>

            <p class="text-sm text-neutral-500 mt-1">
              Hết hạn: ${v.endDate || "Không giới hạn"}
            </p>

            <button onclick="copyVoucher('${v.code}')"
              class="mt-5 bg-black text-white rounded-full px-5 py-3 font-bold">
              Sao chép mã
            </button>
          </div>
        `).join("")}
      </div>
    </section>
  `;
}

function copyVoucher(code){
  navigator.clipboard.writeText(code);
  showToast("Đã sao chép", `Mã ${code} đã được sao chép`, "success");
}

loadPromoPage();