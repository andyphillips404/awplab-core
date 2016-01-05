package com.awplab.core.rest.jackson.command;


import com.awplab.core.rest.jackson.JacksonManagerService;
import com.awplab.core.rest.jackson.JacksonModulesProvider;
import com.awplab.core.rest.jackson.JacksonModulesService;
import com.fasterxml.jackson.databind.Module;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "jackson", name="modules")
@Service
public class ModulesCommand implements Action {

    @Reference
    JacksonManagerService managerService;

    @Override
    public Object execute() throws Exception {

        for (JacksonModulesService provider : managerService.getModulesProviders()) {
            for (Class<Module> clazz : provider.getModuleClasses()) {
                System.out.println(clazz.getName());
            }
        }

        return null;
    }

}
