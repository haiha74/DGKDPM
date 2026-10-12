
let recommendProducts = [];

/* =========================================
   HERO SLIDER
========================================= */

function hero() {
  const slides = [
    {
      image: "https://images.unsplash.com/photo-1496747611176-843222e1e57c?q=80&w=1600&auto=format&fit=crop",
      title: "Summer Collection 2026",
      desc: "Đầm, blazer và áo sơ mi thanh lịch cho mùa mới."
    },
    {
      image: "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?q=80&w=1600&auto=format&fit=crop",
      title: "Luxury Fashion Week",
      desc: "Thiết kế sang trọng dành cho phong cách hiện đại."
    },
    {
      image: "https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=1600&auto=format&fit=crop",
      title: "Elegant New Arrival",
      desc: "Khám phá bộ sưu tập thời trang nữ cao cấp mới nhất."
    }
  ];

  return `
    <section class="relative h-[560px] overflow-hidden bg-black">

      <div id="heroSlides" class="relative w-full h-full">

        ${slides.map((slide, index) => `
          <div class="hero-slide absolute inset-0 transition-opacity duration-700 ${index === 0 ? 'opacity-100 z-10' : 'opacity-0 z-0'}">

            <img
              class="absolute inset-0 w-full h-full object-cover"
              src="${slide.image}"
              alt="${slide.title}"
            >

            <div class="absolute inset-0 bg-gradient-to-r from-black/80 via-black/30 to-transparent"></div>

            <div class="wrap relative h-full flex items-center text-white">
              <div class="max-w-2xl">

                <p class="uppercase tracking-[.35em] text-red-200 font-semibold">
                  JODOK
                </p>

                <h1 class="serif text-7xl leading-tight mt-4">
                  ${slide.title}
                </h1>

                <p class="text-xl mt-5 text-white/85">
                  ${slide.desc}
                </p>

                <button
                  onclick="go('shop')"
                  class="mt-8 rounded-full bg-white text-black px-8 py-4 font-bold hover:bg-red-800 hover:text-white transition"
                >
                  Khám phá ngay
                </button>

              </div>
            </div>

          </div>
        `).join("")}

      </div>

      <!-- BUTTON LEFT -->
      <button
        onclick="prevHeroSlide()"
        aria-label="Slide trước"
        class="absolute left-6 top-1/2 -translate-y-1/2 z-30
               w-14 h-14 rounded-full bg-white/20 backdrop-blur
               text-white text-3xl hover:bg-white hover:text-black transition"
      >
        ‹
      </button>

      <!-- BUTTON RIGHT -->
      <button
        onclick="nextHeroSlide()"
        aria-label="Slide tiếp theo"
        class="absolute right-6 top-1/2 -translate-y-1/2 z-30
               w-14 h-14 rounded-full bg-white/20 backdrop-blur
               text-white text-3xl hover:bg-white hover:text-black transition"
      >
        ›
      </button>

      <!-- DOTS -->
      <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-30 flex gap-3">

        ${slides.map((_, index) => `
          <button
            onclick="goHeroSlide(${index})"
            aria-label="Chuyển đến slide ${index + 1}"
            class="hero-dot w-3 h-3 rounded-full transition ${index === 0 ? 'bg-white scale-125' : 'bg-white/40'}"
          ></button>
        `).join("")}

      </div>

    </section>
  `;
}

/* =========================================
   HERO SLIDER FUNCTIONS
========================================= */

let currentHeroSlide = 0;
let heroInterval;

function updateHeroSlides() {
  const slides = document.querySelectorAll(".hero-slide");
  const dots = document.querySelectorAll(".hero-dot");

  if (!slides.length) return;

  slides.forEach((slide, index) => {
    if (index === currentHeroSlide) {
      slide.classList.remove("opacity-0", "z-0");
      slide.classList.add("opacity-100", "z-10");
    } else {
      slide.classList.remove("opacity-100", "z-10");
      slide.classList.add("opacity-0", "z-0");
    }
  });

  dots.forEach((dot, index) => {
    if (index === currentHeroSlide) {
      dot.classList.remove("bg-white/40");
      dot.classList.add("bg-white", "scale-125");
    } else {
      dot.classList.remove("bg-white", "scale-125");
      dot.classList.add("bg-white/40");
    }
  });
}

