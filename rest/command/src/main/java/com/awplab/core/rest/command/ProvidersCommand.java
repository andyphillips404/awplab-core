package com.awplab.core.rest.command;


import com.awplab.core.rest.service.RestManagerService;
import com.awplab.core.rest.service.RestService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "rest", name="providers")
@Service
public class ProvidersCommand implements Action {

    @Reference
    RestManagerService managerService;

    @Override
    public Object execute() throws Exception {

        ShellTable table = new ShellTable();
        table.column("Alias");
        table.column("Class");
        //table.column("Bundle Id");
        //table.column("State");

            for (RestService restProvider : managerService.getProviders()) {
                table.addRow().addContent(restProvider.getAlias(), restProvider.getClass().getName());
            }

        table.print(System.out);

        return null;
    }

}
