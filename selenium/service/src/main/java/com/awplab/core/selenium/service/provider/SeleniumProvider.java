package com.awplab.core.selenium.service.provider;

import com.awplab.core.selenium.service.AutoClosableWebDriver;
import com.awplab.core.selenium.service.SeleniumService;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 4/23/16.
 */

@Component(immediate = true, publicFactory=false, managedservice = SeleniumProvider.CONFIG_MANAGED_SERVICE_NAME)
@Instantiate
@Provides(specifications = SeleniumService.class)
public class SeleniumProvider implements SeleniumService {
    public static final String CONFIG_MANAGED_SERVICE_NAME = "com.awplab.core.selenium.service";


    public static final String PROPERTY_FORCE_LOCAL = "com.awplab.core.selenium.service.forceLocal";


    public static final String PROPERTY_LOCAL_DRIVER_CLASS = "com.awplab.core.selenium.service.localDriverClass";


    public static final String PROPERTY_REMOTE_URL = "com.awplab.core.selenium.service.remoteUrl";


    public static final String PROPERTY_REMOTE_DESIRED_BROWSER = "com.awplab.core.selenium.service.remoteDesiredBrowser";

    public static final String PROPERTY_REMOTE_DESIRED_VERSION = "com.awplab.core.selenium.service.remoteDesiredVersion";

    public static final String PROPERTY_REMOTE_DESIRED_PLATFORM = "com.awplab.core.selenium.service.remoteDesiredPlatform";


    public static final String PROPERTY_IMPLICITLY_WAIT_TIME = "com.awplab.core.selenium.service.implicitlyWaitTime";

    public static final String PROPERTY_IMPLICITLY_WAIT_TIME_UNIT = "com.awplab.core.selenium.service.implicitlyWaitTimeUnit";

    public static final String PROPERTY_PAGE_LOAD_TIMEOUT = "com.awplab.core.selenium.service.pageLoadTimeout";

    public static final String PROPERTY_PAGE_LOAD_TIMEOUT_UNIT = "com.awplab.core.selenium.service.pageLoadTimeoutUnit";

    public static final String PROPERTY_SCRIPT_TIMEOUT = "com.awplab.core.selenium.service.scriptTimeout";

    public static final String PROPERTY_SCRIPT_TIMEOUT_UNIT = "com.awplab.core.selenium.service.scriptTimeoutUnit";


    public static final String PROPERTY_WINDOW_WIDTH = "com.awplab.core.selenium.service.windowWidth";

    public static final String PROPERTY_WINDOW_HEIGHT = "com.awplab.core.selenium.service.windowHeight";


    public static final String PROPERTY_WAIT_UNTIL_TIMEOUT = "com.awplab.core.selenium.service.waitUntilTimeout";

    public static final String PROPERTY_WAIT_UNTIL_TIMEOUT_UNIT = "com.awplab.core.selenium.service.waitUntilTimeoutUnit";


    public static final String PROPERTY_FIREFOX_PROFILE_PREFERENCE_PAIRS = "com.awplab.core.selenium.service.firefoxProfilePreferencePairs";

    public static final String PROPERTY_FIREFOX_PROFILE_TRUST_ALL_CERTS = "com.awplab.core.selenium.service.firefoxProfileTrustAllCerts";


    public static final String PROPERTY_CHROME_EXPERIMENTAL_OPTIONS = "com.awplab.core.selenium.service.chromeExperimentalOptions";

    public static final String PROPERTY_CHROME_ARGUMENTS = "com.awplab.core.selenium.service.chromeArguments";


    @ServiceProperty(name = PROPERTY_FORCE_LOCAL, value = "false")
    private boolean forceLocal;

    @ServiceProperty(name = PROPERTY_LOCAL_DRIVER_CLASS, value = "org.openqa.selenium.firefox.FirefoxDriver")
    //@ServiceProperty(name = PROPERTY_LOCAL_DRIVER_CLASS, value = "org.openqa.selenium.chrome.ChromeDriver")
    private String localDriverClass;

