package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.Module;

import java.util.Set;

/**
 * Created by andyphillips404 on 12/20/15.
 */
public interface JacksonModulesService {
    Set<Class<Module>> getModuleClasses() throws ClassNotFoundException;
}
