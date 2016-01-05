package com.awplab.core.ipojo.command;


import com.awplab.core.ipojo.IPojoManagerService;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "ipojo", name="instances")
@Service
public class InstancesCommand implements Action {

    @Reference
    IPojoManagerService managerService;

    @Option(name = "-v",
            aliases = "--verbose",
            description = "When activated, display DISPOSED instances as well.",
            required = false)
    private boolean verbose = false;

    @Override
    public Object execute() throws Exception {

        ShellTable table = new ShellTable();
        table.column("Name");
        table.column("Bundle Id");
        table.column("State");

        for (Architecture architecture : managerService.getArchitectures()) {
            InstanceDescription instanceDescription = architecture.getInstanceDescription();
            if (verbose || instanceDescription.getState() != ComponentInstance.DISPOSED) {
                table.addRow().addContent(instanceDescription.getName(), instanceDescription.getBundleId(), IPojoManagerService.instanceDescriptionState(instanceDescription.getState()));
            }
        }

        table.print(System.out);

        return null;
    }

}
