package com.awplab.core.ipojo.command;


import com.awplab.core.ipojo.IPojoManagerService;
import org.apache.felix.ipojo.Factory;
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
@Command(scope = "ipojo", name="factory")
@Service
public class FactoryCommand implements Action {

    @Reference
    IPojoManagerService managerService;

    @Option(name = "-v",
            aliases = "--verbose",
            description = "When activated, display additional Factory information.")
    private boolean verbose = false;

    @Argument(name = "factory-name",
            required = true,
            description = "A given Component Factory name")
    private String name;

    @Override
    public Object execute() throws Exception {

        Ansi buffer = Ansi.ansi();
        boolean found = false;
        for (Factory factory : managerService.getFactories()) {

            if (name.equals(factory.getName())) {
                printFactoryDetails(buffer, factory);
                found = true;
            }
        }

        PrintStream stream = System.out;
        if (!found) {
            // Use error stream
            stream = System.err;

            // Creates an error message
            buffer.a(" [");
            buffer.a(Ansi.Color.RED);
            buffer.a("ERROR");
            buffer.reset();
            buffer.a("] ");

            buffer.a("Factory '");
            buffer.a(Ansi.Attribute.ITALIC);
            buffer.a(name);
            buffer.a(Ansi.Attribute.ITALIC_OFF);
            buffer.a("' was not found.\n");
        }

        // Flush buffer's content
        stream.println(buffer.toString());

        return null;
    }

    private void printFactoryDetails(Ansi buffer, Factory factory) {

        String status = "INVALID";
        Ansi.Color color = Ansi.Color.RED;

        // Check Factory state
        if (factory.getState() == Factory.VALID) {
            status = "VALID";
            color = Ansi.Color.GREEN;
        }

        // Print factory name first
        buffer.a(factory.getName());
        buffer.a(" ");

        // Then its status
        buffer.a("[");
        buffer.fg(color);
        buffer.a(status);
        buffer.reset();
        buffer.a("]\n");

        // Finally display the factory description

        AnsiPrintToolkit toolkit = new AnsiPrintToolkit(buffer);
        toolkit.printElement(0, factory.getDescription());

    }

}
