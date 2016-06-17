package com.awplab.core.selenium.service;

import com.awplab.core.common.TemporaryFile;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 4/23/16.
 */
public class AutoClosableWebDriver implements WebDriver, AutoCloseable {


    private WebDriver webDriver;

    public AutoClosableWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    @Override
    public void get(String s) {
        webDriver.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return webDriver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return webDriver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return webDriver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return webDriver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return webDriver.getPageSource();
    }

    @Override
    public void close() {
        //webDriver.close();

        // for a quit with all close calls as this is now a full AutoClosableWebDriver
        webDriver.quit();
    }

    @Override
    public void quit() {
        webDriver.quit();
    }

    public void closeWindow() {
        webDriver.close();
    }

    @Override
    public Set<String> getWindowHandles() {
        return webDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return webDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return webDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return webDriver.navigate();
    }

    @Override
    public Options manage() {
        return webDriver.manage();
    }


    private int defaultRetryCount = 5;

    public int getDefaultRetryCount() {
        return defaultRetryCount;
    }

    public void setDefaultRetryCount(int defaultRetryCount) {
        this.defaultRetryCount = defaultRetryCount;
    }

    private long defaultWaitUntilTimeout = 1;

    private TimeUnit defaultWaitUntilTimeoutUnit = TimeUnit.MINUTES;

    public long getDefaultWaitUntilTimeout() {
        return defaultWaitUntilTimeout;
    }

    public void setDefaultWaitUntilTimeout(long defaultWaitUntilTimeout) {
        this.defaultWaitUntilTimeout = defaultWaitUntilTimeout;
    }

    public TimeUnit getDefaultWaitUntilTimeoutUnit() {
        return defaultWaitUntilTimeoutUnit;
    }

    public void setDefaultWaitUntilTimeoutUnit(TimeUnit defaultWaitUntilTimeoutUnit) {
        this.defaultWaitUntilTimeoutUnit = defaultWaitUntilTimeoutUnit;
    }

    public <T> T waitUntil(ExpectedCondition<T> condition) {
        return waitUntil(defaultWaitUntilTimeout, defaultWaitUntilTimeoutUnit, condition);
    }

    public <T> T waitUntil(Long duration, TimeUnit timeUnit, ExpectedCondition<T> condition) {
        WebDriverWait webDriverWait = new WebDriverWait(webDriver, timeUnit.toSeconds(duration));
        return webDriverWait.until(condition);

    }

    public <T> T get(String url, ExpectedCondition<T> expectedCondition) {
        this.get(url);
        return waitUntil(expectedCondition);
    }

    public <T> T get(String url, ExpectedCondition<T> expectedCondition, Long duration, TimeUnit timeUnit) {
        this.get(url);
        return waitUntil(duration, timeUnit, expectedCondition);
    }

    public <T> T getWithRetry(String url, ExpectedCondition<T> expectedCondition) {
        return getWithRetry(url, expectedCondition, defaultRetryCount);
    }


    public <T> T getWithRetry(String url, ExpectedCondition<T> expectedCondition, int retryCount) {
        return this.getWithRetry(url, expectedCondition, defaultWaitUntilTimeout, defaultWaitUntilTimeoutUnit, retryCount);
    }

    public <T> T getWithRetry(String url, ExpectedCondition<T> expectedCondition, Long duration, TimeUnit timeUnit, int retryCount) {
        TimeoutException lastException = null;
        for (int ctr = 0; ctr < retryCount; ctr++) {
            try {
                return get(url, expectedCondition, duration, timeUnit);
            }
            catch (TimeoutException ignored) {
                lastException = ignored;
            }
        }

        throw lastException;

    }

    public Optional<String> findOptionalElementText(By by) {
        return this.findElements(by).stream().findFirst().flatMap(webElement -> Optional.of(webElement.getText()));
    }

    public Optional<WebElement> findOptionalElement(By by) {
        return this.findElements(by).stream().findFirst();
    }

    public TemporaryFile getScreenshot() {
        if (webDriver instanceof TakesScreenshot) {
            return TemporaryFile.wrapByAbsolutePath(((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE));
        }
        else {
            WebDriver augmentedDriver = new Augmenter().augment(webDriver);
            return TemporaryFile.wrapByAbsolutePath(((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE));
        }

    }


}
