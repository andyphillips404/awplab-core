package com.awplab.core.rest.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/22/15.
 */
public interface RestService {

    default String getAlias() {
        return RestManagerService.DEFAULT_ALIAS;
    }

    default Set<Class<?>> getClasses() { return Collections.emptySet(); }

    default Set<Object> getSingletons() {return Collections.singleton(this); }

}
