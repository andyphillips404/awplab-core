package com.awplab.core.selenium.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by andyphillips404 on 4/23/16.
 */
public interface SeleniumService {


    DesiredCapabilities getRemoteDesiredCapabilities();

    FirefoxProfile getFirefoxProfile();

    ChromeOptions getChromeOptions();

    AutoClosableWebDriver getWebDriver();

    AutoClosableWebDriver getRemoteDriver(DesiredCapabilities desiredCapabilities);

    AutoClosableWebDriver getLocalDriver();

    AutoClosableWebDriver wrapDriver(WebDriver webDriver);


}
