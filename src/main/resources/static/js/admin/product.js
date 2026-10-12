// Refactored from legacy /js/admin.js. Business logic preserved.

// Loaded as a compatibility classic script by admin/admin.js so existing inline handlers keep working.



function changeProductFilter(type, value) {

  if (type === "category") {

    productCategoryFilter = value;

  }



  if (type === "brand") {

    productBrandFilter = value;

  }



  if (type === "status") {

    productStatusFilter = value;

  }



  productPage = 1;

  render();

}



// Giữ nguyên focus/caret khi danh sách được render lại sau khi tìm kiếm.
function applyProductSearch(input) {
  if (!input || !input.isConnected) return;
  const value = input.value;
  const start = input.selectionStart;
  const end = input.selectionEnd;
  adminSearch.products = value.trim().toLowerCase();
  productPage = 1;
  render();
  const replacement = document.querySelector('[data-search="products"]');
  if (replacement) {
    replacement.focus({ preventScroll: true });
    try {
      replacement.setSelectionRange(start, end);
    } catch (e) {
      // Một số trình duyệt không hỗ trợ selection cho type=search.
    }
  }
}

function searchProductAdmin(input) {
  // Đừng phá chuỗi tiếng Việt đang được IME/Unikey xử lý.
  if (input.isComposing || window.productSearchComposing) return;
  clearTimeout(productSearchTimer);
  productSearchTimer = setTimeout(() => applyProductSearch(input), 350);
}

function finishProductSearch(input) {
  window.productSearchComposing = false;
  clearTimeout(productSearchTimer);
  productSearchTimer = setTimeout(() => applyProductSearch(input), 0);
}

// Đọc dữ liệu mới khi vào tab QLSP, không tải lại toàn bộ trang.
async function refreshProductFilterOptions() {
  const headers = adminAuthHeaders();
  const [categoriesResult, brandsResult] = await Promise.allSettled([
    fetch(`${API_BASE}/categories`, { headers, cache: 'no-store' })
      .then(async r => { if (!r.ok) throw new Error(`Danh mục HTTP ${r.status}`); return r.json(); }),
    fetch(`${API_BASE}/brands`, { headers, cache: 'no-store' })
      .then(async r => { if (!r.ok) throw new Error(`Thương hiệu HTTP ${r.status}`); return r.json(); })
  ]);
  if (currentTab !== 'products') return;
  if (categoriesResult.status === 'fulfilled' && Array.isArray(categoriesResult.value)) {
    categories = categoriesResult.value;
  } else {
    console.warn('Không thể cập nhật danh mục cho bộ lọc', categoriesResult.reason);
  }
  if (brandsResult.status === 'fulfilled' && Array.isArray(brandsResult.value)) {
    brands = brandsResult.value;
  } else {
    console.warn('Không thể cập nhật thương hiệu cho bộ lọc', brandsResult.reason);
  }
  // Tránh dựng lại toàn bộ trang trong trường hợp người dùng đã bắt đầu nhập.
  const toolbar = document.querySelector('.admin-product-toolbar');
  if (!toolbar) return;
  const existingCategory = toolbar.querySelector('[data-product-filter="category"]');
  const existingBrand = toolbar.querySelector('[data-product-filter="brand"]');
  if (existingCategory) {
    existingCategory.innerHTML = productCategoryOptions();
    existingCategory.value = productCategoryFilter || '';
  }
  if (existingBrand) {
    existingBrand.innerHTML = productBrandOptions();
    existingBrand.value = productBrandFilter || '';
  }
}

function productCategoryOptions() {
  return `<option value="">Danh mục</option>` + categories
    .map(c => `<option value="${escapeHtml(String(c.categoryId))}">${escapeHtml(c.categoryName || '')}${c.parent == null ? ' (Danh mục cha)' : ''}</option>`)
    .join('');
}

function productBrandOptions() {
  return `<option value="">Thương hiệu</option>` + brands
    .map(b => `<option value="${escapeHtml(String(b.brandId))}">${escapeHtml(b.brandName || '')}${String(b.status || 'ACTIVE').toUpperCase() !== 'ACTIVE' ? ' (Ngừng hoạt động)' : ''}</option>`)
    .join('');
}

function changeProductPage(page) {

  productPage = page;

  render();



  window.scrollTo({

    top: 0,

    behavior: "smooth"

  });

}



function productPagination(totalPages) {

  if (totalPages <= 1) return "";



  let pages = [];



  if (totalPages <= 7) {

    pages = Array.from(

      { length: totalPages },

      (_, i) => i + 1

    );

  } else {

    pages = [

      1,

      2,

      3,

      "...",

      totalPages

    ];



    if (productPage > 3 && productPage < totalPages - 1) {

      pages = [

        1,

        "...",

        productPage,

        "...",

        totalPages

      ];

    }

  }



  return `

    <div class="admin-product-pagination">



      <button

        class="product-page-arrow"

        onclick="changeProductPage(${Math.max(1, productPage - 1)})"

        ${productPage === 1 ? "disabled" : ""}>

        ${icon("chevron-left", "w-4 h-4")}

      </button>



      ${pages.map(page => {

        if (page === "...") {

          return `

            <span class="product-page-dots">...</span>

          `;

        }



        return `

          <button

            onclick="changeProductPage(${page})"

            class="product-page-number

              ${productPage === page ? "active" : ""}">

            ${page}

          </button>

        `;

      }).join("")}



      <button

        class="product-page-arrow"

        onclick="changeProductPage(${Math.min(totalPages, productPage + 1)})"

        ${productPage === totalPages ? "disabled" : ""}>

        ${icon("chevron-right", "w-4 h-4")}

      </button>



    </div>

  `;

}



