package com.awplab.core.selenium.command;


import com.awplab.core.common.TemporaryFile;
import com.awplab.core.selenium.service.AutoClosableWebDriver;
import com.awplab.core.selenium.service.SeleniumService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.openqa.selenium.OutputType;

import java.io.File;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "selenium", name="get-screenshot")
@Service
public class GetScreenshot implements Action {

    @Reference
    SeleniumService seleniumService = null;

    @Argument(index = 0, name = "url", description = "Url to get", required = true)
    private String url;

    @Override
    public Object execute() throws Exception {

        try (AutoClosableWebDriver webDriver = seleniumService.getWebDriver()) {
            webDriver.get(url);
            TemporaryFile file = webDriver.getScreenshot();
            System.out.println("Saved: " + file.toString());

        }

        return null;
    }

}