function nextHeroSlide() {
  const slides = document.querySelectorAll(".hero-slide");

  if (!slides.length) return;

  currentHeroSlide++;

  if (currentHeroSlide >= slides.length) {
    currentHeroSlide = 0;
  }

  updateHeroSlides();
}

function prevHeroSlide() {
  const slides = document.querySelectorAll(".hero-slide");

  if (!slides.length) return;

  currentHeroSlide--;

  if (currentHeroSlide < 0) {
    currentHeroSlide = slides.length - 1;
  }

  updateHeroSlides();
}

function goHeroSlide(index) {
  currentHeroSlide = index;
  updateHeroSlides();
}

function startHeroAutoSlide() {
  clearInterval(heroInterval);

  heroInterval = setInterval(() => {
    nextHeroSlide();
  }, 8000);
}

/* =========================================
   FEATURED CATEGORIES
========================================= */

function categoryGrid() {
  const parentCategories = categories.filter(
    c => !c.parent && !c.parentId
  );

  return `
    <section class="wrap py-12">

      <div class="w-full bg-white border rounded-3xl overflow-hidden shadow-sm">

        <h2 class="px-6 py-4 border-b text-lg font-bold uppercase text-neutral-700">
          Danh mục nổi bật
        </h2>

        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6">

          ${parentCategories.map((c, i) => `

            <button
              type="button"
              onclick="location.href='/products?categoryId=${c.categoryId}'"
              class="h-28 border-r border-b hover:bg-red-50
                     flex flex-col items-center justify-center
                     gap-2 transition"
            >

              <img
                class="w-14 h-14 rounded-full object-cover"
                src="${c.imageUrl ? c.imageUrl + (c.imageUrl.includes('?') ? '&' : '?') + 't=' + Date.now() : '/images/no-image.png'}"
                alt="${c.categoryName}"
                loading="lazy"
                onerror="this.onerror=null;this.src='/images/no-image.png';"
              >

              <span class="text-sm font-semibold text-center px-2">
                ${c.categoryName}
              </span>

            </button>

          `).join("")}

        </div>

      </div>

    </section>
  `;
}

/* =========================================
   PERSONALIZED RECOMMENDATIONS
========================================= */
function recommendSection() {
  if (!recommendProducts || recommendProducts.length === 0) {
    return "";
  }

  return `
    <section class="wrap py-10">

      <div class="mb-7">
        <p class="text-red-800 tracking-widest uppercase font-bold">
          Gợi ý cá nhân hóa
        </p>

        <h2 class="serif text-5xl">
          Dành riêng cho bạn
        </h2>

        <p class="mt-3 text-neutral-500">
          Dựa trên sản phẩm bạn đã xem trước đó.
        </p>
      </div>

      ${productGrid(recommendProducts.slice(0, 10))}

      <div class="flex justify-center mt-8">
        <a href="/products"
           class="inline-flex items-center justify-center
                  border border-neutral-200 rounded-full
                  px-10 py-3 bg-white font-semibold
                  hover:bg-red-800 hover:text-white
                  hover:border-red-800 transition">
          Xem thêm
        </a>
      </div>

    </section>
  `;
}

/* =========================================
   JODOK - SERVICE BENEFITS
========================================= */

