package com.awplab.core.rest.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/22/15.
 */
public interface RestManagerService {

    String ROOT_ALIAS = "/";
    String GLOBAL_ALIAS = "GLOBAL";


    void registerProvider(RestService restProvider);

    void unregisterProvider(RestService restProvider);

    void registerClass(String alias, Class<?> clazz);

    void registerSingleton(String alias, Object singleton);

    void unregisterClass(String alias, Class<?> clazz);

    void unregisterSingleton(String alias, Object singleton);

    Set<RestService> getProviders();

    Set<String> getAliases();

    RestApplication getApplication(String alias);

    default void reloadAliases() {
        reloadAlias(RestManagerService.GLOBAL_ALIAS);
    }

    void reloadAlias(String alias);

    static RestManagerService getProvider() {
        BundleContext bundleContext = FrameworkUtil.getBundle(RestManagerService.class).getBundleContext();
        ServiceReference ref = bundleContext.getServiceReference(RestManagerService.class.getName());
        if (ref != null) {
            return (RestManagerService)bundleContext.getService(ref);
        }

        return null;
    }


}