    @ServiceProperty(name = PROPERTY_REMOTE_URL)
    private String remoteUrl = null; //"http://192.168.99.100:32768/wd/hub";

    @ServiceProperty(name = PROPERTY_REMOTE_DESIRED_BROWSER, value = "firefox")
    private String remoteDesiredBrowser;

    @ServiceProperty(name = PROPERTY_REMOTE_DESIRED_VERSION, value = "")
    private String remoteDesiredVersion;

    @ServiceProperty(name = PROPERTY_REMOTE_DESIRED_PLATFORM, value = "ANY")
    private String remoteDesiredPlatform;


    @ServiceProperty(name = PROPERTY_FIREFOX_PROFILE_PREFERENCE_PAIRS)
    private String[] firefoxProfilePreferencePairs;

    @ServiceProperty(name = PROPERTY_FIREFOX_PROFILE_TRUST_ALL_CERTS, value = "true")
    private boolean firefoxProfileTrustAllCerts;

    @ServiceProperty(name = PROPERTY_CHROME_EXPERIMENTAL_OPTIONS)
    private String[] chromeExperimentalOptions;

    @ServiceProperty(name = PROPERTY_CHROME_ARGUMENTS)
    private String[] chromeArguments;

    @ServiceProperty(name = PROPERTY_WAIT_UNTIL_TIMEOUT)
    private Long waitUntilTimeout;

    @ServiceProperty(name = PROPERTY_WAIT_UNTIL_TIMEOUT_UNIT)
    private String waitUntilTimeoutUnit;

    @ServiceProperty(name = PROPERTY_IMPLICITLY_WAIT_TIME)
    private Long implicityWaitTime;

    @ServiceProperty(name = PROPERTY_IMPLICITLY_WAIT_TIME_UNIT)
    private String implicityWaitTimeUnit;

    @ServiceProperty(name = PROPERTY_PAGE_LOAD_TIMEOUT)
    private Long pageLoadTimeout;

    @ServiceProperty(name = PROPERTY_PAGE_LOAD_TIMEOUT_UNIT)
    private String pageLoadTimeoutUnit;

    @ServiceProperty(name = PROPERTY_SCRIPT_TIMEOUT)
    private Long scriptTimeout;

    @ServiceProperty(name = PROPERTY_SCRIPT_TIMEOUT_UNIT)
    private String scriptTimeoutUnit;

    @ServiceProperty(name = PROPERTY_WINDOW_WIDTH)
    private Integer windowWidth;

    @ServiceProperty(name = PROPERTY_WINDOW_HEIGHT)
    private Integer windowHeight;

    private Logger logger = LoggerFactory.getLogger(SeleniumProvider.class);

