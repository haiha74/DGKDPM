package com.dacs.fashion.selenium.tests;

import com.dacs.fashion.selenium.base.BaseTest;
import com.dacs.fashion.selenium.pages.RegisterPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest extends BaseTest {

    private static final String PASSWORD = "Password123";

    private String newEmail(String prefix) {
        String unique = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        return prefix + unique + "@example.com";
    }

    private RegisterPage openRegisterPage() {
        RegisterPage page = new RegisterPage(driver);
        page.openRegister();
        return page;
    }

    private void registerSuccessfully(
            String name,
            String email) {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                name,
                email,
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(page.isRegisterSuccess());
    }

    // ==========================================
    // DK_01 - DANG KY HOP LE
    // ==========================================

    @Test
    @DisplayName("DK_01 - Đăng ký tài khoản hợp lệ")
    void DK01_registerValidAccount() {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn A",
                newEmail("dk01"),
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(page.isRegisterSuccess());

        assertTrue(page.hasSuccessToast());
    }

    // ==========================================
    // DK_02 - EMAIL DA TON TAI
    // ==========================================

    @Test
    @DisplayName("DK_02 - Đăng ký email đã tồn tại")
    void DK02_registerDuplicateEmail() {

        String email = newEmail("dk02");

        registerSuccessfully("Nguyễn Văn A", email);

        // Quay về trang auth, mở form đăng ký
        driver.get(BASE_URL + "/auth");

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn B",
                email,
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "Email đã được sử dụng"
                )
        );
    }

    // ==========================================
    // DK_03 - BO TRONG EMAIL
    // ==========================================

    @Test
    @DisplayName("DK_03 - Bỏ trống email")
    void DK03_emptyEmail() {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn A",
                "",
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "định dạng email"
                )
        );

        assertTrue(page.isFieldInvalid("regEmail"));
    }

    // ==========================================
    // DK_04 - BO TRONG MAT KHAU
    // ==========================================

    @Test
    @DisplayName("DK_04 - Bỏ trống mật khẩu")
    void DK04_emptyPassword() {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn A",
                newEmail("dk04"),
                "",
                ""
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "Mật khẩu cần có"
                )
        );

        assertTrue(page.isFieldInvalid("regPassword"));
    }

    // ==========================================
    // DK_05 - EMAIL SAI DINH DANG
    // ==========================================

    @Test
    @DisplayName("DK_05 - Email sai định dạng")
    void DK05_invalidEmail() {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn A",
                "abc@",
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "định dạng email"
                )
        );

        assertTrue(page.isFieldInvalid("regEmail"));
    }

    // ==========================================
    // DK_06 - BO TRONG TAT CA
    // ==========================================

    @Test
    @DisplayName("DK_06 - Bỏ trống toàn bộ thông tin")
    void DK06_allFieldsEmpty() {

        RegisterPage page = openRegisterPage();

        page.clickRegister();

        // Form kiểm tra họ tên trước tiên.
        assertTrue(
                page.getError().contains(
                        "Họ tên phải có"
                )
        );

        assertTrue(page.isFieldInvalid("regFullname"));
    }

    // ==========================================
    // DK_07 - EMAIL CO KHOANG TRANG
    // ==========================================

    @Test
    @DisplayName("DK_07 - Email có khoảng trắng đầu cuối")
    void DK07_emailWithSpaces() {

        RegisterPage page = openRegisterPage();

        String email = newEmail("dk07");

        page.fillRegister(
                "Nguyễn Văn A",
                "  " + email + "  ",
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        // auth.js thực hiện trim() trước khi gửi API.
        assertTrue(page.isRegisterSuccess());
    }

    // ==========================================
    // DK_08 - DANG KY HAI LAN
    // ==========================================

    @Test
    @DisplayName("DK_08 - Gửi đăng ký hai lần cùng email")
    void DK08_registerTwice() {

        String email = newEmail("dk08");

        registerSuccessfully("Nguyễn Văn A", email);

        driver.get(BASE_URL + "/auth");

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn A",
                email,
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "Email đã được sử dụng"
                )
        );
    }

    // ==========================================
    // DK_09 - DANG KY ROI DANG NHAP
    // ==========================================

    @Test
    @DisplayName("DK_09 - Đăng ký xong có thể đăng nhập")
    void DK09_registerThenLogin() {

        String email = newEmail("dk09");

        registerSuccessfully("Nguyễn Văn A", email);

        // Sau đăng ký thành công,
        // auth.js chuyển về form đăng nhập.
        WebElement loginEmail = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("loginEmail")
                )
        );

        loginEmail.sendKeys(email);

        driver.findElement(
                By.id("loginPassword")
        ).sendKeys(PASSWORD);

        driver.findElement(
                By.id("loginSubmit")
        ).click();

        // Đăng nhập thành công chuyển về trang chủ.
        wait.until(
                ExpectedConditions.urlToBe(BASE_URL + "/")
        );

        assertEquals(
                BASE_URL + "/",
                driver.getCurrentUrl()
        );
    }

    // ==========================================
    // DK_10 - HO TEN CO DAU GACH NOI
    // ==========================================

    @Test
    @DisplayName("DK_10 - Họ tên có dấu gạch nối")
    void DK10_nameWithHyphen() {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn A-B",
                newEmail("dk10"),
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(page.isRegisterSuccess());
    }

    // ==========================================
    // DK_11 - BO TRONG HO TEN
    // ==========================================

    @Test
    @DisplayName("DK_11 - Bỏ trống họ tên")
    void DK11_emptyFullname() {

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "",
                newEmail("dk11"),
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "Họ tên phải có"
                )
        );

        assertTrue(page.isFieldInvalid("regFullname"));
    }

    // ==========================================
    // DK_12 - EMAIL KHAC HOA THUONG
    // ==========================================

    @Test
    @DisplayName("DK_12 - Email trùng khác chữ hoa thường")
    void DK12_caseInsensitiveEmail() {

        String email = newEmail("dk12");

        registerSuccessfully("Nguyễn Văn A", email);

        driver.get(BASE_URL + "/auth");

        RegisterPage page = openRegisterPage();

        page.fillRegister(
                "Nguyễn Văn B",
                email.toUpperCase(),
                PASSWORD,
                PASSWORD
        );

        page.clickRegister();

        assertTrue(
                page.getError().contains(
                        "Email đã được sử dụng"
                )
        );
    }
}