function productTable() {

  const keyword = (adminSearch.products || "").trim().toLowerCase();



  let filtered = products.filter(p => {

    const name = (p.productName || "").toLowerCase();

    const categoryName = (p.category?.categoryName || "").toLowerCase();

    const brandName = (p.brand?.brandName || "").toLowerCase();



    const matchKeyword =

      name.includes(keyword) ||

      categoryName.includes(keyword) ||

      brandName.includes(keyword);



    const matchCategory =

      !productCategoryFilter ||

      String(p.category?.categoryId || "") === String(productCategoryFilter);



    const matchBrand =

      !productBrandFilter ||

      String(p.brand?.brandId || "") === String(productBrandFilter);



    const matchStatus =

      !productStatusFilter ||

      p.status === productStatusFilter;



    return (

      matchKeyword &&

      matchCategory &&

      matchBrand &&

      matchStatus

    );

  });



  const totalProducts = filtered.length;



  const totalPages = Math.max(

    1,

    Math.ceil(totalProducts / productPageSize)

  );



  if (productPage > totalPages) {

    productPage = totalPages;

  }



  if (productPage < 1) {

    productPage = 1;

  }



  const start = (productPage - 1) * productPageSize;



  const list = filtered.slice(

    start,

    start + productPageSize

  );



  return `

    <div class="admin-product-page">



      <!-- ================= TOOLBAR ================= -->

      <div class="admin-product-toolbar">



        <!-- SEARCH -->

        <div class="admin-product-search">

          ${icon("search", "w-4 h-4")}



          <input

            data-search="products"

            type="search"

            value="${escapeHtml(adminSearch.products || "")}"

            oninput="searchProductAdmin(this)"
            oncompositionstart="window.productSearchComposing = true"

            oncompositionend="finishProductSearch(this)"

            autocomplete="off"

            spellcheck="false"

            placeholder="Tìm kiếm sản phẩm..."

          >

        </div>



        <!-- CATEGORY -->

        <select

          class="admin-product-filter"
          data-product-filter="category"

          onchange="changeProductFilter('category', this.value)"

        >

          ${productCategoryOptions()}
        </select>



        <!-- BRAND -->

        <select

          class="admin-product-filter"
          data-product-filter="brand"

          onchange="changeProductFilter('brand', this.value)"

        >

          ${productBrandOptions()}
        </select>



        <!-- STATUS -->

        <select

          class="admin-product-filter"

          onchange="changeProductFilter('status', this.value)"

        >

          <option value="">Trạng thái</option>



          <option

            value="ACTIVE"

            ${productStatusFilter === "ACTIVE" ? "selected" : ""}

          >

            Đang bán

          </option>



          <option

            value="INACTIVE"

            ${productStatusFilter === "INACTIVE" ? "selected" : ""}

          >

            Ngừng bán

          </option>

        </select>



        <!-- ADD PRODUCT -->

        <button

          type="button"

          onclick="openProductForm()"

          class="admin-add-product"

        >

          ${icon("plus", "w-4 h-4")}

          <span>Thêm sản phẩm</span>

        </button>



      </div>

      <!-- =============== END TOOLBAR =============== -->





      <!-- ================= TABLE ================= -->

      <div class="admin-product-table-wrap">



        <table class="admin-product-table">



          <thead>

            <tr>

              <th>#</th>

              <th>Ảnh</th>

              <th>Tên sản phẩm</th>

              <th>Danh mục</th>

              <th>Thương hiệu</th>

              <th>Giá</th>

              <th>Trạng thái</th>

              <th>Hành động</th>

            </tr>

          </thead>



          <tbody>



            ${

              list.length

                ? list.map((p, index) => {

                    const number = start + index + 1;

                    const active = p.status === "ACTIVE";



                    return `

                      <tr>



                        <td class="product-index">

                          ${number}

                        </td>



                        <td>

                          <img

                            src="${productImg(p, index)}"

                            class="admin-product-image"

                            alt="${escapeHtml(p.productName || "")}"

                            onerror="this.onerror=null;this.src='/images/no-image.png'"

                          >

                        </td>



                        <td>

                          <div class="admin-product-name">



                            <strong>

                              ${escapeHtml(p.productName || "Chưa có tên")}

                            </strong>



                            ${

                              productHasVariant(p.productId)

                                ? ""

                                : `

                                  <span class="product-no-variant">

                                    Chưa có biến thể

                                  </span>

                                `

                            }



                          </div>

                        </td>



                        <td>

                          ${escapeHtml(

                            p.category?.categoryName || "Chưa có"

                          )}

                        </td>



                        <td>

                          ${escapeHtml(getActiveBrandName(p) || "-")}

                        </td>



                        <td class="admin-product-price">

                          ${money(p.basePrice)}

                        </td>



                        <td>

                          <span class="

                            admin-product-status

                            ${active ? "active" : "inactive"}

                          ">

                            ${active ? "Đang bán" : "Ngừng bán"}

                          </span>

                        </td>



                        <td>

                          <div class="admin-product-actions">



                            <!-- EDIT -->

                            <button

                              type="button"

                              onclick="openProductForm(${p.productId})"

                              title="Chỉnh sửa"

                              class="product-action edit"

                            >

                              ${icon("pencil", "w-4 h-4")}

                            </button>



                            <!-- SHOW / HIDE -->

                            ${

                              active

                                ? `

                                  <button

                                    type="button"

                                    onclick="hideProduct(${p.productId})"

                                    title="Ẩn sản phẩm"

                                    class="product-action view"

                                  >

                                    ${icon("eye", "w-4 h-4")}

                                  </button>

                                `

                                : `

                                  <button

                                    type="button"

                                    onclick="showProduct(${p.productId})"

                                    title="Hiện sản phẩm"

                                    class="product-action view"

                                  >

                                    ${icon("eye-off", "w-4 h-4")}

                                  </button>

                                `

                            }



                            <!-- DELETE -->

                            ${

                              !productHasOrders(p.productId)

                                ? `

                                  <button

                                    type="button"

                                    onclick="deleteProduct(${p.productId})"

                                    title="Xóa sản phẩm"

                                    class="product-action delete"

                                  >

                                    ${icon("trash-2", "w-4 h-4")}

                                  </button>

                                `

                                : ""

                            }



                          </div>

                        </td>



                      </tr>

                    `;

                  }).join("")

                : `

                  <tr>

                    <td

                      colspan="8"

                      class="admin-product-empty"

                    >

                      Không tìm thấy sản phẩm

                    </td>

                  </tr>

                `

            }



          </tbody>



        </table>



      </div>

      <!-- ================= END TABLE ================= -->





      <!-- ================= FOOTER ================= -->

      <div class="admin-product-footer">



        <p>

          Hiển thị

          <b>${list.length ? start + 1 : 0}</b>

          –

          <b>${list.length ? start + list.length : 0}</b>

          / ${totalProducts} sản phẩm

        </p>



        ${productPagination(totalPages)}



      </div>

      <!-- ================ END FOOTER ================ -->



    </div>



    ${productModal()}

  `;

}



