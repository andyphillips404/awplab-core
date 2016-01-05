package com.awplab.core.rest.jackson.manager;

import com.awplab.core.rest.jackson.JacksonJaxrsService;
import com.awplab.core.rest.jackson.JacksonManagerService;
import com.awplab.core.rest.jackson.JacksonModulesService;
import com.awplab.core.rest.service.RestManagerService;
import com.awplab.core.rest.service.RestService;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/19/15.
 */
@Provides(specifications = {JacksonManagerService.class, RestService.class})
@Component(immediate = true, publicFactory = false)
@Instantiate
public class JacksonManagerProvider implements JacksonManagerService, RestService {



    @Requires
    RestManagerService restManagerService;

    private Set<JacksonModulesService> modulesProviders = Collections.synchronizedSet(new HashSet<>());

    private Set<JacksonJaxrsService> jaxrsProviders = Collections.synchronizedSet(new HashSet<>());


    @Bind(aggregate=true, optional = true)
    private synchronized void bindJacksonModuleService(JacksonModulesService jacksonModulesProvider) {
        modulesProviders.add(jacksonModulesProvider);
        updateChange();
    }

    @Unbind
    private synchronized void unbindJacksonModuleService(JacksonModulesService jacksonModulesProvider) {
        modulesProviders.remove(jacksonModulesProvider);
        updateChange();
    }

    @Bind(aggregate=true, optional = true)
    private synchronized void bindJacksonJaxrsService(JacksonJaxrsService jacksonJaxrsProvider) {
        jaxrsProviders.add(jacksonJaxrsProvider);
        updateChange();
    }

    @Unbind
    private synchronized void unbindJacksonJaxrsService(JacksonJaxrsService jacksonJaxrsProvider) {
        jaxrsProviders.remove(jacksonJaxrsProvider);
        updateChange();
    }

    @Override
    public synchronized Set<JacksonModulesService> getModulesProviders() {
        return new HashSet<>(modulesProviders);
    }

    @Override
    public synchronized Set<JacksonJaxrsService> getJaxrsProviders() {
        return new HashSet<>(jaxrsProviders);
    }

    @Override
    public void updateChange() {
        restManagerService.reloadAliases();
    }

    @Override
    public synchronized void registerModulesWithObjectMapper(ObjectMapper objectMapper) {
        Logger logger = LoggerFactory.getLogger(JacksonManagerProvider.class);
        for (JacksonModulesService jacksonModulesProvider : modulesProviders) {
            try {
                for (Class<Module> moduleClass : jacksonModulesProvider.getModuleClasses()) {
                    try {
                        objectMapper.registerModule(moduleClass.newInstance());
                    } catch (Exception ex) {
                        logger.error("Exception trying to create module: " + moduleClass.getName(), ex);
                    }
                }
            }
            catch (Exception ex) {
                logger.error("Exception getting module classes from module provider", ex);
            }
        }
    }

    @Override
    public String getAlias() {
        return RestService.GLOBAL_ALIAS;
    }

    @Override
    public synchronized Set<Object> getSingletons() {

        Logger logger = LoggerFactory.getLogger(JacksonManagerProvider.class);

        Set<Object> singletons = new HashSet<>();

        for (JacksonJaxrsService jacksonJaxrsProvider : jaxrsProviders) {
            try {
                ObjectMapper objectMapper = (ObjectMapper)jacksonJaxrsProvider.getMapperClass().newInstance();
                registerModulesWithObjectMapper(objectMapper);
                Annotations[] annotations = {Annotations.JACKSON, Annotations.JAXB};
                singletons.add(jacksonJaxrsProvider.getProviderClass().getConstructor(objectMapper.getClass(), Annotations[].class).newInstance(objectMapper, annotations));
            }
            catch (Exception ex) {
                logger.error("Exception creating JAXRS provider", ex);
            }
        }

        return singletons;
    }



}
