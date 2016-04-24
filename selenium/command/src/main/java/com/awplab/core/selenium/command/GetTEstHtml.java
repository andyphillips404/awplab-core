package com.awplab.core.selenium.command;


import com.awplab.core.selenium.service.AutoClosableWebDriver;
import com.awplab.core.selenium.service.SeleniumService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "selenium", name="get-test-html")
@Service
public class GetTestHtml implements Action {

    @Reference
    SeleniumService seleniumService = null;

    @Argument(index = 0, name = "url", description = "Url to get", required = true)
    private String url;

    @Override
    public Object execute() throws Exception {

        FirefoxDriver firefoxDriver = null;
        try {
            firefoxDriver = new FirefoxDriver();
            firefoxDriver.get(url);
            System.out.println(firefoxDriver.getPageSource());
        }

        finally {
            if (firefoxDriver != null)             firefoxDriver.quit();

        }
        return null;

    }

}
