package com.dacs.fashion.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By switchRegister = By.xpath(
        "//button[contains(normalize-space(.), 'Đăng ký ngay')]"
    );

    private final By fullname = By.id("regFullname");
    private final By email = By.id("regEmail");
    private final By password = By.id("regPassword");
    private final By confirm = By.id("regConfirm");
    private final By submit = By.id("registerSubmit");

    private final By errorMessage = By.id("authMsgText");
    private final By successToast = By.id("toast");
    private final By loginEmail = By.id("loginEmail");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver, Duration.ofSeconds(10)
        );
    }

    public void openRegister() {
        wait.until(
            ExpectedConditions.elementToBeClickable(
                switchRegister
            )
        ).click();

        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                fullname
            )
        );
    }

    private void fill(By locator, String value) {
        WebElement element = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                locator
            )
        );

        element.clear();

        if (value != null && !value.isEmpty()) {
            element.sendKeys(value);
        }
    }

    public void enterFullname(String value) {
        fill(fullname, value);
    }

    public void enterEmail(String value) {
        fill(email, value);
    }

    public void enterPassword(String value) {
        fill(password, value);
    }

    public void enterConfirm(String value) {
        fill(confirm, value);
    }

    public void fillRegister(
            String name,
            String mail,
            String pass,
            String confirmPass) {

        enterFullname(name);
        enterEmail(mail);
        enterPassword(pass);
        enterConfirm(confirmPass);
    }

    public void clickRegister() {
        wait.until(
            ExpectedConditions.elementToBeClickable(
                submit
            )
        ).click();
    }

    public String getError() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                errorMessage
            )
        ).getText();
    }

    public boolean isFieldInvalid(String fieldId) {
        return wait.until(
            ExpectedConditions.attributeToBe(
                By.id(fieldId),
                "aria-invalid",
                "true"
            )
        );
    }

    public boolean isRegisterSuccess() {
        // auth.js chuyển sang form đăng nhập
        // sau khi đăng ký thành công.
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                loginEmail
            )
        );

        return !driver.findElements(loginEmail).isEmpty();
    }

    public boolean hasSuccessToast() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                successToast
            )
        ).getText().contains("Đăng ký thành công");
    }
}
