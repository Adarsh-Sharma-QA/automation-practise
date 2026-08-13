package com.practise.automation;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.hc.core5.util.Asserts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;

/**
 * Minimal, standalone Selenium driver setup - nothing else. Run this file
 * directly to confirm your environment (JDK, Maven deps, Chrome browser)
 * is wired up correctly before moving on to real test scripts.
 */
public class DriverSetup {

    public static WebDriver createDriver() {
        WebDriverManager.chromedriver().setup(); // detects installed Chrome, downloads matching driver

        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        return driver;
    }

    public static void main(String[] args) {
        WebDriver driver = createDriver();
        try {
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
            driver.get("https://datatables.net/examples/index");
            
            WebElement ZeroConfig = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Zero configuration']")));
            ZeroConfig.click();
            
            WebElement Count = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='example_info']")));
            String[] Split = Count.getText().split("of");
            // Split.split("of")
            String number = Split[1].split("entries")[0].trim();
            int Records = Integer.parseInt(number);
            
            if(Records < 25 ){
                throw new AssertionError("Records are less than 25 Records");
            }
            
                        WebElement sortAge = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//th[@class='dt-type-numeric dt-orderable-asc dt-orderable-desc'][1]")));
            sortAge.click();
            sortAge.click();
            
             WebElement ageCount = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//tr/td[4])[1]")));
            String agenumber = ageCount.getText();
            int age = Integer.parseInt(agenumber);

            if(age < 65 ){
                throw new AssertionError("First Records have age less than 65 ");
            }


        } finally {
            driver.quit();
        }
    }
}
