package com.awplab.core.rest.jackson.command;


import com.awplab.core.rest.jackson.JacksonJaxrsService;
import com.awplab.core.rest.jackson.JacksonManagerService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "jackson", name="providers")
@Service
public class ProvidersCommand implements Action {

    @Reference
    JacksonManagerService managerService;

    @Override
    public Object execute() throws Exception {

        ShellTable table = new ShellTable();
        table.column("Provider");
        table.column("Mapper");


        for (JacksonJaxrsService provider : managerService.getJaxrsProviders()) {
            table.addRow().addContent(provider.getProviderClass().getName(), provider.getMapperClass().getName());
        }
        table.print(System.out);

        return null;
    }

}
