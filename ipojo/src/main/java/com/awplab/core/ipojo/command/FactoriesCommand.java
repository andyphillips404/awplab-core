package com.awplab.core.ipojo.command;


import com.awplab.core.ipojo.IPojoManagerService;
import org.apache.felix.ipojo.Factory;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.fusesource.jansi.Ansi;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "ipojo", name="factories")
@Service
public class FactoriesCommand implements Action {

    @Reference
    IPojoManagerService managerService;

    @Override
    public Object execute() throws Exception {

        ShellTable table = new ShellTable();
        table.column("Name");
        table.column("Version");
        table.column("Bundle Id");
        table.column("State");

        for (Factory factory : managerService.getFactories()) {
            table.addRow().addContent(factory.getName(), factory.getVersion(), factory.getBundleContext().getBundle().getBundleId(), (factory.getState() == Factory.VALID ? Ansi.ansi().fg(Ansi.Color.GREEN).a("VALID").reset().toString() : Ansi.ansi().fg(Ansi.Color.GREEN).a((factory.getState() == Factory.INVALID ? "INVALID" : "UNKNOWN")).reset().toString()));
        }

        table.print(System.out);

        return null;
    }

}