function serviceBenefits() {
  const benefits = [
    {
      icon: "thumbs-up",
      title: "HÀNG HOÁ CHẤT LƯỢNG",
      description: "Tận hưởng các mặt hàng chất lượng hàng đầu với giá cả hợp lý"
    },
    {
      icon: "headset",
      title: "HỖ TRỢ 24/7",
      description: "Nhận hỗ trợ ngay lập tức bất cứ khi nào bạn cần"
    },
    {
      icon: "truck",
      title: "VẬN CHUYỂN NHANH CHÓNG",
      description: "Tùy chọn giao hàng nhanh chóng và đáng tin cậy"
    },
    {
      icon: "circle-dollar-sign",
      title: "THANH TOÁN AN TOÀN",
      description: "Nhiều phương thức thanh toán an toàn"
    }
  ];

  return `
      <section class="jodok-benefits wrap">
        
        <div class="text-left mb-5">

          <h2 class="serif text-2xl md:text-3xl font-bold text-neutral-900 mb-2">
            Mua sắm an tâm, trải nghiệm trọn vẹn
          </h2>

          <p class="text-neutral-500 text-sm md:text-base leading-relaxed">
            JODOK luôn đồng hành cùng bạn trong từng trải nghiệm mua sắm
          </p>

        </div>

        <div class="jodok-benefits-grid">

        ${benefits.map(item => `
          <div class="jodok-benefit-card">

            <div class="jodok-benefit-icon">
              ${icon(item.icon, "w-9 h-9")}
            </div>

            <h3 class="jodok-benefit-title">
              ${item.title}
            </h3>

            <p class="jodok-benefit-description">
              ${item.description}
            </p>

          </div>
        `).join("")}

      </div>

    </section>
  `;
}

/* =========================================
   HOME PAGE
========================================= */

function home() {
  return header()

    + hero()

    + categoryGrid()

    + recommendSection()

    + `

      <!-- BEST SELLERS -->
    <section class="wrap py-10">

      <div class="mb-7">
        <p class="text-red-800 tracking-widest uppercase font-bold">
          Sản phẩm nổi bật
        </p>

        <h2 class="serif text-5xl">
          Best Sellers
        </h2>
      </div>

      ${productGrid(
        [...products]
          .sort((a, b) =>
            getSoldCount(b.productId) - getSoldCount(a.productId)
          )
          .slice(0, 10)
      )}

      <div class="flex justify-center mt-8">
        <a href="/products"
          class="inline-flex items-center justify-center
                  border border-neutral-200 rounded-full
                  px-10 py-3 bg-white font-semibold
                  hover:bg-red-800 hover:text-white
                  hover:border-red-800 transition">
          Xem thêm
        </a>
      </div>

    </section>
    `

    /* Khối 4 cam kết nằm trước footer */
    + serviceBenefits()

    + footer();
}

/* =========================================
   LOAD HOME PAGE
========================================= */

async function loadHomePage() {
  try {
    const [
      productData,
      categoryData,
      brandData,
      orderData
    ] = await Promise.all([
      fetchJson(`${API_BASE}/products`),
      fetchJson(`${API_BASE}/categories`),
      fetchJson(`${API_BASE}/brands`).catch(() => []),
      fetchJson(`${API_BASE}/orders`).catch(() => [])
    ]);

    /* PRODUCTS */
    allProducts = productData.filter(
      p => p.status === "ACTIVE"
    );

    products = allProducts;

    /* CATEGORIES */
    categories = categoryData;

    /* BRANDS */
    brands = brandData;

    /* ORDERS */
    window.allOrders = orderData;

    /* SOLD COUNTS */
    window.soldCounts = {};

    await Promise.all(
      allProducts.map(async p => {
        try {
          const count = await fetchJson(
            `${API_BASE}/products/${p.productId}/sold-count`
          );

          window.soldCounts[p.productId] = Number(count || 0);
        } catch (e) {
          window.soldCounts[p.productId] = 0;
        }
      })
    );

    /* PERSONALIZED RECOMMENDATIONS */
    const user = getUser();

    recommendProducts = [];

    if (user?.userId) {
      try {
        recommendProducts = await fetchJson(
          `${API_BASE}/recommendations/${user.userId}`
        );
      } catch (e) {
        recommendProducts = [];
      }
    }

    /* RENDER HOME */
    renderApp(home());

    /* START SLIDER */
    setTimeout(() => {
      startHeroAutoSlide();
    }, 100);

  } catch (err) {
    console.error(err);

    renderApp(
      header()

      + `
        <main class="wrap flex-1 p-10 text-center">

          <h1 class="text-3xl font-bold text-red-800">
            Không kết nối được backend
          </h1>

          <p class="mt-3">
            Kiểm tra Spring Boot đang chạy ở cổng 8080
            và API /api/products hoạt động.
          </p>

        </main>
      `

      + footer()
    );
  }
}

/* =========================================
   INITIALIZE
========================================= */

loadHomePage();
