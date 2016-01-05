package com.awplab.core.rest.service;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/22/15.
 */
public interface RestManagerService {


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
        reloadAlias(RestService.GLOBAL_ALIAS);
    }

    void reloadAlias(String alias);
}
