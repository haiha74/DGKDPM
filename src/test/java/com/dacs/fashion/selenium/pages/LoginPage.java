
package com.dacs.fashion.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By email = By.id("loginEmail");
    private final By password = By.id("loginPassword");
    private final By submit = By.id("loginSubmit");
    private final By error = By.id("authMsgText");

    private final By logoutButton = By.cssSelector(
        "button[onclick='logout()']"
    );

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
            driver, Duration.ofSeconds(15)
        );
    }

    public void enterEmail(String value) {

        WebElement element = wait.until(
            ExpectedConditions.visibilityOfElementLocated(email)
        );

        element.clear();

        if (value != null && !value.isEmpty()) {
            element.sendKeys(value);
        }
    }

    public void enterPassword(String value) {

        WebElement element = wait.until(
            ExpectedConditions.visibilityOfElementLocated(password)
        );

        element.clear();

        if (value != null && !value.isEmpty()) {
            element.sendKeys(value);
        }
    }

    public void clickLogin() {

        wait.until(
            ExpectedConditions.elementToBeClickable(submit)
        ).click();
    }

    public void login(String mail, String pass) {
        enterEmail(mail);
        enterPassword(pass);
        clickLogin();
    }

    public String getError() {

        return wait.until(d -> {

            WebElement element = d.findElement(error);
            String message = element.getText().trim();

            return message.isEmpty() ? null : message;
        });
    }

    public boolean isFieldInvalid(String fieldId) {

        return "true".equals(
            driver.findElement(By.id(fieldId))
                  .getAttribute("aria-invalid")
        );
    }

    public boolean hasStoredSession() {

        Object result = ((JavascriptExecutor) driver)
            .executeScript(
                "return localStorage.getItem('ha_user') !== null;"
            );

        return Boolean.TRUE.equals(result);
    }

    public boolean isLoggedIn() {

        return wait.until(d -> {

            Object result = ((JavascriptExecutor) d)
                .executeScript(
                    "return localStorage.getItem('ha_user') !== null;"
                );

            return Boolean.TRUE.equals(result);
        });
    }

    public String getStoredRole() {

        Object result = ((JavascriptExecutor) driver)
            .executeScript(
                "const raw = localStorage.getItem('ha_user');" +
                "if (!raw) return null;" +
                "try { return JSON.parse(raw).role; }" +
                "catch (e) { return null; }"
            );

        return result == null ? null : result.toString();
    }

    public boolean isLogoutVisible() {

        return driver.findElements(logoutButton)
            .stream()
            .anyMatch(WebElement::isDisplayed);
    }

    public void waitForLogoutButton() {

        try {

            wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    logoutButton
                )
            );

        } catch (TimeoutException ex) {

            throw new AssertionError(
                "Không tìm thấy nút Đăng xuất tại "
                + driver.getCurrentUrl()
                + ". Kiểm tra trạng thái phiên đăng nhập "
                + "và request GET /api/account/me.",
                ex
            );
        }
    }

    public void clickLogout() {

        wait.until(
            ExpectedConditions.elementToBeClickable(
                logoutButton
            )
        ).click();
    }

    public void waitUntilLoggedOut() {

        wait.until(d -> {

            Object result = ((JavascriptExecutor) d)
                .executeScript(
                    "return localStorage.getItem('ha_user') === null;"
                );

            return Boolean.TRUE.equals(result);
        });
    }
}
