package com.awplab.core.rest.service;


import com.awplab.core.rest.service.security.BasicAuthKarafSecurityDynamicFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/23/15.
 */
public class RestApplication extends Application {


    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    public RestApplication(String alias, Set<RestService> providers) {

        classes.add(MultiPartFeature.class);
        classes.add(BasicAuthKarafSecurityDynamicFeature.class);

        for (RestService provider : providers) {
            singletons.addAll(provider.getSingletons());
            classes.addAll(provider.getClasses());
        }

        StringBuilder logStringBuilder = new StringBuilder();
        logStringBuilder.append("Creating REST application for alias: ");
        logStringBuilder.append(alias);
        logStringBuilder.append("\n");
        logStringBuilder.append("Application Classes:\n");
        for (Class<?> clazz : classes) {
            logStringBuilder.append(clazz.getName());
            logStringBuilder.append("\n");
        }
        logStringBuilder.append("Singletons:\n");
        for (Object singleton : singletons) {
            logStringBuilder.append(singleton.getClass().getName());
            logStringBuilder.append("\n");
        }

        LoggerFactory.getLogger(RestApplication.class).info(logStringBuilder.toString().trim());
    }


    @Override
    public Set<Class<?>> getClasses() {
        return Collections.unmodifiableSet(classes);
    }

    @Override
    public Set<Object> getSingletons() {
        return Collections.unmodifiableSet(singletons);
    }
}
