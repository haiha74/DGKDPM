package com.dacs.fashion.config;

import com.dacs.fashion.entity.*;
import com.dacs.fashion.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data demo lưu trực tiếp trong code. Chỉ chạy khi CSDL chưa có dữ liệu nghiệp vụ. */
@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoDataSeeder {
    private static BigDecimal money(long value) { return BigDecimal.valueOf(value); }

    @Bean
    CommandLineRunner seedDemoData(
            BrandRepository brands, CategoryRepository categories, ProductRepository products,
            ProductImageRepository images, ProductVariantRepository variants,
            UserRepository users, VoucherRepository vouchers, OrderRepository orders,
            OrderItemRepository orderItems, PaymentRepository payments,
            ShipmentRepository shipments, ReviewRepository reviews,
            CartRepository carts, CartItemRepository cartItems,
            ProductViewRepository views, PasswordEncoder encoder) {
        return args -> {
            // Tài khoản admin demo: bổ sung được cả cho DB đã seed bằng phiên bản trước.
            // Không thay đổi mật khẩu hay quyền của tài khoản nếu email đã tồn tại.
            if (!users.existsByEmailIgnoreCase("admin@gmail.com")) {
                users.save(User.builder().fullname("Quản trị viên")
                    .email("admin@gmail.com").phone("0900999999")
                    .password(encoder.encode("123456"))
                    .address("Hà Nội, Việt Nam").role("ADMIN").status("ACTIVE").build());
                System.out.println("[DEMO SEED] Đã tạo tài khoản admin@gmail.com (ADMIN).");
            }
            // Sửa ảnh chân váy cho cả các database đã seed bằng phiên bản cũ.
            repairDemoSkirtImage(variants, images, categories);
            // Admin vừa được seed KHÔNG được ngăn seeder tạo dữ liệu demo.
            // Nếu DB đã có sản phẩm, chỉ bổ sung lượt bán còn thiếu cho sản phẩm demo;
            // tuyệt đối không sinh sản phẩm/đơn hàng mẫu vào cửa hàng đã có dữ liệu riêng.
            if (brands.count() > 0 || categories.count() > 0 || products.count() > 0
                    || orders.count() > 0) {
                repairDemoSoldCounts(variants, products, orders, orderItems, users);
                return;
            }

            String[] catNames = {"Áo thun", "Áo sơ mi", "Áo khoác", "Quần jeans", "Quần dài",
                    "Chân váy", "Đầm nữ", "Hoodie", "Giày thể thao", "Túi xách"};
            String[] brandNames = {"Urban Basic", "Daily Wear", "Street Mode", "Denim Lab", "Minimal Studio",
                    "Elegant Day", "Luna Fashion", "Cozy Club", "Move Active", "Everyday Bags"};
            // Ảnh thật từ Unsplash, mỗi URL tương ứng một nhóm hàng cụ thể.
            String[] imageUrls = {
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1598032895397-b9472444bf93?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1544923246-77307dd654cb?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=900&auto=format&fit=crop&q=85",
                "https://sakurafashion.vn/upload/sanpham/large/61627-chan-vay-xep-ly-nu-dang-dai-3.jpg",
                "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=900&auto=format&fit=crop&q=85",
                "https://images.unsplash.com/photo-1547949003-9792a18a2601?w=900&auto=format&fit=crop&q=85"
            };
            String[] names = {"Áo thun cotton trắng basic", "Áo sơ mi nam Oxford xanh", "Áo khoác denim xanh cổ điển",
                    "Quần jeans xanh ống đứng", "Quần tây dáng suông thanh lịch", "Chân váy xếp ly nữ",
                    "Đầm nữ midi dự tiệc", "Áo hoodie unisex form rộng", "Giày sneaker thể thao đỏ", "Túi xách da thời trang"};
            List<Brand> bs = new ArrayList<>();
            List<Category> cs = new ArrayList<>();
            List<Product> ps = new ArrayList<>();
            List<ProductVariant> vs = new ArrayList<>();
            List<User> us = new ArrayList<>();
            for (int i=0; i<10; i++) {
                bs.add(brands.save(Brand.builder().brandName(brandNames[i]).description("Thương hiệu thời trang demo " + (i+1)).status("ACTIVE").build()));
                cs.add(categories.save(Category.builder().categoryName(catNames[i]).description("Danh mục " + catNames[i]).status("ACTIVE").imageUrl(imageUrls[i]).build()));
            }
            for (int i=0; i<10; i++) {
                Product product = products.save(Product.builder().category(cs.get(i)).brand(bs.get(i))
                    .productName(names[i]).description("Sản phẩm thời trang mẫu: " + names[i] + ". Chất liệu đẹp, phù hợp sử dụng hằng ngày.")
                    .basePrice(money(149000 + i*65000L)).status("ACTIVE").build());
                ps.add(product);
                images.save(ProductImage.builder().product(product).imageUrl(imageUrls[i]).color("Màu mẫu").isMain(true).build());
                vs.add(variants.save(ProductVariant.builder().product(product).size(i==8?"40":"M")
                    .color(i%2==0?"Trắng":"Xanh").sku("DEMO-2026-"+String.format("%03d",i+1))
                    .price(product.getBasePrice()).stock(220+i*10).status("ACTIVE").build()));
            }
            for (int i=0; i<10; i++) {
                String role = i==0?"ADMIN":i==1?"STAFF":"USER";
                us.add(users.save(User.builder().fullname(i==0?"Quản trị demo":i==1?"Nhân viên demo":"Khách hàng demo "+(i-1))
                   .email(i==0?"admin.demo@example.com":i==1?"staff.demo@example.com":"customer"+(i-1)+".demo@example.com")
                   .phone("090000"+String.format("%04d",i)).password(encoder.encode("Demo@12345"))
                   .address("Hà Nội, Việt Nam").role(role).status("ACTIVE").build()));
                vouchers.save(Voucher.builder().code("DEMO"+String.format("%02d",i+1))
                    .discountType("FIXED").discountValue(money(10000L*(i+1)))
                    .minOrderValue(money(200000)).endDate(LocalDate.now().plusMonths(6))
                    .status("ACTIVE").build());
            }
            for (int i=0; i<10; i++) {
                User customer = us.get(i);
                ProductVariant variant = vs.get(i);
                // Số lượng bán demo 12, 16, 20...48 cho từng sản phẩm.
                // API sold-count hiện tính từ các order_items thuộc đơn COMPLETED.
                int soldQuantity = 12 + i * 4;
                BigDecimal total=variant.getPrice().multiply(BigDecimal.valueOf(soldQuantity));
                Order order = orders.save(Order.builder().user(customer).address(customer.getAddress())
                    .totalAmount(total).discountAmount(BigDecimal.ZERO).finalAmount(total)
                    .orderStatus("COMPLETED").createdAt(LocalDateTime.now().minusDays(20-i)).build());
                OrderItem item=orderItems.save(OrderItem.builder().order(order).variant(variant).quantity(soldQuantity)
                    .price(variant.getPrice()).unitPrice(variant.getPrice()).subtotal(total).build());
                payments.save(Payment.builder().order(order).paymentMethod("COD").paymentStatus("PAID")
                    .paidAt(LocalDateTime.now().minusDays(18-i)).build());
                shipments.save(Shipment.builder().order(order).staff(us.get(1)).shippingStatus("DELIVERED")
                    .trackingCode("DEMO-SHIP-"+String.format("%04d",i+1))
                    .shippedAt(LocalDateTime.now().minusDays(19-i))
                    .deliveredAt(LocalDateTime.now().minusDays(18-i)).build());
                reviews.save(Review.builder().user(customer).orderItem(item).rating(4+i%2)
                    .comment("Sản phẩm đẹp, giao hàng nhanh, đúng mô tả (đánh giá mẫu " + (i+1) + ").")
                    .createdAt(LocalDateTime.now().minusDays(17-i)).build());
                Cart cart=carts.save(Cart.builder().user(customer).build());
                cartItems.save(CartItem.builder().cart(cart).variant(vs.get((i+1)%10)).quantity(1+i%3).build());
                ProductView pv=new ProductView(); pv.setUser(customer);pv.setProduct(ps.get(i));
                pv.setViewedAt(LocalDateTime.now().minusHours(i+1));views.save(pv);
            }
            System.out.println("[DEMO SEED] Số lượng đã bán (10 sản phẩm): 12, 16, 20, 24, 28, 32, 36, 40, 44, 48.");
            System.out.println("[DEMO SEED] Đã tạo 10 bản ghi cho mỗi nhóm: brands, categories, products, images, variants, users, vouchers, orders, order_items, payments, shipments, reviews, carts, cart_items, product_views.");
        };
    }
    /** Chỉ sửa URL ảnh seed cũ, không ghi đè ảnh người dùng đã tự thay. */
    private void repairDemoSkirtImage(ProductVariantRepository variants,
                                      ProductImageRepository images,
                                      CategoryRepository categories) {
        String oldUrl = "https://images.unsplash.com/photo-1583496661160-fb5886a13d77?w=900&auto=format&fit=crop&q=85";
        String newUrl = "https://sakurafashion.vn/upload/sanpham/large/61627-chan-vay-xep-ly-nu-dang-dai-3.jpg";
        for (ProductVariant v : variants.findAll()) {
            if (!"DEMO-2026-006".equals(v.getSku())) continue;
            for (ProductImage img : images.findByProduct_ProductId(v.getProduct().getProductId())) {
                if (oldUrl.equals(img.getImageUrl())) {
                    img.setImageUrl(newUrl);
                    images.save(img);
                    System.out.println("[DEMO SEED] Đã sửa ảnh Chân váy xếp ly nữ.");
                }
            }
            Category category = v.getProduct().getCategory();
            if (category != null && oldUrl.equals(category.getImageUrl())) {
                category.setImageUrl(newUrl);
                categories.save(category);
            }
        }
    }

    /**
     * Bản cũ có thể đã seed sản phẩm DEMO-2026-* nhưng không có đơn COMPLETED.
     * Chỉ bổ sung đơn cho sản phẩm demo có số bán = 0; chạy lại không tạo trùng.
     * Không can thiệp dữ liệu của những sản phẩm thực khác.
     */
    private void repairDemoSoldCounts(ProductVariantRepository variants,
                                      ProductRepository products,
                                      OrderRepository orders,
                                      OrderItemRepository orderItems,
                                      UserRepository users) {
        User demoBuyer = users.findAll().stream()
                .filter(u -> "admin@gmail.com".equalsIgnoreCase(u.getEmail()))
                .findFirst().orElse(null);
        if (demoBuyer == null) return;
        int repaired = 0;
        for (ProductVariant variant : variants.findAll()) {
            String sku = variant.getSku();
            if (sku == null || !sku.matches("DEMO-2026-00[1-9]|DEMO-2026-010")) continue;
            Long productId = variant.getProduct().getProductId();
            if (products.getSoldCountByProductId(productId) > 0) continue;
            int number = Integer.parseInt(sku.substring(sku.length() - 3));
            int quantity = 12 + (number - 1) * 4;
            BigDecimal price = variant.getPrice() != null ? variant.getPrice() : variant.getProduct().getBasePrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
            Order order = orders.save(Order.builder()
                    .user(demoBuyer).address(demoBuyer.getAddress())
                    .totalAmount(total).discountAmount(BigDecimal.ZERO).finalAmount(total)
                    .orderStatus("COMPLETED").createdAt(LocalDateTime.now().minusDays(number))
                    .build());
            orderItems.save(OrderItem.builder().order(order).variant(variant)
                    .quantity(quantity).unitPrice(price).price(price).subtotal(total).build());
            repaired++;
        }
        if (repaired > 0) {
            System.out.println("[DEMO SEED] Đã bổ sung số lượng bán cho " + repaired + " sản phẩm demo.");
        }
    }

}
