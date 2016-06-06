package com.awplab.core.rest.command;


import com.awplab.core.rest.service.RestApplication;
import com.awplab.core.rest.service.RestManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.fusesource.jansi.Ansi;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "rest", name="applications")
@Service
public class ApplicationsCommand implements Action {

    @Reference
    RestManager managerService;

    @Override
    public Object execute() throws Exception {

        StringBuilder logStringBuilder = new StringBuilder();
        for (String aliases : managerService.getAliases()) {
            logStringBuilder.append(Ansi.ansi().bold().fgBright(Ansi.Color.WHITE).toString());
            logStringBuilder.append("Rest Applicaiton for Alias: ");
            logStringBuilder.append(aliases);
            logStringBuilder.append("\n");
            logStringBuilder.append(Ansi.ansi().reset().toString());
            logStringBuilder.append("Application Classes:\n");
            RestApplication restApplication = managerService.getApplication(aliases);
            for (Class<?> clazz : restApplication.getClasses()) {
                logStringBuilder.append(clazz.getName());
                logStringBuilder.append("\n");
            }
            logStringBuilder.append("Singletons:\n");
            for (Object singleton :restApplication.getSingletons()) {
                logStringBuilder.append(singleton.getClass().getName());
                logStringBuilder.append("\n");
            }
        }
        System.out.println(logStringBuilder.toString());
        return null;
    }

}
