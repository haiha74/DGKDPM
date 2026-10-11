# HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY PROJECT

## 1. Yêu cầu

Máy tính cần cài đặt:

- Java JDK 17
- MySQL Server
- Git
- VS Code / IntelliJ IDEA / Eclipse

## Cấu trúc cơ bản của Project

```text
Do_an_co_so/
│
├── src/
│   ├── main/
│   │   ├── java/com/dacs/fashion/
│   │   │   ├── config/          # Cấu hình hệ thống, JWT, Seed Data
│   │   │   ├── controller/      # Xử lý các HTTP Request/API
│   │   │   ├── service/         # Xử lý logic nghiệp vụ
│   │   │   ├── repository/      # Truy vấn cơ sở dữ liệu
│   │   │   ├── entity/          # Các thực thể ánh xạ bảng MySQL
│   │   │   ├── dto/             # Dữ liệu truyền giữa các lớp
│   │   │   └── FashionApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/          # CSS, JavaScript, hình ảnh
│   │       ├── templates/       # Giao diện Thymeleaf
│   │       └── application.properties
│   │
│   └── test/                    # Kiểm thử
│
├── database/                    # Script cơ sở dữ liệu
├── pom.xml                      # Cấu hình Maven và dependencies
├── mvnw                         # Maven Wrapper cho Linux/macOS
├── mvnw.cmd                     # Maven Wrapper cho Windows
└── README.md                    # Hướng dẫn cài đặt và chạy
```

**Kiến trúc hoạt động:**

`Frontend → Controller → Service → Repository → MySQL`

Project sử dụng mô hình phân lớp, giúp dễ dàng quản lý, bảo trì và phát triển thêm chức năng.
## 2. Clone project

Mở Terminal và chạy:

```bash
git clone https://github.com/haiha74/DGKDPM.git
cd DGKDPM
```

Mở project bằng VS Code:

```bash
code .
```

## 3. Cấu hình Database

Mở file:

`src/main/resources/application.properties`

Nếu file nằm trong thư mục `Do_an_co_so`, mở file theo đường dẫn tương ứng trong thư mục đó.

Cấu hình:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/${DB_NAME:fashion_clone_demo}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh

spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update
app.seed.enabled=true
```

**Giải thích:**
- `username`: Tên đăng nhập MySQL, mặc định `root`.
- `password`: Mật khẩu MySQL trên máy cá nhân, mặc định để trống.
- `fashion_clone_demo`: Tên database được sử dụng.
- `createDatabaseIfNotExist=true`: Tự tạo database nếu chưa có và tài khoản MySQL đủ quyền.
- `ddl-auto=update`: Tự tạo và cập nhật bảng.
- `app.seed.enabled=true`: Bật dữ liệu mẫu.

**Lưu ý:** Mỗi người sử dụng MySQL riêng trên máy mình, không dùng database gốc. Không commit mật khẩu MySQL lên GitHub.

## 4. Khởi động project

Đảm bảo MySQL Server đang chạy.

Mở Terminal tại thư mục chứa `pom.xml`, chạy:

```bash
mvn spring-boot:run
```

Hoặc sử dụng Maven Wrapper trên Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Truy cập website:

**http://localhost:8080/**
**http://localhost:8080/admin**
Khi khởi động, ứng dụng sẽ tự tạo database, các bảng và thêm dữ liệu mẫu nếu đáp ứng điều kiện seed.

## 5. Tài khoản Admin demo
**http://localhost:8080/admin**
| Thông tin | Giá trị |
|---|---|
| Email | admin@gmail.com |
| Mật khẩu | 123456 |
| Quyền | ADMIN |

Tài khoản này chỉ dùng để demo và kiểm thử.

## 6. Lỗi thường gặp

- **Access denied:** Sai username hoặc password MySQL.
- **Communications link failure:** MySQL Server chưa chạy hoặc sai cổng.
- **Unknown database:** Kiểm tra `createDatabaseIfNotExist=true` và quyền tạo database.
- **Port 8080 already in use:** Cổng 8080 đang bị ứng dụng khác sử dụng.
- **Không có dữ liệu mẫu:** Kiểm tra `app.seed.enabled=true` và log khi khởi động.

**Lưu ý:** Không cần import database thủ công nếu MySQL đã được cấu hình đúng và tài khoản có quyền tạo database.