function productModal(){

  return `

    <div id="productModal" class="admin-product-modal hidden">



      <div class="admin-product-modal-box">



        <!-- HEADER -->

        <div class="admin-product-form-header">



          <div>

            <h2 id="productFormTitle">

              Thêm sản phẩm

            </h2>



            <p>

              Nhập thông tin sản phẩm trong hệ thống JODOK

            </p>

          </div>



          <button

            type="button"

            class="admin-product-modal-close"

            onclick="closeProductForm()"

          >

            ${icon("x", "w-5 h-5")}

          </button>



        </div>





        <!-- ID SẢN PHẨM - BẮT BUỘC -->

        <input

          type="hidden"

          id="productId"

        >





        <!-- BODY -->

        <div class="admin-product-form-body">





          <!-- ================= LEFT ================= -->

          <div class="admin-product-form-left">





            <!-- TÊN -->

            <div class="admin-form-group">



              <label for="productName">

                Tên sản phẩm

                <span>*</span>

              </label>



              <input

                id="productName"

                type="text"

                placeholder="Nhập tên sản phẩm"

                autocomplete="off"

              >



            </div>





            <!-- BRAND + CATEGORY -->

            <div class="admin-form-row">



              <div class="admin-form-group">



                <label for="productBrand">

                  Thương hiệu

                </label>



                <select id="productBrand">



                  <option value="">

                    Không chọn thương hiệu

                  </option>



                  ${brands.map(b => `

                    <option value="${b.brandId}">

                      ${escapeHtml(b.brandName || "")}

                    </option>

                  `).join("")}



                </select>



              </div>





              <div class="admin-form-group">



                <label for="productCategory">

                  Danh mục

                  <span>*</span>

                </label>



                <select id="productCategory">



                  <option value="">

                    Chọn danh mục

                  </option>



                  ${categories.map(c => `

                    <option value="${c.categoryId}">

                      ${escapeHtml(c.categoryName || "")}

                    </option>

                  `).join("")}



                </select>



              </div>



            </div>





            <!-- DESCRIPTION -->

            <div class="admin-form-group">



              <label for="productDesc">

                Mô tả

              </label>



              <div class="admin-description-editor">



                <div class="admin-editor-toolbar">

                  <button type="button"><b>B</b></button>

                  <button type="button"><i>I</i></button>

                  <button type="button"><u>U</u></button>

                  <span></span>

                  <button type="button">≡</button>

                  <button type="button">•</button>

                </div>



                <textarea

                  id="productDesc"

                  placeholder="Nhập mô tả sản phẩm..."

                ></textarea>



              </div>



            </div>





            <!-- IMAGE -->

            <div class="admin-form-group">



              <label>

                Hình ảnh sản phẩm

              </label>





              <!-- CLICK UPLOAD -->

              <div

                class="admin-product-upload"

                onclick="document.getElementById('productImageFile').click()"

              >



                <div class="admin-upload-icon">

                  ${icon("image-up", "w-7 h-7")}

                </div>



                <strong>

                  Kéo thả hình ảnh vào đây hoặc

                  <em>chọn file</em>

                </strong>



                <small>

                  JPG, JPEG, PNG, WEBP

                </small>



              </div>





              <!-- INPUT THẬT -->

              <input

                id="productImageFile"

                type="file"

                accept="image/*"

                multiple

                hidden

                onchange="previewImage(event)"

              >





              <!-- PREVIEW -->

              <div

                id="productImagePreviewList"

                class="admin-product-preview-box"

                style="

                  display: flex;

                  flex-wrap: wrap;

                  gap: 12px;

                  margin-top: 12px;

                "

              ></div>



              <p class="admin-upload-note">

                Có thể chọn tối đa 3 ảnh sản phẩm. Nhập màu cho từng ảnh (ví dụ: Đen, Trắng); để trống nếu là ảnh mặc định.

              </p>



            </div>









              <!-- MULTI VARIANTS: add several size/color rows while creating product -->

              <div id="productVariantsEditor" class="admin-form-group" style="margin-top:20px">

                <label>Biến thể sản phẩm (thêm nhiều size / màu)</label>

                <p style="font-size:12px;color:#6b7280;margin:6px 0 10px">Mỗi dòng là một tổ hợp màu + size. Tồn kho là số sản phẩm của dòng đó.</p>

                <div id="productVariantRows" style="display:grid;gap:10px"></div>

                <button type="button" onclick="addProductVariantRow()" style="margin-top:10px;border:1px solid #991b1b;color:#991b1b;border-radius:9px;padding:9px 14px;font-weight:600;background:white">+ Thêm size / màu</button>

              </div>




                    </div>

          <!-- ================= RIGHT ================= -->

          <div class="admin-product-form-right">



            <h3>

              Thông tin cơ bản

            </h3>





            <!-- PRICE -->

            <div class="admin-form-group">



              <label for="productPrice">

                Giá gốc

              </label>



              <div class="admin-price-input">



                <input

                  id="productPrice"

                  type="number"

                  min="0"

                  placeholder="150000"

                >



                <span>đ</span>



              </div>



            </div>





            <!-- STATUS -->

            <div class="admin-form-group">



              <label for="productStatus">

                Trạng thái

              </label>



              <select id="productStatus">



                <option value="ACTIVE">

                  Đang bán

                </option>



                <option value="INACTIVE">

                  Ngừng bán

                </option>



              </select>



            </div>



          </div>



        </div>





        <!-- FOOTER -->

        <div class="admin-product-form-footer">



          <button

            type="button"

            class="admin-product-cancel-btn"

            onclick="closeProductForm()"

          >

            Hủy

          </button>





          <button

            type="button"

            class="admin-product-save-btn"

            onclick="saveProduct()"

          >

            ${icon("save", "w-4 h-4")}



            <span>

              Lưu sản phẩm

            </span>

          </button>



        </div>



      </div>



    </div>

  `;

}



