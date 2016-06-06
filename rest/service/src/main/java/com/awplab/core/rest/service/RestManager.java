package com.awplab.core.rest.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Set;

/**
 * Created by andyphillips404 on 2/22/15.
 */
public interface RestManager {

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
        reloadAlias(RestManager.GLOBAL_ALIAS);
    }

    void reloadAlias(String alias);

    static RestManager getProvider() {
        BundleContext bundleContext = FrameworkUtil.getBundle(RestManager.class).getBundleContext();
        ServiceReference ref = bundleContext.getServiceReference(RestManager.class.getName());
        if (ref != null) {
            return (RestManager)bundleContext.getService(ref);
        }

        return null;
    }


}
