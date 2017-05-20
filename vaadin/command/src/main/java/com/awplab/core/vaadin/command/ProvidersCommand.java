package com.awplab.core.vaadin.command;


import com.awplab.core.vaadin.service.VaadinManager;
import com.awplab.core.vaadin.service.VaadinProvider;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "vaadin", name="providers")
@Service
public class ProvidersCommand implements Action {



    @Reference
    private VaadinManager vaadinManager;

    @Override
    public Object execute() throws Exception {

        ShellTable table = new ShellTable();
        table.column("Path");
        table.column("Bundle Id");
        table.column("Bundle");
        table.column("UI Class");
        table.column("Production Mode");
        //table.column("Bundle Id");
        //table.column("State");

        for (VaadinProvider vaadinProvider : vaadinManager.getProviders()) {
            Bundle bundle = FrameworkUtil.getBundle(vaadinProvider.getClass());
            table.addRow().addContent(vaadinProvider.getPath(), bundle.getBundleId(), bundle.getSymbolicName(), vaadinProvider.getUIClass().getSimpleName(), vaadinProvider.productionMode());
        }

        table.print(System.out);

        return null;
    }

}
