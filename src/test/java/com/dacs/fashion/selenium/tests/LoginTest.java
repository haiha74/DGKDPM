
package com.dacs.fashion.selenium.tests;

import com.dacs.fashion.selenium.base.BaseTest;
import com.dacs.fashion.selenium.pages.LoginPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class LoginTest extends BaseTest {

    private static final String USER_EMAIL =
        System.getProperty("test.user.email", "");

    private static final String USER_PASSWORD =
        System.getProperty("test.user.password", "");

    private static final String STAFF_EMAIL =
        System.getProperty("test.staff.email", "");

    private static final String STAFF_PASSWORD =
        System.getProperty("test.staff.password", "");

    private static final String ADMIN_EMAIL =
        System.getProperty("test.admin.email", "");

    private static final String ADMIN_PASSWORD =
        System.getProperty("test.admin.password", "");

    // ==========================================
    // HÀM HỖ TRỢ
    // ==========================================

    private LoginPage page() {
        return new LoginPage(driver);
    }

    private void requireUser() {

        assumeTrue(
            !USER_EMAIL.isBlank()
                && !USER_PASSWORD.isBlank(),
            "Chưa cấu hình tài khoản USER"
        );
    }

    private void loginAsUser() {

        requireUser();

        page().login(USER_EMAIL, USER_PASSWORD);

        assertTrue(
            page().isLoggedIn(),
            "Không tìm thấy phiên đăng nhập USER"
        );

        assertEquals(
            "USER",
            page().getStoredRole(),
            "Vai trò đăng nhập phải là USER"
        );
    }

    private void waitForHomePage() {

        wait.until(
            ExpectedConditions.urlToBe(BASE_URL + "/")
        );
    }

    // ==========================================
    // DN_01 - ĐĂNG NHẬP USER HỢP LỆ
    // ==========================================

    @Test
    @DisplayName("DN_01 - Đăng nhập User hợp lệ")
    void DN01_validUserLogin() {

        loginAsUser();

        waitForHomePage();

        assertTrue(
            page().hasStoredSession(),
            "Không lưu phiên đăng nhập"
        );

        assertEquals(
            "USER",
            page().getStoredRole()
        );
    }

    // ==========================================
    // DN_02 - ĐĂNG NHẬP STAFF TẠI SHOP
    // ==========================================

    @Test
    @DisplayName("DN_02 - Staff bị từ chối tại trang Shop")
    void DN02_staffLoginAtShop() {

        assumeTrue(
            !STAFF_EMAIL.isBlank()
                && !STAFF_PASSWORD.isBlank(),
            "Chưa cấu hình tài khoản STAFF"
        );

        LoginPage loginPage = page();

        loginPage.login(STAFF_EMAIL, STAFF_PASSWORD);

        String actualMessage = loginPage.getError();

        System.out.println(
            "DN_02 - Thông báo thực tế: "
                + actualMessage
        );

        assertTrue(
            actualMessage.contains(
                "Admin/Staff không được đăng nhập"
            ),
            "Thông báo không đúng. Thực tế: "
                + actualMessage
        );

        assertFalse(
            loginPage.hasStoredSession(),
            "STAFF không được có phiên Shop"
        );
    }

    // ==========================================
    // DN_03 - ĐĂNG NHẬP ADMIN TẠI SHOP
    // ==========================================

    @Test
    @DisplayName("DN_03 - Admin bị từ chối tại trang Shop")
    void DN03_adminLoginAtShop() {

        assumeTrue(
            !ADMIN_EMAIL.isBlank()
                && !ADMIN_PASSWORD.isBlank(),
            "Chưa cấu hình tài khoản ADMIN"
        );

        LoginPage loginPage = page();

        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        String actualMessage = loginPage.getError();

        System.out.println(
            "DN_03 - Thông báo thực tế: "
                + actualMessage
        );

        assertTrue(
            actualMessage.contains(
                "Admin/Staff không được đăng nhập"
            ),
            "Thông báo không đúng. Thực tế: "
                + actualMessage
        );

        assertFalse(
            loginPage.hasStoredSession(),
            "ADMIN không được có phiên Shop"
        );
    }

    // ==========================================
    // DN_04 - ĐĂNG NHẬP SAI MẬT KHẨU
    // ==========================================

    @Test
    @DisplayName("DN_04 - Đăng nhập sai mật khẩu")
    void DN04_wrongPassword() {

        requireUser();

        LoginPage loginPage = page();

        loginPage.login(
            USER_EMAIL,
            "WrongPass123"
        );

        assertTrue(
            loginPage.getError().contains(
                "Email hoặc mật khẩu không chính xác"
            )
        );

        assertFalse(
            loginPage.hasStoredSession()
        );
    }

    // ==========================================
    // DN_05 - EMAIL KHÔNG TỒN TẠI
    // ==========================================

    @Test
    @DisplayName("DN_05 - Email không tồn tại")
    void DN05_unknownEmail() {

        String unknownEmail =
            "unknown"
            + UUID.randomUUID()
                .toString()
                .replace("-", "")
            + "@example.com";

        LoginPage loginPage = page();

        loginPage.login(
            unknownEmail,
            "Password123"
        );

        assertTrue(
            loginPage.getError().contains(
                "Email hoặc mật khẩu không chính xác"
            )
        );

        assertFalse(
            loginPage.hasStoredSession()
        );
    }

    // ==========================================
    // DN_06 - BỎ TRỐNG EMAIL
    // ==========================================

    @Test
    @DisplayName("DN_06 - Bỏ trống email")
    void DN06_emptyEmail() {

        LoginPage loginPage = page();

        loginPage.login(
            "",
            "Password123"
        );

        assertTrue(
            loginPage.getError().contains(
                "Vui lòng nhập email và mật khẩu"
            )
        );

        assertTrue(
            loginPage.isFieldInvalid("loginEmail")
        );

        assertFalse(
            loginPage.hasStoredSession()
        );
    }

    // ==========================================
    // DN_07 - BỎ TRỐNG MẬT KHẨU
    // ==========================================

    @Test
    @DisplayName("DN_07 - Bỏ trống mật khẩu")
    void DN07_emptyPassword() {

        LoginPage loginPage = page();

        loginPage.login(
            "user@example.com",
            ""
        );

        assertTrue(
            loginPage.getError().contains(
                "Vui lòng nhập email và mật khẩu"
            )
        );

        assertTrue(
            loginPage.isFieldInvalid(
                "loginPassword"
            )
        );

        assertFalse(
            loginPage.hasStoredSession()
        );
    }

    // ==========================================
    // DN_08 - BỎ TRỐNG CẢ HAI TRƯỜNG
    // ==========================================

    @Test
    @DisplayName("DN_08 - Bỏ trống email và mật khẩu")
    void DN08_allFieldsEmpty() {

        LoginPage loginPage = page();

        loginPage.login("", "");

        assertTrue(
            loginPage.getError().contains(
                "Vui lòng nhập email và mật khẩu"
            )
        );

        assertFalse(
            loginPage.hasStoredSession()
        );
    }

    // ==========================================
    // DN_09 - EMAIL SAI ĐỊNH DẠNG
    // ==========================================

    @Test
    @DisplayName("DN_09 - Email sai định dạng")
    void DN09_invalidEmailFormat() {

        LoginPage loginPage = page();

        loginPage.login(
            "invalid-email",
            "Password123"
        );

        assertTrue(
            loginPage.getError().contains(
                "Email hoặc mật khẩu không chính xác"
            )
        );

        assertFalse(
            loginPage.hasStoredSession()
        );
    }

    // ==========================================
    // DN_10 - USER KHÔNG TRUY CẬP API ADMIN
    // ==========================================

    @Test
    @DisplayName("DN_10 - User không truy cập API Admin")
    void DN10_userCannotAccessAdminAPI() {

        loginAsUser();

        waitForHomePage();

        JavascriptExecutor js =
            (JavascriptExecutor) driver;

        Object status = js.executeAsyncScript(
            "const done = arguments[arguments.length - 1];"
            + "const raw = localStorage.getItem('ha_user');"
            + "const user = JSON.parse(raw);"
            + "fetch('/api/users', {"
            + "  headers: {"
            + "    Authorization: 'Bearer ' + user.token"
            + "  }"
            + "})"
            + ".then(r => done(r.status))"
            + ".catch(e => done('ERROR: ' + e.message));"
        );

        assertTrue(
            status instanceof Number,
            "Không nhận được HTTP Status: "
                + status
        );

        assertEquals(
            403,
            ((Number) status).intValue(),
            "USER không được truy cập API Admin"
        );
    }

    // ==========================================
    // DN_11 - ĐĂNG NHẬP RỒI ĐĂNG XUẤT
    // ==========================================

    @Test
    @DisplayName("DN_11 - Đăng nhập rồi đăng xuất")
    void DN11_loginThenLogout() {

        loginAsUser();

        waitForHomePage();

        assertTrue(
            page().hasStoredSession(),
            "Phiên chưa được lưu sau đăng nhập"
        );

        // Mở trang tài khoản
        driver.get(BASE_URL + "/auth");

        // Chờ hiển thị nút Đăng xuất thực sự.
        // Nếu không thấy, test sẽ báo lỗi rõ nguyên nhân.
        page().waitForLogoutButton();

        assertTrue(
            page().hasStoredSession(),
            "Phiên bị mất khi mở trang /auth"
        );

        page().clickLogout();

        page().waitUntilLoggedOut();

        assertFalse(
            page().hasStoredSession(),
            "Phiên chưa bị xóa sau đăng xuất"
        );
    }

    // ==========================================
    // DN_12 - TẢI LẠI TRANG SAU ĐĂNG NHẬP
    // ==========================================

    @Test
    @DisplayName("DN_12 - Duy trì phiên sau khi F5")
    void DN12_refreshAfterLogin() {

        loginAsUser();

        waitForHomePage();

        assertTrue(
            page().hasStoredSession(),
            "Phiên chưa được lưu"
        );

        // Tải lại trang chủ
        driver.navigate().refresh();

        assertTrue(
            page().isLoggedIn(),
            "Phiên bị mất sau khi F5"
        );

        assertEquals(
            "USER",
            page().getStoredRole(),
            "Vai trò bị thay đổi sau F5"
        );

        // Kiểm tra giao diện nhận diện tài khoản
        driver.get(BASE_URL + "/auth");

        page().waitForLogoutButton();

        assertTrue(
            page().hasStoredSession(),
            "Phiên bị mất khi mở trang tài khoản"
        );

        assertTrue(
            page().isLogoutVisible(),
            "Không hiển thị nút Đăng xuất"
        );
    }
}