// Các dòng biến thể được lưu cùng sản phẩm (không thay đổi trang quản lý biến thể riêng).

function addProductVariantRow(data = {}) {

  const list = document.getElementById("productVariantRows");

  if (!list) return;

  const row = document.createElement("div");

  row.className = "jodok-variant-row";

  row.style.cssText = "display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fafafa";

  const fields = [

    ["size", "Size * (S, M, XL...)", "text", data.size || ""],

    ["color", "Màu * (Đen, Trắng...)", "text", data.color || ""],

    ["sku", "SKU * (duy nhất)", "text", data.sku || ""],

    ["price", "Giá biến thể *", "number", data.price ?? ""],

    ["stock", "Tồn kho *", "number", data.stock ?? 0]

  ];

  fields.forEach(([key, placeholder, type, value]) => {

    const field = document.createElement("label");

    field.style.cssText = "font-size:12px;font-weight:600;color:#374151";

    field.textContent = placeholder;

    const input = document.createElement("input");

    input.dataset.variantField = key;

    input.type = type;

    input.placeholder = placeholder;

    input.value = value;

    input.required = true;

    if (type === "number") { input.min = "0"; input.step = key === "stock" ? "1" : "any"; }

    input.style.cssText = "display:block;width:100%;box-sizing:border-box;margin-top:5px;padding:9px;border:1px solid #d1d5db;border-radius:8px;background:white";

    field.appendChild(input);

    row.appendChild(field);

  });

  const remove = document.createElement("button");

  remove.type = "button";

  remove.textContent = "Xóa dòng";

  remove.style.cssText = "border:0;background:#fee2e2;color:#991b1b;border-radius:8px;padding:8px;cursor:pointer";

  remove.onclick = () => row.remove();

  row.appendChild(remove);

  list.appendChild(row);

}



function collectProductVariantRows() {

  const rows = Array.from(document.querySelectorAll("#productVariantRows .jodok-variant-row"));

  const variants = rows.map(row => {

    const get = key => row.querySelector(`[data-variant-field="${key}"]`).value.trim();

    return { size:get("size"), color:get("color"), sku:get("sku"), price:Number(get("price")), stock:Number(get("stock")),

      priceRaw:get("price"), stockRaw:get("stock"), status:"ACTIVE" };

  });

  const seenSku = new Set();

  const seenPair = new Set();

  for (const v of variants) {

    if (!v.size || !v.color || !v.sku || !v.priceRaw || !v.stockRaw || !Number.isFinite(v.price) || v.price < 0 || !Number.isInteger(v.stock) || v.stock < 0) {

      throw new Error("Điền đầy đủ size, màu, SKU, giá và tồn kho hợp lệ cho từng dòng.");

    }

    const sku = v.sku.toLocaleLowerCase("vi-VN");

    const pair = `${v.color.trim().toLocaleLowerCase("vi-VN")}::${v.size.trim().toLocaleLowerCase("vi-VN")}`;

    if (seenSku.has(sku)) throw new Error(`SKU bị trùng trong form: ${v.sku}`);

    if (seenPair.has(pair)) throw new Error(`Trùng biến thể ${v.color} / ${v.size}`);

    seenSku.add(sku); seenPair.add(pair);

    delete v.priceRaw; delete v.stockRaw;

  }

  return variants;

}



