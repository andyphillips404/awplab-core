package com.awplab.core.selenium.service;

import com.awplab.core.common.TemporaryFile;
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


    String CONFIG_MANAGED_SERVICE_NAME = "com.awplab.core.selenium.service";


    String PROPERTY_FORCE_LOCAL = "com.awplab.core.selenium.service.forceLocal";


    String PROPERTY_LOCAL_DRIVER_CLASS = "com.awplab.core.selenium.service.localDriverClass";


    String PROPERTY_REMOTE_URL = "com.awplab.core.selenium.service.remoteUrl";


    String PROPERTY_REMOTE_DESIRED_BROWSER = "com.awplab.core.selenium.service.remoteDesiredBrowser";

    String PROPERTY_REMOTE_DESIRED_VERSION = "com.awplab.core.selenium.service.remoteDesiredVersion";

    String PROPERTY_REMOTE_DESIRED_PLATFORM = "com.awplab.core.selenium.service.remoteDesiredPlatform";


    String PROPERTY_IMPLICITLY_WAIT_TIME = "com.awplab.core.selenium.service.implicitlyWaitTime";

    String PROPERTY_IMPLICITLY_WAIT_TIME_UNIT = "com.awplab.core.selenium.service.implicitlyWaitTimeUnit";

    String PROPERTY_PAGE_LOAD_TIMEOUT = "com.awplab.core.selenium.service.pageLoadTimeout";

    String PROPERTY_PAGE_LOAD_TIMEOUT_UNIT = "com.awplab.core.selenium.service.pageLoadTimeoutUnit";

    String PROPERTY_SCRIPT_TIMEOUT = "com.awplab.core.selenium.service.scriptTimeout";

    String PROPERTY_SCRIPT_TIMEOUT_UNIT = "com.awplab.core.selenium.service.scriptTimeoutUnit";


    String PROPERTY_WINDOW_WIDTH = "com.awplab.core.selenium.service.windowWidth";

    String PROPERTY_WINDOW_HEIGHT = "com.awplab.core.selenium.service.windowHeight";


    String PROPERTY_WAIT_UNTIL_TIMEOUT = "com.awplab.core.selenium.service.waitUntilTimeout";

    String PROPERTY_WAIT_UNTIL_TIMEOUT_UNIT = "com.awplab.core.selenium.service.waitUntilTimeoutUnit";


    String PROPERTY_FIREFOX_PROFILE_PREFERENCE_PAIRS = "com.awplab.core.selenium.service.firefoxProfilePreferencePairs";

    String PROPERTY_FIREFOX_PROFILE_TRUST_ALL_CERTS = "com.awplab.core.selenium.service.firefoxProfileTrustAllCerts";


    String PROPERTY_CHROME_EXPERIMENTAL_OPTIONS = "com.awplab.core.selenium.service.chromeExperimentalOptions";

    String PROPERTY_CHROME_ARGUMENTS = "com.awplab.core.selenium.service.chromeArguments";

}
