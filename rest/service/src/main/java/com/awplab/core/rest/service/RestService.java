package com.awplab.core.rest.service;

import java.util.Collections;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/22/15.
 */
public interface RestService {

    String DEFAULT_ALIAS = "/service";

    default String getAlias() {
        return DEFAULT_ALIAS;
    }

    default Set<Class<?>> getClasses(String alias) { return Collections.emptySet(); }

    default Set<Object> getSingletons(String alias) {
        if (!getAlias().equals(RestManager.GLOBAL_ALIAS)) return Collections.singleton(this);
        else return Collections.emptySet();
    }

}
