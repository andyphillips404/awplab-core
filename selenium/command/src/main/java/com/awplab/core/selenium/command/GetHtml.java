package com.awplab.core.selenium.command;


import com.awplab.core.selenium.service.AutoClosableWebDriver;
import com.awplab.core.selenium.service.SeleniumService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "selenium", name="get-html")
@Service
public class GetHtml implements Action {

    @Reference
    SeleniumService seleniumService = null;

    @Argument(index = 0, name = "url", description = "Url to get", required = true)
    private String url;

    @Override
    public Object execute() throws Exception {

        try (AutoClosableWebDriver webDriver = seleniumService.getWebDriver()) {
            webDriver.get(url);
            System.out.println(webDriver.getPageSource());
        }

        return null;
    }

}
