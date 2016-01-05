package com.awplab.core.rest.service.security;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;

import javax.security.auth.Subject;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Created by andyphillips404 on 3/26/15.
 */
public class KarafSecurityContext implements SecurityContext {

    private Subject subject;
    private boolean isSecure;
    private String authentationScheme;

    public KarafSecurityContext(Subject subject, boolean isSecure, String authentationScheme) {
        this.subject = subject;
        this.isSecure = isSecure;
        this.authentationScheme = authentationScheme;
    }

    public boolean isUserInGroup(String g) {
        for (GroupPrincipal principal : subject.getPrincipals(GroupPrincipal.class)) {
            if (principal.getName().equals(g)) return true;
        }
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return subject.getPrincipals(UserPrincipal.class).iterator().next();
    }

    @Override
    public boolean isUserInRole(String s) {
        for (RolePrincipal principal : subject.getPrincipals(RolePrincipal.class)) {
            if (principal.getName().equals(s)) return true;
        }
        return false;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return authentationScheme;
    }
}
