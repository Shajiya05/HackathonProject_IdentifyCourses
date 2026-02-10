package com.project.base;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js = (JavascriptExecutor) driver;
    }

    protected WebElement waitFor(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String value) {
        WebElement el = waitFor(locator);
        el.clear();
        el.sendKeys(value);
    }

    protected void pressEnter(By locator) {
        waitFor(locator).sendKeys(Keys.ENTER);
    }
    int a=0;
    protected void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }
}
