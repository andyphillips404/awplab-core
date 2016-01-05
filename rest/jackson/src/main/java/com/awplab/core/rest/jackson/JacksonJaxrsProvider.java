package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.base.ProviderBase;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleWiring;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/19/15.
 */

@Component(name = "com.awplab.core.rest.jackson.jaxrs", immediate = true)
@Provides(specifications = {JacksonJaxrsService.class})
public class JacksonJaxrsProvider implements JacksonJaxrsService, BundleListener {

    @Property(name = "com.awplab.core.rest.jackson.jaxrs.provider", mandatory = true)
    private String providerBaseClassName;

    @Property(name = "com.awplab.core.rest.jackson.jaxrs.mapper", mandatory = true)
    private String objectMapperClassName;

    @Updated
    private void updated() {
        updateAfterChaange();
    }

    @Requires
    JacksonManagerService jacksonManagerService;

    @ServiceController(value = false, specification = JacksonJaxrsService.class)
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
        if (providerBaseClassName == null || objectMapperClassName == null) {
            return;
        }

        Set<String> classNamesOfCurrentBundle = JacksonManagerService.getClassNames(bundleEvent.getBundle());

        if (classNamesOfCurrentBundle.contains(objectMapperClassName) || classNamesOfCurrentBundle.contains(providerBaseClassName)) {
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
            if (!jacksonServiceController)
                jacksonServiceController = true;
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
            return (providerBaseClassName != null && objectMapperClassName != null && getProviderClass() != null && getMapperClass() != null);
        }
        catch (ClassNotFoundException ignored) {
            return false;
        }

    }

    @Override
    public Class<ProviderBase> getProviderClass() throws ClassNotFoundException {
        return (Class<ProviderBase>)Class.forName(providerBaseClassName);
    }

    @Override
    public Class<ObjectMapper> getMapperClass() throws ClassNotFoundException {
        return (Class<ObjectMapper>)Class.forName(objectMapperClassName);
    }

    public String getProviderBaseClassName() {
        return providerBaseClassName;
    }

    
    public String getObjectMapperClassName() {
        return objectMapperClassName;
    }

}

