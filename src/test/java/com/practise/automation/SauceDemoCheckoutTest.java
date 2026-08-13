package com.practise.automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * ~10 step end-to-end scenario: log in, add an item to the cart, and
 * complete checkout on saucedemo.com (a public sandbox built for this
 * exact kind of practice, standard_user/secret_sauce are demo creds).
 */
public class SauceDemoCheckoutTest {

    private static final String BASE_URL = "https://www.saucedemo.com/";
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        driver = DriverSetup.createDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void completesCheckoutEndToEnd() {
        // 1. Open the site
        driver.get(BASE_URL);

        // 2. Log in
        driver.findElement(By.id("user-name")).sendKeys("standard_user");
        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        click(By.id("login-button"));

        // 3. Verify inventory page loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should land on inventory page after login");

        // 4. Add an item to the cart
        click(By.id("add-to-cart-sauce-labs-backpack"));

        // 5. Verify cart badge shows 1 item
        String badgeCount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge"))).getText();
        Assert.assertEquals(badgeCount, "1", "Cart badge should show 1 item added");

        // 6. Go to the cart
        click(By.className("shopping_cart_link"));
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // 7. Verify the correct item is in the cart
        List<WebElement> cartItems = driver.findElements(By.className("cart_item"));
        Assert.assertEquals(cartItems.size(), 1, "Cart should contain exactly 1 item");
        Assert.assertTrue(driver.findElement(By.className("inventory_item_name")).getText().contains("Backpack"));

        // 8. Proceed to checkout and fill in shipping info
        click(By.id("checkout"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        driver.findElement(By.id("first-name")).sendKeys("Adarsh");
        driver.findElement(By.id("last-name")).sendKeys("Sharma");
        driver.findElement(By.id("postal-code")).sendKeys("00000");
        // The zip field's value can lag behind the keystrokes on this SPA, so confirm
        // it actually landed before submitting - otherwise Continue silently no-ops.
        wait.until(d -> "00000".equals(d.findElement(By.id("postal-code")).getAttribute("value")));
        click(By.id("continue"));

        // 9. Verify overview page and finish the order
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        Assert.assertTrue(driver.findElement(By.className("summary_total_label")).getText().contains("Total"));
        click(By.id("finish"));

        // 10. Verify order completion
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        Assert.assertEquals(completeHeader.getText(), "Thank you for your order!");
    }

    /** Waits for the element to be clickable before clicking - avoids races right after a page/route transition. */
    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
