package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.Module;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/19/15.
 */
@Component(name = JacksonModulesProvider.CONFIG_FACTORY_NAME, immediate = true)
@Provides(specifications = {JacksonModulesService.class})
public class JacksonModulesProvider implements JacksonModulesService, BundleListener {

    public static final String CONFIG_FACTORY_NAME = "com.awplab.core.rest.jackson.module";
    public static final String PROPERTY_MODULE_CLASSES = "com.awplab.core.rest.jackson.module.classes";

    @Property(name = PROPERTY_MODULE_CLASSES)
    private String[] moduleClassNames;

    @Updated
    private void update() {
        updateAfterChange();
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
        updateAfterChange();
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



    private void updateAfterChange() {
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