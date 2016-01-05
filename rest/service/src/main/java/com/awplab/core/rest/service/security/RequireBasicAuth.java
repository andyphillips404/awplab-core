package com.awplab.core.rest.service.security;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by andyphillips404 on 3/26/15.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface RequireBasicAuth {
    boolean requiresSecure() default true;
    String karafRealm() default "karaf";
    String[] limitToGroups() default {};
    String[] limitToRoles() default {};
    String httpRealm() default "This is a secure area.  Please enter your username and password.";
}
