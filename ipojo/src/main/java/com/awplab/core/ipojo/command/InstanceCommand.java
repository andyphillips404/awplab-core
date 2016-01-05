package com.awplab.core.ipojo.command;


import com.awplab.core.ipojo.IPojoManagerService;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.fusesource.jansi.Ansi;

import java.io.PrintStream;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "ipojo", name="instance")
@Service
public class InstanceCommand implements Action {

    @Reference
    IPojoManagerService managerService;

    @Option(name = "-v",
            aliases = "--verbose",
            description = "When activated, display additional information about the given instance.",
            required = false)
    private boolean verbose = false;

    @Argument(index = 0,
            name = "instance-name",
            required = true,
            description = "A given Component Instance name")
    private String name;

    @Override
    public Object execute() throws Exception {

        AnsiPrintToolkit toolkit = new AnsiPrintToolkit();
        boolean found = false;
        for (Architecture arch : managerService.getArchitectures()) {
            InstanceDescription instance = arch.getInstanceDescription();
            if (name.equals(instance.getName())) {
                printInstanceDetails(toolkit, instance);
                found = true;
            }
        }

        PrintStream stream = System.out;
        if (!found) {
            // Use error stream
            stream = System.err;
            Ansi buffer = toolkit.getBuffer();

            // Creates an error message
            buffer.a(" [");
            toolkit.red("ERROR");
            buffer.a("] ");

            buffer.a("Instance '");
            toolkit.italic(name);
            buffer.a("' was not found.\n");
        }

        // Flush buffer's content
        stream.println(toolkit.getBuffer().toString());

        return null;
    }

    private String getStateName(int state) {
        switch (state) {
            case ComponentInstance.VALID:
                return "VALID";
            case ComponentInstance.INVALID:
                return "INVALID";
            case ComponentInstance.STOPPED:
                return "STOPPED";
            case ComponentInstance.DISPOSED:
                return "DISPOSED";
        }
        // Should not happen (no other known component instances states)
        return "";
    }

    private Ansi.Color getStateColor(int state) {
        switch (state) {
            case ComponentInstance.VALID:
                return Ansi.Color.GREEN;
            case ComponentInstance.INVALID:
                return Ansi.Color.RED;
            case ComponentInstance.STOPPED:
                return Ansi.Color.YELLOW;
            case ComponentInstance.DISPOSED:
                return Ansi.Color.BLUE;
        }
        // Should not happen (no other known component instances states)
        return Ansi.Color.DEFAULT;
    }

    private void printInstanceDetails(AnsiPrintToolkit toolkit,
                                      InstanceDescription instance) {

        Ansi buffer = toolkit.getBuffer();
        int state = instance.getState();

        //toolkit.eol();
        toolkit.bold(instance.getName());

        // Print status in the first column
        buffer.a(" [");
        buffer.fg(getStateColor(state));
        buffer.a(getStateName(state));
        buffer.fg(Ansi.Color.DEFAULT);
        buffer.a("]");

        toolkit.eol();

        toolkit.printElement(0, instance.getDescription());

    }

}
