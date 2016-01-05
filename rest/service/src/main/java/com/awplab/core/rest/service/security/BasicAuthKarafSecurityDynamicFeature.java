package com.awplab.core.rest.service.security;

import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * Created by andyphillips404 on 3/26/15.
 */
public class BasicAuthKarafSecurityDynamicFeature implements DynamicFeature {


    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        RequireBasicAuth requireBasicAuth = null;
        if (am.isAnnotationPresent(RequireBasicAuth.class)) {
            requireBasicAuth = am.getAnnotation(RequireBasicAuth.class);
        }
        else {
            requireBasicAuth = resourceInfo.getResourceClass().getAnnotation(RequireBasicAuth.class);
        }

        if (requireBasicAuth != null) {
            featureContext.register(new BasicAuthKarafSecurityRequestFilter(requireBasicAuth.limitToGroups(), requireBasicAuth.limitToRoles(), requireBasicAuth.karafRealm(), requireBasicAuth.requiresSecure(), requireBasicAuth.httpRealm()));
        }
    }



}
