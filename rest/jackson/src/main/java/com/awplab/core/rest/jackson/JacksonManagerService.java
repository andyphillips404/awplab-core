package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/19/15.
 */
public interface JacksonManagerService {


    void registerModulesWithObjectMapper(ObjectMapper objectMapper);

    Set<JacksonModulesService> getModulesProviders();

    Set<JacksonJaxrsService> getJaxrsProviders();

    void updateChange();

    static Set<String> getClassNames(Bundle bundle) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null)
            return Collections.emptySet();
        Collection<String> resources = bundleWiring.listResources("/", "*.class", BundleWiring.LISTRESOURCES_RECURSE);
        Set<String> classNamesOfCurrentBundle = new HashSet<>();
        for (String resource : resources) {
            URL localResource = bundle.getEntry(resource);
            // Bundle.getEntry() returns null if the resource is not located in the specific bundle
            if (localResource != null) {
                String className = resource.replaceAll("/", ".").replaceAll("^(.*?)(\\.class)$", "$1");
                classNamesOfCurrentBundle.add(className);
            }
        }

        return classNamesOfCurrentBundle;
    }

    static JacksonManagerService getProvider() {
        BundleContext bundleContext = FrameworkUtil.getBundle(JacksonManagerService.class).getBundleContext();
        ServiceReference ref = bundleContext.getServiceReference(JacksonManagerService.class.getName());
        if (ref != null) {
            return (JacksonManagerService)bundleContext.getService(ref);
        }

        return null;



    }

}
