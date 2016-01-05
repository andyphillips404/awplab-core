package com.awplab.core.rest.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/22/15.
 */
public interface RestService {

    String DEFAULT_ALIAS = "/";
    String GLOBAL_ALIAS = "GLOBAL";

    default String getAlias() {
        return DEFAULT_ALIAS;
    }

    default Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

    default Set<Object> getSingletons() {
        if (getAlias().equals(GLOBAL_ALIAS)) {
            return Collections.emptySet();
        }
        return Collections.singleton(this);
    }

}
