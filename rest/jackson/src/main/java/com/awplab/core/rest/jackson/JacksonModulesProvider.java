package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.Module;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleWiring;

import java.net.URL;
import java.util.*;

/**
 * Created by andyphillips404 on 12/19/15.
 */
@Component(name = "com.awplab.core.rest.jackson.module", immediate = true)
@Provides(specifications = {JacksonModulesService.class})
public class JacksonModulesProvider implements JacksonModulesService, BundleListener {

    @Property(name = "com.awplab.core.rest.jackson.module.classes")
    private String[] moduleClassNames;

    @Updated
    private void update() {
        updateAfterChaange();
    }

    @Requires
    JacksonManagerService jacksonManagerService;

    @ServiceController(value = false, specification = JacksonModulesService.class)
    private boolean jacksonServiceController;


    @Context
    private BundleContext context;

    @Validate
    public void start() {
        context.addBundleListener(this);
        updateAfterChaange();
    }

    @Invalidate
    public void stop() {
        context.removeBundleListener(this);
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (moduleClassNames == null) {
            return;
        }


        Set<String> classNamesOfCurrentBundle = JacksonManagerService.getClassNames(bundleEvent.getBundle());

        boolean found = false;
        for (String className : moduleClassNames) {
            if (classNamesOfCurrentBundle.contains(className)) {
                found = true;
                break;
            }
        }
        if (found) {
            if (bundleEvent.getType() == BundleEvent.STARTED && isValid()) {
                jacksonServiceController = true;
            }
            if (bundleEvent.getType() == BundleEvent.STOPPED) {
                jacksonServiceController = false;
            }
        }
    }



    private void updateAfterChaange() {
        if (isValid()) {
            if (!jacksonServiceController) jacksonServiceController = true;
            else {
                jacksonManagerService.updateChange();
            }
        }
        else {
            if (jacksonServiceController) jacksonServiceController = false;
        }

    }
    public boolean isValid() {
        try {
            return getModuleClasses().size() > 0;
        }
        catch (ClassNotFoundException ignored) {
            return true;
        }
    }

    @Override
    public Set<Class<Module>> getModuleClasses() throws ClassNotFoundException {
        HashSet<Class<Module>> classes = new HashSet<>();
        for (String className : moduleClassNames) {
            classes.add((Class<Module>)Class.forName(className));
        }

        return classes;
    }



    public String[] getModuleClassNames() {
        return moduleClassNames;
    }


}