package com.awplab.core.selenium.command;


import com.awplab.core.selenium.service.AutoClosableWebDriver;
import com.awplab.core.selenium.service.SeleniumService;
import com.awplab.core.selenium.service.provider.SeleniumProvider;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "selenium", name="configure")
@Service
public class Configure implements Action {

    @Reference
    ConfigurationAdmin configurationAdmin;

    @Option(name = "--force-local", description = "Force traffic to local server")
    Boolean forceLocal;

    @Option(name = "--local-driver-class", description = "Local driver class")
    String localDriverClass;

    @Option(name = "--remote-url", description = "Remote selenium url, ex: http://192.168.99.100:32768/wd/hub")
    String remoteUrl;

    @Option(name = "--browser", description = "Desired capabilities browser")
    String browser;

    @Option(name = "--version", description = "Desired capabilities browser version")
    String browserVersion;

    @Option(name = "--platform", description = "Desired capabilities platform")
    String platform;

    @Option(name = "--implicitly-wait", description = "Implicitly wait time (seconds) for any DOM element to be available.  WebDriver default is zero.")
    Integer implicitlyWait;

    @Option(name = "--page-load", description = "Page load timeout (seconds)")
    Integer pageLoad;

    @Option(name = "--script-timeout", description = "Script timeout (seconds)")
    Integer scriptTimeout;

    @Option(name = "--window-width", description = "Window width")
    Integer windowWidth;

    @Option(name = "--window-height", description = "Window height")
    Integer windowHeight;

    @Option(name = "--wait-until-timeout", description = "Default wait until timeout (explicit wait) in AutoClosabelDriver")
    Integer waitUntilTimeout;


    private String url;

    @Override
    public Object execute() throws Exception {

        Configuration config = configurationAdmin.getConfiguration(SeleniumProvider.CONFIG_MANAGED_SERVICE_NAME);
        Dictionary props = config.getProperties();
        if (props == null) {
            props = new Hashtable();
        }

        if (forceLocal != null) props.put(SeleniumProvider.PROPERTY_FORCE_LOCAL, forceLocal);
        if (localDriverClass != null) props.put(SeleniumProvider.PROPERTY_LOCAL_DRIVER_CLASS, localDriverClass);
        if (remoteUrl != null) props.put(SeleniumProvider.PROPERTY_REMOTE_URL, remoteUrl);
        if (browser != null) props.put(SeleniumProvider.PROPERTY_REMOTE_DESIRED_BROWSER, browser);
        if (browserVersion != null) props.put(SeleniumProvider.PROPERTY_REMOTE_DESIRED_VERSION, browserVersion);
        if (platform != null) props.put(SeleniumProvider.PROPERTY_REMOTE_DESIRED_PLATFORM, platform);
        if (implicitlyWait != null) {
            props.put(SeleniumProvider.PROPERTY_IMPLICITLY_WAIT_TIME, implicitlyWait);
            props.put(SeleniumProvider.PROPERTY_IMPLICITLY_WAIT_TIME_UNIT, "SECONDS");
        }
        if (pageLoad != null) {
            props.put(SeleniumProvider.PROPERTY_PAGE_LOAD_TIMEOUT, pageLoad);
            props.put(SeleniumProvider.PROPERTY_PAGE_LOAD_TIMEOUT_UNIT, "SECONDS");
        }
        if (scriptTimeout != null) {
            props.put(SeleniumProvider.PROPERTY_SCRIPT_TIMEOUT, scriptTimeout);
            props.put(SeleniumProvider.PROPERTY_SCRIPT_TIMEOUT_UNIT, "SECONDS");
        }

        if (waitUntilTimeout != null) {
            props.put(SeleniumProvider.PROPERTY_WAIT_UNTIL_TIMEOUT, waitUntilTimeout);
            props.put(SeleniumProvider.PROPERTY_WAIT_UNTIL_TIMEOUT_UNIT, "SECONDS");
        }
        if (windowHeight != null) props.put(SeleniumProvider.PROPERTY_WINDOW_HEIGHT, windowHeight);
        if (windowWidth != null) props.put(SeleniumProvider.PROPERTY_WINDOW_WIDTH, windowWidth);

        config.update(props);

        return null;
    }

}
