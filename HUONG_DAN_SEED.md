# Project website thoi trang - clone ve co san data DEMO

## Clone / giai nen va chay
1. Cai Java (kiem tra phien ban trong `pom.xml`), MySQL, Maven; khoi dong MySQL.
2. Chay file `database/create_demo_database.sql` trong MySQL Workbench, hoac lenh:
   `mysql -u root -p < database/create_demo_database.sql`
3. Mo `src/main/resources/application.properties`. Mac dinh ket noi `localhost:3306/fashion_clone_demo`, username `root`, password rong. Sua DB_USERNAME va DB_PASSWORD theo tai khoan MySQL cua **ban**. Co the dat bien moi truong thay vi dua password len Git:
   - Windows PowerShell: `$env:DB_USERNAME="root"; $env:DB_PASSWORD="mat-khau-cua-ban"`
   - Linux/macOS: `export DB_USERNAME=root; export DB_PASSWORD='mat-khau-cua-ban'`
   - Neu muon doi ten DB: dat `DB_NAME=ten_database_moi` va tu tao DB ten do.
4. Khoi dong backend bang `mvn spring-boot:run` (hoac `mvnw.cmd spring-boot:run` tren Windows).
5. Khi chay lan dau: Hibernate tao cac bang, sau do `DemoDataSeeder` tao san data demo; khong can import database cu. Lan chay sau giu nguyen data.

## Du lieu demo tu dong
- `src/main/java/com/dacs/fashion/config/DemoDataSeeder.java` chua 10 ban ghi / nhom cho 15 nhom: brands, categories, products, product_images, product_variants, users, vouchers, orders, order_items, payments, shipments, reviews, carts, cart_items, product_views.
- San pham, ten, gia, ton kho, voucher, don hang, user la du lieu GIA LAP; URL anh san pham Unsplash can internet de hien thi. Khong su dung du lieu that tu database goc.
- Tai khoan test: `admin.demo@example.com`, `staff.demo@example.com`, `customer0.demo@example.com`...`customer7.demo@example.com` (8 khach); mat khau demo chung: `Demo@12345`. Chi dung de test, khong su dung cho production.
- Seeder **chi chay neu database chua co brand/category/product/user/order**. Neu da co data se khong ghi de, khong xoa. Tat seeder bang `APP_SEED_ENABLED=false`.
- Neu muon seed lai: dung mot database demo MOI rong, tao qua SQL script. KHONG xoa database co data quan trong.

## Tinh nang va cau hinh rieng
- Chatbot da duoc loai bo trong ban project nay.
- PayOS: **khong** dung key goc. Muon su dung thanh toan PayOS, tu cung cap `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY` cua rieng ban, sau do bat `PAYOS_ENABLED=true`. Mac dinh PayOS tat nen khong can key de khoi dong demo. Co the de trong khi demo chuc nang khong lien quan thanh toan; neu khoi dong gap loi PayOS, cau hinh key test cua ban.
- Thay `JWT_SECRET` va mat khau demo truoc khi dua website len moi truong that.