    @Override
    public DesiredCapabilities getRemoteDesiredCapabilities() {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(remoteDesiredBrowser, remoteDesiredVersion, (remoteDesiredPlatform == null ? Platform.ANY : Enum.valueOf(Platform.class, remoteDesiredPlatform)));
        desiredCapabilities.setCapability(FirefoxDriver.PROFILE, getFirefoxProfile());
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, getChromeOptions());
        return desiredCapabilities;
    }

    private interface ProcessOption {
        void process(String name, Object value);
    }


    private void processOptionValues(String[] optionPairs, ProcessOption processOption) {
        if (optionPairs != null && optionPairs.length > 0) {
            for (int x = 0; x + 1 < optionPairs.length; x += 2) {
                try {
                    processOption.process(optionPairs[x], Integer.parseInt(optionPairs[x + 1]));
                } catch (NumberFormatException ignored) {
                    if (optionPairs[x + 1].equalsIgnoreCase("false") || optionPairs[x + 1].equalsIgnoreCase("true")) {
                        processOption.process(optionPairs[x], Boolean.valueOf(optionPairs[x + 1]));
                    } else {
                        processOption.process(optionPairs[x], optionPairs[x + 1]);
                    }
                }
            }
        }
    }

    @Override
    public FirefoxProfile getFirefoxProfile() {
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        processOptionValues(firefoxProfilePreferencePairs, (name, value) -> {
            if (value instanceof Integer) firefoxProfile.setPreference(name, (Integer)value);
            if (value instanceof Boolean) firefoxProfile.setPreference(name, (Boolean)value);
            if (value instanceof String) firefoxProfile.setPreference(name, (String)value);
        });
        firefoxProfile.setAcceptUntrustedCertificates(firefoxProfileTrustAllCerts);
        return firefoxProfile;
    }

    @Override
    public ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        processOptionValues(chromeExperimentalOptions, chromeOptions::setExperimentalOption);
        if (chromeArguments != null && chromeArguments.length > 0) chromeOptions.addArguments(chromeArguments);
        return chromeOptions;
    }



    @Override
    public AutoClosableWebDriver getWebDriver() {

        if (forceLocal || remoteUrl == null) {
            return getLocalDriver();
        }
        else {
            return getRemoteDriver(getRemoteDesiredCapabilities());
        }

    }

    @Override
    public AutoClosableWebDriver getLocalDriver() {
        try {
            Class<WebDriver> driverClass = (Class<WebDriver>) Class.forName(localDriverClass);

            if (driverClass.isAssignableFrom(FirefoxDriver.class)) {
                return wrapDriver(driverClass.getDeclaredConstructor(FirefoxProfile.class).newInstance(getFirefoxProfile()));
            }
            if (driverClass.isAssignableFrom(ChromeDriver.class)) {
                return wrapDriver(driverClass.getDeclaredConstructor(ChromeOptions.class).newInstance(getChromeOptions()));
            }

            return wrapDriver(driverClass.newInstance());
        }
        catch (ClassNotFoundException ex) {
            logger.error("Class not found define in " + PROPERTY_LOCAL_DRIVER_CLASS, ex);
            return null;
        }
        catch (Exception ex) {
            logger.error("Exception creating local web driver", ex);
            return null;
        }

    }

    @Override
    public AutoClosableWebDriver getRemoteDriver(DesiredCapabilities desiredCapabilities) {

        try {
            return wrapDriver(new RemoteWebDriver(new URL(remoteUrl), desiredCapabilities));
        }
        catch (MalformedURLException ex) {
            logger.error("Remote url malformed as defined in " + PROPERTY_REMOTE_URL, ex);
            return null;
        }

    }

    @Override
    public AutoClosableWebDriver wrapDriver(WebDriver webDriver) {
        AutoClosableWebDriver autoClosableWebDriver = new AutoClosableWebDriver(webDriver);
        if (waitUntilTimeout != null && waitUntilTimeoutUnit != null) {
            autoClosableWebDriver.setDefaultWaitUntilTimeout(waitUntilTimeout);
            autoClosableWebDriver.setDefaultWaitUntilTimeoutUnit(TimeUnit.valueOf(waitUntilTimeoutUnit));
        }
        if (implicityWaitTime != null && implicityWaitTimeUnit != null) {
            autoClosableWebDriver.manage().timeouts().implicitlyWait(implicityWaitTime, TimeUnit.valueOf(implicityWaitTimeUnit));
        }
        if (pageLoadTimeout != null && pageLoadTimeoutUnit != null) {
            autoClosableWebDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.valueOf(pageLoadTimeoutUnit));
        }
        if (scriptTimeout != null && scriptTimeoutUnit != null) {
            autoClosableWebDriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.valueOf(scriptTimeoutUnit));
        }
        if (windowHeight != null && windowWidth != null) {
            autoClosableWebDriver.manage().window().setSize(new Dimension(windowWidth, windowHeight));
        }

        return autoClosableWebDriver;
    }
}
