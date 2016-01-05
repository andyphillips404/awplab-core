package com.awplab.core.ipojo;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.fusesource.jansi.Ansi;

/**
 * Created by andyphillips404 on 12/21/15.
 */
public interface IPojoManagerService {
    Architecture[] getArchitectures();

    HandlerFactory[] getHandlerFactories();

    Factory[] getFactories();

    InstanceDescription getInstanceDescription(Object instance);

    static String instanceDescriptionState(int state) {

        switch (state) {
            case ComponentInstance.VALID:
                return Ansi.ansi().fg(Ansi.Color.GREEN).a("VALID").reset().toString();
            case ComponentInstance.INVALID:
                return Ansi.ansi().fg(Ansi.Color.RED).a("INVALID").reset().toString();
            case ComponentInstance.STOPPED:
                return Ansi.ansi().fg(Ansi.Color.YELLOW).a("STOPPED").reset().toString();
            case ComponentInstance.DISPOSED:
                return Ansi.ansi().fg(Ansi.Color.BLUE).a("DISPOSED").reset().toString();
        }
        // Should not happen (no other known component instances states)
        return Ansi.ansi().fg(Ansi.Color.RED).a("UNKNOWN").toString();


    }
}
