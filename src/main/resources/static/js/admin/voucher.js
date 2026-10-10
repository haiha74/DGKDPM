// Refactored from legacy /js/admin.js. Business logic preserved.
// Loaded as a compatibility classic script by admin/admin.js so existing inline handlers keep working.

function voucherPanel(){
  const list = vouchers.filter(v =>
    (v.code || "").toLowerCase().includes(adminSearch.promo)
  );
  return `<div class="soft-card overflow-hidden">
    ${adminToolbar("promo", "Quản lý voucher", "Thêm", "openVoucherForm()")}

    <div class="overflow-x-auto">
      <table class="w-full text-left">
        <thead class="bg-neutral-50 text-sm text-neutral-500">
          <tr>
            <th class="p-4">Mã</th>
            <th>Loại</th>
            <th>Giá trị</th>
            <th>Đơn tối thiểu</th>
            <th>Hết hạn</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>
          ${
            list.length
              ? list.map(v => `
              <tr class="border-t">
                <td class="p-4 font-bold">${v.code}</td>
                <td>${v.discountType}</td>
                <td class="text-red-800 font-bold">
                  ${v.discountType === "PERCENT" ? v.discountValue + "%" : money(v.discountValue)}
                </td>
                <td>${money(v.minOrderValue)}</td>
                <td>${v.endDate || "-"}</td>
                <td>
                  <span class="rounded-full px-3 py-1 text-sm ${
                    isVoucherExpired(v)
                      ? "bg-red-50 text-red-700"
                      : v.status === "ACTIVE"
                        ? "bg-green-50 text-green-700"
                        : "bg-neutral-100 text-neutral-500"
                  }">
                    ${voucherStatusText(v)}
                  </span>
                </td>
                <td class="space-x-2">
                  <button onclick="openVoucherForm(${v.voucherId})" class="border rounded-full px-2 py-2 text-sm">Sửa</button>
                  <button onclick="deleteVoucher(${v.voucherId})" class="bg-red-800 text-white rounded-full px-2 py-2 text-sm">Xóa</button>
                </td>
              </tr>
            `).join("")
            : `<tr><td colspan="7" class="p-6 text-neutral-500 text-center">Chưa có voucher</td></tr>`
          }
        </tbody>
      </table>
    </div>
  </div>${voucherModal()}`;
}

function voucherModal(){
  return `<div id="voucherModal" class="fixed inset-0 bg-black/40 z-[999] hidden items-center justify-center p-5">
    <div class="bg-white rounded-3xl p-7 w-full max-w-lg shadow-xl">
      <div class="flex justify-between items-center mb-5">
        <h2 id="voucherFormTitle" class="serif text-3xl">Thêm voucher</h2>
        <button onclick="closeVoucherForm()" class="text-2xl">×</button>
      </div>

      <input type="hidden" id="voucherId">

      <div class="space-y-4">
        <input id="voucherCode" class="input-ui" placeholder="Mã voucher, VD: SALE10">

        <select id="voucherType" class="input-ui">
          <option value="PERCENT">Giảm theo %</option>
          <option value="FIXED">Giảm tiền cố định</option>
        </select>

        <input id="voucherValue" type="number" class="input-ui" placeholder="Giá trị giảm">

        <input id="voucherMinOrder" type="number" class="input-ui" placeholder="Đơn tối thiểu">

        <input id="voucherEndDate" type="date" class="input-ui">

        <select id="voucherStatus" class="input-ui">
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>

        <button onclick="saveVoucher()" class="btn-primary w-full">Lưu voucher</button>
      </div>
    </div>
  </div>`;
}

function openVoucherForm(id=null){
  document.getElementById("voucherModal").classList.remove("hidden");
  document.getElementById("voucherModal").classList.add("flex");

  document.getElementById("voucherFormTitle").innerText = id ? "Sửa voucher" : "Thêm voucher";
  document.getElementById("voucherId").value = id || "";

  const v = vouchers.find(x => x.voucherId === id);

  document.getElementById("voucherCode").value = v?.code || "";
  document.getElementById("voucherType").value = v?.discountType || "PERCENT";
  document.getElementById("voucherValue").value = v?.discountValue || "";
  document.getElementById("voucherMinOrder").value = v?.minOrderValue || 0;
  document.getElementById("voucherEndDate").value = v?.endDate || "";
  document.getElementById("voucherStatus").value = v?.status || "ACTIVE";
}

function closeVoucherForm(){
  document.getElementById("voucherModal").classList.add("hidden");
  document.getElementById("voucherModal").classList.remove("flex");
}

async function saveVoucher(){
  const id = document.getElementById("voucherId").value;

  const body = {
    code: document.getElementById("voucherCode").value.trim(),
    discountType: document.getElementById("voucherType").value,
    discountValue: Number(document.getElementById("voucherValue").value),
    minOrderValue: Number(document.getElementById("voucherMinOrder").value),
    endDate: document.getElementById("voucherEndDate").value || null,
    status: document.getElementById("voucherStatus").value
  };

  if(!body.code || !body.discountValue){
    showToast("Lỗi", "Vui lòng nhập mã và giá trị voucher", "error");
    return;
  }

  const url = id ? `${API_BASE}/vouchers/${id}` : `${API_BASE}/vouchers`;
  const method = id ? "PUT" : "POST";

  const res = await fetch(url,{
    method,
    headers: adminJsonHeaders(),
    body: JSON.stringify(body)
  });

  if(!res.ok){
    showToast("Lỗi", await res.text(), "error");
    return;
  }

  closeVoucherForm();
  showToast("Thành công", "Đã lưu voucher");
  await init();

  currentTab = "promo";
  render();
}

function deleteVoucher(id){
  showConfirm("Xóa voucher này?", async () => {
    const res = await fetch(
      `${API_BASE}/vouchers/${id}`,
      {
        method:"DELETE",
        headers: adminAuthHeaders()
      }
    );

    if(!res.ok){
      showToast("Lỗi", "Xóa voucher thất bại", "error");
      return;
    }

    showToast("Thành công", "Đã xóa voucher");
    await init();
    currentTab = "promo";
    render();
  });
}