async function openProductForm(id = null) {
  selectedFiles = [];
  window.jodokSavedProductImages = [];
  const modal = document.getElementById("productModal");
  if (!modal) return;
  const get = key => document.getElementById(key);
  const isEdit = id !== null && id !== undefined && id !== "";
  const previewList = get("productImagePreviewList");
  const variantRows = get("productVariantRows");
  const variantEditor = get("productVariantsEditor");
  const fileInput = get("productImageFile");

  modal.classList.remove("hidden");
  modal.classList.add("flex");
  get("productFormTitle").textContent = isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm";
  get("productId").value = isEdit ? String(id) : "";
  if (fileInput) fileInput.value = "";
  if (previewList) previewList.innerHTML = "";
  if (variantRows) variantRows.innerHTML = "";

  const p = isEdit
    ? products.find(x => Number(x.productId) === Number(id))
    : null;
  if (isEdit && !p) {
    showToast("Lỗi", "Không tìm thấy sản phẩm", "error");
    closeProductForm();
    return;
  }

  get("productName").value = p?.productName || "";
  get("productDesc").value = p?.description || "";
  get("productPrice").value = p?.basePrice ?? "";
  get("productCategory").value = p?.category?.categoryId || "";
  get("productBrand").value = p?.brand?.brandId || "";
  get("productStatus").value = p?.status || "ACTIVE";

  const addButton = variantEditor?.querySelector('button[onclick="addProductVariantRow()"]');
  if (addButton) addButton.style.display = isEdit ? "none" : "";

  if (!isEdit) {
    addProductVariantRow();
    renderProductImagePreviews();
    return;
  }

  // Tải ảnh từ API để tránh dữ liệu products thiếu images.
  try {
    const response = await fetch(`${API_BASE}/product-images/product/${id}`, {
      headers: adminAuthHeaders()
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const images = await response.json();
    window.jodokSavedProductImages = (Array.isArray(images) ? images : [])
      .filter(image => image?.imageUrl)
      .sort((a, b) => Number(a.imageId || 0) - Number(b.imageId || 0));
  } catch (error) {
    console.warn("Không tải được ảnh từ API, dùng ảnh có sẵn:", error);
    window.jodokSavedProductImages = (Array.isArray(p.images) ? p.images : [])
      .filter(image => image?.imageUrl);
  }
  // Không render kết quả cũ nếu người dùng đã đóng / chuyển form.
  if (get("productId")?.value !== String(id)) return;
  renderProductImagePreviews();

  try {
    const response = await fetch(`${API_BASE}/variants/product/${id}`, {
      headers: adminAuthHeaders()
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const data = await response.json();
    if (get("productId")?.value !== String(id)) return;
    if (variantRows) {
      variantRows.innerHTML = "";
      const list = Array.isArray(data) ? data : [];
      if (list.length) {
        list.forEach(v => addProductVariantRow({
          size: v.size || "", color: v.color || "", sku: v.sku || "",
          price: v.price ?? "", stock: v.stock ?? 0
        }));
        variantRows.querySelectorAll("input, button").forEach(el => {
          el.disabled = true;
        });
      } else {
        variantRows.textContent = "Sản phẩm chưa có biến thể.";
      }
    }
  } catch (error) {
    console.error("Không tải được biến thể:", error);
    if (variantRows) variantRows.textContent = "Không tải được dữ liệu biến thể. Vui lòng thử lại.";
  }
}

function closeProductForm(){

  const modal =

    document.getElementById("productModal");



  if(!modal) return;



  modal.classList.add("hidden");

  modal.classList.remove("flex");



  const fileInput =

    document.getElementById("productImageFile");



  if(fileInput){

    fileInput.value = "";

  }



  selectedFiles = [];
  window.jodokSavedProductImages = [];

  const previewList = document.getElementById(

    "productImagePreviewList"

  );



  if (previewList) {

    previewList.innerHTML = "";

  }

}



function renderProductImagePreviews() {
  const container = document.getElementById("productImagePreviewList");
  if (!container) return;
  container.innerHTML = "";
  container.style.display = "flex";
  container.style.flexWrap = "wrap";
  container.style.gap = "12px";

  function createCard(src, color, saved, removeCallback) {
    const card = document.createElement("div");
    card.style.cssText = "width:155px;padding:8px;border:1px solid #e5e7eb;border-radius:12px;background:#fff;box-sizing:border-box;display:flex;flex-direction:column;gap:8px";
    const picture = document.createElement("div");
    picture.style.cssText = "height:120px;position:relative";
    const img = document.createElement("img");
    img.src = src;
    img.alt = saved ? "Ảnh đã lưu" : "Ảnh mới";
    img.style.cssText = "width:100%;height:100%;object-fit:cover;border-radius:8px";
    picture.appendChild(img);
    if (!saved) {
      const remove = document.createElement("button");
      remove.type = "button";
      remove.textContent = "×";
      remove.title = "Bỏ ảnh mới";
      remove.style.cssText = "position:absolute;top:4px;right:4px;width:26px;height:26px;border:0;border-radius:50%;background:#dc2626;color:white;font-size:20px;cursor:pointer";
      remove.onclick = removeCallback;
      picture.appendChild(remove);
    }
    const label = document.createElement("label");
    label.textContent = saved ? "Màu đã lưu" : "Màu ảnh";
    label.style.cssText = "font-size:12px;font-weight:600;color:#374151";
    const input = document.createElement("input");
    input.type = "text";
    input.value = color || "";
    input.placeholder = "VD: Đen, Trắng";
    input.readOnly = saved;
    input.style.cssText = `width:100%;box-sizing:border-box;padding:8px;border:1px solid #d1d5db;border-radius:8px;background:${saved ? "#f9fafb" : "#fff"};font-size:12px`;
    const status = document.createElement("small");
    status.textContent = saved ? "Ảnh đã lưu" : "Ảnh mới";
    status.style.cssText = "color:#6b7280;font-size:11px";
    card.append(picture, label, input, status);
    container.appendChild(card);
    return { img, input };
  }

  (window.jodokSavedProductImages || []).forEach(image => {
    createCard(image.imageUrl, image.color, true, null);
  });

  selectedFiles.forEach((file, index) => {
    const url = URL.createObjectURL(file);
    const { img, input } = createCard(url, file.jodokColor || "", false, () => {
      selectedFiles.splice(index, 1);
      renderProductImagePreviews();
    });
    input.oninput = () => { file.jodokColor = input.value; };
    img.onload = () => URL.revokeObjectURL(url);
    img.onerror = () => URL.revokeObjectURL(url);
  });
}

function previewImage(event) {

  const input = event.target;



  const files = Array.from(input.files || []);



  if (!files.length) return;



  const validTypes = [

    "image/jpeg",

    "image/png",

    "image/webp"

  ];



  const invalidFile = files.find(

    file => !validTypes.includes(file.type)

  );



  if (invalidFile) {

    showToast(

      "Lỗi",

      "Chỉ hỗ trợ JPG, JPEG, PNG, WEBP",

      "error"

    );



    input.value = "";

    return;

  }



  const remainingSlots = Math.max(0, 3 - (window.jodokSavedProductImages || []).length - selectedFiles.length);



  if (remainingSlots <= 0) {

    showToast(

      "Thông báo",

      "Chỉ được chọn tối đa 3 ảnh",

      "error"

    );



    input.value = "";

    return;

  }



  const newFiles = files.slice(0, remainingSlots);



  selectedFiles.push(...newFiles);



  renderProductImagePreviews();



  if (files.length > remainingSlots) {

    showToast(

      "Thông báo",

      "Chỉ lấy ảnh trong giới hạn tối đa 3 ảnh",

      "error"

    );

  } else {

    showToast(

      "Thành công",

      `Đã chọn ${selectedFiles.length}/3 ảnh`

    );

  }



  input.value = "";

}



function previewCategoryImage(event){

  const file = event.target.files[0];

  if(!file) return;



  if(!file.type.startsWith('image/')){

    showToast("Lỗi", "Vui lòng chọn file ảnh", "error");

    return;

  }



  selectedCategoryFile = file;



  const reader = new FileReader();

  reader.onload = function(e){

    const img = document.getElementById('previewCategoryImage');

    img.src = e.target.result;

    img.classList.remove('hidden');

  };



  reader.readAsDataURL(file);

}



async function saveProduct(){



  // =====================================================

  // 1. LẤY DỮ LIỆU FORM

  // =====================================================



  const id =

    document.getElementById("productId").value;





  const body = {



    productName:

      document

        .getElementById("productName")

        .value

        .trim(),



    description:

      document

        .getElementById("productDesc")

        .value

        .trim(),



    basePrice:

      Number(

        document.getElementById("productPrice").value

      ),



    categoryId:

      Number(

        document.getElementById("productCategory").value

      ),



    brandId:

      document.getElementById("productBrand").value

        ? Number(

            document.getElementById("productBrand").value

          )

        : null,



    status:

      document.getElementById("productStatus").value

  };





  // =====================================================

  // 2. VALIDATE FE

  // =====================================================



  if(!body.productName){



    showToast(

      "Lỗi",

      "Vui lòng nhập tên sản phẩm",

      "error"

    );



    return;

  }





  if(!body.categoryId){



    showToast(

      "Lỗi",

      "Vui lòng chọn danh mục",

      "error"

    );



    return;

  }





  if(

    Number.isNaN(body.basePrice) ||

    body.basePrice < 0

  ){



    showToast(

      "Lỗi",

      "Giá sản phẩm không hợp lệ",

      "error"

    );



    return;

  }





  // Kiểm tra các dòng biến thể trước khi tạo sản phẩm.

  let pendingVariants = [];

  if (!id) {

    try {

      pendingVariants = collectProductVariantRows();

      if (!pendingVariants.length) throw new Error("Hãy thêm ít nhất một biến thể cho sản phẩm.");

    } catch (error) {

      showToast("Lỗi biến thể", error.message, "error");

      return;

    }

  }



  // =====================================================

  // 3. XÁC ĐỊNH POST / PUT

  // =====================================================



  const url = id

    ? `${API_BASE}/products/${id}`

    : `${API_BASE}/products`;





  const method = id

    ? "PUT"

    : "POST";





  // =====================================================

  // DEBUG

  // =====================================================



  console.log(

    "========== SAVE PRODUCT =========="

  );



  console.log(

    "URL:",

    url

  );



  console.log(

    "METHOD:",

    method

  );



  console.log(

    "BODY:",

    body

  );



  console.log(

    "AUTH:",

    adminAuthHeaders()

  );





  // =====================================================

  // 4. LƯU SẢN PHẨM

  // =====================================================



  let res;





  try{



    res = await fetch(

      url,

      {

        method: method,



        headers:

          adminJsonHeaders(),



        body:

          JSON.stringify(body)

      }

    );



  }catch(error){



    console.error(

      "SAVE PRODUCT FETCH ERROR:",

      error

    );





    showToast(

      "Lỗi",

      "Không thể kết nối tới server",

      "error"

    );



    return;

  }





  // =====================================================

  // 5. BACKEND TRẢ LỖI

  // =====================================================



  if(!res.ok){



    const errorText =

      await res.text();





    console.error(

      "SAVE PRODUCT FAILED:",

      {

        status:

          res.status,



        statusText:

          res.statusText,



        url:

          url,



        method:

          method,



        body:

          body,



        response:

          errorText,



        auth:

          adminAuthHeaders()

      }

    );





    let message =

      errorText;





    // Backend có thể trả JSON

    if(errorText){



      try{



        const data =

          JSON.parse(errorText);





        message =

          data.message ||

          data.error ||

          data.detail ||

          errorText;



      }catch(e){



        // Backend trả plain text

      }



    }





    // Nếu backend không trả message

    if(!message){



      if(res.status === 400){



        message =

          "Dữ liệu sản phẩm không hợp lệ (HTTP 400)";



      }else if(res.status === 401){



        message =

          "Phiên đăng nhập không hợp lệ (HTTP 401)";



      }else if(res.status === 403){



        message =

          "Backend từ chối quyền lưu sản phẩm (HTTP 403)";



      }else if(res.status === 404){



        message =

          "Không tìm thấy API lưu sản phẩm (HTTP 404)";



      }else if(res.status === 409){



        message =

          "Dữ liệu sản phẩm bị trùng hoặc xung đột (HTTP 409)";



      }else if(res.status >= 500){



        message =

          `Backend gặp lỗi khi lưu sản phẩm (HTTP ${res.status})`;



      }else{



        message =

          `Lưu sản phẩm thất bại (HTTP ${res.status})`;

      }

    }





    showToast(

      "Lỗi",

      message,

      "error"

    );





    return;

  }





  // =====================================================

  // 6. ĐỌC PRODUCT BACKEND TRẢ VỀ

  // =====================================================



  let product;





  try{



    product =

      await res.json();



  }catch(error){



    console.error(

      "SAVE PRODUCT JSON ERROR:",

      error

    );





    showToast(

      "Lỗi",

      "Backend đã lưu nhưng không trả về dữ liệu sản phẩm hợp lệ",

      "error"

    );



    return;

  }





  console.log(

    "PRODUCT SAVED:",

    product

  );





  // =====================================================

  // 7. KIỂM TRA PRODUCT ID

  // =====================================================



  if(!product?.productId){



    console.error(

      "PRODUCT RESPONSE KHÔNG CÓ productId:",

      product

    );





    showToast(

      "Lỗi",

      "Không xác định được ID sản phẩm vừa lưu",

      "error"

    );



    return;

  }





  // =====================================================

  // 8. UPLOAD ẢNH

  // =====================================================



  if(

    Array.isArray(selectedFiles) &&

    selectedFiles.length > 0

  ){



    console.log(

      "SỐ ẢNH CẦN UPLOAD:",

      selectedFiles.length

    );





    for(

      let i = 0;

      i < selectedFiles.length;

      i++

    ){



      const file =

        selectedFiles[i];





      console.log(

        `UPLOAD IMAGE ${i + 1}:`,

        file

      );





      const formData =

        new FormData();





      formData.append(

        "file",

        file

      );





      // =================================================

      // 8.1 UPLOAD FILE

      // =================================================



      let uploadRes;





      try{



        uploadRes =

          await fetch(

            `${API_BASE}/upload/image`,

            {

              method:

                "POST",



              // KHÔNG set Content-Type

              // browser tự tạo multipart boundary

              headers:

                adminAuthHeaders(),



              body:

                formData

            }

          );



      }catch(error){



        console.error(

          "UPLOAD IMAGE FETCH ERROR:",

          error

        );





        showToast(

          "Lỗi",

          "Sản phẩm đã lưu nhưng không thể kết nối server để upload ảnh",

          "error"

        );



        return;

      }





      // =================================================

      // 8.2 ĐỌC RESPONSE UPLOAD

      // =================================================



      const uploadText =

        await uploadRes.text();





      console.log(

        "UPLOAD STATUS:",

        uploadRes.status

      );





      console.log(

        "UPLOAD RESPONSE:",

        uploadText

      );





      if(!uploadRes.ok){



        let message =

          uploadText;





        if(uploadText){



          try{



            const data =

              JSON.parse(uploadText);





            message =

              data.message ||

              data.error ||

              data.detail ||

              uploadText;



          }catch(e){



          }



        }





        showToast(

          "Lỗi",

          message ||

          `Upload ảnh thất bại (HTTP ${uploadRes.status})`,

          "error"

        );





        return;

      }





      // =================================================

      // 8.3 PARSE RESPONSE UPLOAD

      // =================================================



      let uploadData;





      try{



        uploadData =

          uploadText

            ? JSON.parse(uploadText)

            : {};



      }catch(error){



        console.error(

          "UPLOAD RESPONSE KHÔNG PHẢI JSON:",

          uploadText

        );





        showToast(

          "Lỗi",

          "Server upload ảnh trả dữ liệu không hợp lệ",

          "error"

        );



        return;

      }





      // =================================================

      // 8.4 KIỂM TRA IMAGE URL

      // =================================================



      if(!uploadData?.imageUrl){



        console.error(

          "UPLOAD KHÔNG CÓ imageUrl:",

          uploadData

        );





        showToast(

          "Lỗi",

          "Upload ảnh thành công nhưng backend không trả imageUrl",

          "error"

        );



        return;

      }





      // =================================================

      // 8.5 LƯU ẢNH VÀO PRODUCT

      // =================================================



      const imageBody = {



        productId:

          product.productId,



        imageUrl:

          uploadData.imageUrl,



        color: (selectedFiles[i].jodokColor || "").trim() || null,



        isMain:

          i === 0 && (window.jodokSavedProductImages || []).length === 0

      };





      console.log(

        "SAVE PRODUCT IMAGE:",

        imageBody

      );





      let imageRes;





      try{



        imageRes =

          await fetch(

            `${API_BASE}/product-images`,

            {

              method:

                "POST",



              headers:

                adminJsonHeaders(),



              body:

                JSON.stringify(imageBody)

            }

          );



      }catch(error){



        console.error(

          "PRODUCT IMAGE FETCH ERROR:",

          error

        );





        showToast(

          "Lỗi",

          "Ảnh đã upload nhưng không thể liên kết ảnh với sản phẩm",

          "error"

        );



        return;

      }





      // =================================================

      // 8.6 KIỂM TRA PRODUCT IMAGE

      // =================================================



      if(!imageRes.ok){



        const imageError =

          await imageRes.text();





        console.error(

          "SAVE PRODUCT IMAGE FAILED:",

          {

            status:

              imageRes.status,



            response:

              imageError,



            body:

              imageBody

          }

        );





        let message =

          imageError;





        if(imageError){



          try{



            const data =

              JSON.parse(imageError);





            message =

              data.message ||

              data.error ||

              data.detail ||

              imageError;



          }catch(e){



          }



        }





        showToast(

          "Lỗi",

          message ||

          `Không thể lưu ảnh sản phẩm (HTTP ${imageRes.status})`,

          "error"

        );





        return;

      }





      console.log(

        `IMAGE ${i + 1} SAVED`

      );

    }

  }





  // =====================================================

  // 9. RESET FILE

  // =====================================================



  selectedFiles = [];





  // =====================================================

  // 10. ĐÓNG FORM

  // =====================================================



  closeProductForm();





  // =====================================================

  // 11. LOAD LẠI DATA

  // =====================================================



  try{



    await init();



  }catch(error){



    console.error(

      "RELOAD ADMIN DATA ERROR:",

      error

    );





    showToast(

      "Lỗi",

      "Sản phẩm đã lưu nhưng không thể tải lại dữ liệu",

      "error"

    );



    return;

  }





  // =====================================================

  // 12. TẠO TẤT CẢ BIẾN THỂ ĐÃ NHẬP TRONG FORM.

  // Backend hiện có POST /variants, không cần đổi API.

  if (!id && pendingVariants.length) {

    const failed = [];

    for (const variant of pendingVariants) {

      try {

        const variantRes = await fetch(`${API_BASE}/variants`, {

          method: "POST",

          headers: adminJsonHeaders(),

          body: JSON.stringify({ productId: product.productId, ...variant })

        });

        if (!variantRes.ok) {

          failed.push(`${variant.color}/${variant.size} (${variant.sku}): ${await variantRes.text()}`);

        }

      } catch (error) {

        failed.push(`${variant.color}/${variant.size} (${variant.sku}): ${error.message}`);

      }

    }

    await init();

    currentTab = "products";

    render();

    if (failed.length) {

      showToast("Lưu chưa hoàn tất", `Sản phẩm đã tạo, nhưng ${failed.length} biến thể lỗi. Xem Console để xử lý tại Quản lý biến thể.`, "error");

      console.error("BIẾN THỂ LƯU THẤT BẠI:", failed);

      return;

    }

    showToast("Thành công", `Đã lưu sản phẩm và ${pendingVariants.length} biến thể`);

    return;

  }



  showToast("Thành công", "Đã lưu sản phẩm");

}



function deleteProduct(id){

  showConfirm("Xóa vĩnh viễn sản phẩm này?", async () => {

    const res = await fetch(`${API_BASE}/products/${id}`, {

  method:'DELETE',

  headers: adminAuthHeaders()

});



    if(!res.ok){

      showToast("Lỗi", await res.text(), "error");

      return;

    }



    showToast("Thành công", "Đã xóa sản phẩm");

    await init();

  }, "Xóa");

}



function productHasOrders(productId){

  return orders.some(order =>

    (order.items || []).some(item =>

      item.variant?.product?.productId === productId

    )

  );

}



async function hideProduct(id){

  const p = products.find(x => x.productId === id);



  if(!p) return;



  showConfirm("Ẩn sản phẩm này?", async () => {



    const res = await fetch(`${API_BASE}/products/${id}`,{

      method:"PUT",

      headers: adminJsonHeaders(),

      body:JSON.stringify({

        productName:p.productName,

        description:p.description || "",

        basePrice:p.basePrice,

        categoryId:p.category?.categoryId,

        brandId:p.brand?.brandId || null,

        status:"INACTIVE"

      })

    });



    if(!res.ok){

      showToast("Lỗi","Không thể ẩn sản phẩm","error");

      return;

    }



    showToast("Thành công","Đã ẩn sản phẩm khỏi shop");

    await init();



  }, "Ẩn");

}



async function showProduct(id){

  const p = products.find(x => x.productId === id);



  if(!p) return;



  showConfirm("Hiện lại sản phẩm này trên shop?", async () => {



    const body = {

      productName: p.productName,

      description: p.description || "",

      basePrice: Number(p.basePrice),

      categoryId: p.category?.categoryId || null,

      brandId: p.brand?.brandId || null,

      status: "ACTIVE"

    };



    if(!body.productName || !body.categoryId){

      showToast("Lỗi", "Sản phẩm thiếu tên hoặc danh mục", "error");

      return;

    }



    const res = await fetch(`${API_BASE}/products/${id}`,{

      method:"PUT",

      headers: adminJsonHeaders(),

      body: JSON.stringify(body)

    });



    if(!res.ok){

      const text = await res.text();

      console.error("Lỗi hiện sản phẩm:", text);

      showToast("Lỗi", text || "Không thể hiện sản phẩm", "error");

      return;

    }



    showToast("Thành công","Đã hiện sản phẩm trên shop");

    await init();



    currentTab = "products";

    render();



  }, "Hiện");

}
