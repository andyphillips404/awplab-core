package com.awplab.core.vaadin.service;

import com.vaadin.server.VaadinRequest;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Optional;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public interface BasicAuthRequired {

    String SUBJECT_REQUEST_ATTRIBUTE = "com.awplab.core.vaadin.subject";

    default boolean requireSecure() {
        return true;
    }

    default String karafRealm() {
        return "karaf";
    }

    default String httpRealm() {
        return "Please enter your username and password";
    }

    boolean allowSubject(Subject subject);

    static boolean isUserInGroup(Subject subject, String group) {
        for (GroupPrincipal principal : subject.getPrincipals(GroupPrincipal.class)) {
            if (principal.getName().equals(group)) return true;
        }
        return false;
    }

    static boolean isUserInRole(Subject subject, String role) {
        for (RolePrincipal principal : subject.getPrincipals(RolePrincipal.class)) {
            if (principal.getName().equals(subject)) return true;
        }
        return false;
    }

    static boolean isUserInGroup(Subject subject, String[] groups) {
        for (String group : groups) {
            if (isUserInGroup(subject, group)) return true;
        }
        return false;
    }

    static boolean isUserInRole(Subject subject, String[] roles) {
        for (String role : roles) {
            if (isUserInRole(subject, role)) return true;
        }
        return false;
    }

    static Optional<Subject> getSubject(VaadinRequest request) {
        return Optional.ofNullable((Subject)request.getAttribute(BasicAuthRequired.SUBJECT_REQUEST_ATTRIBUTE));

    }
}
