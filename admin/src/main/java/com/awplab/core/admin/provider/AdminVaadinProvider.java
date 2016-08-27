package com.awplab.core.admin.provider;

import com.awplab.core.admin.AdminServletConfiguration;
import com.awplab.core.vaadin.service.BasicAuthRequired;
import com.awplab.core.vaadin.service.IPOJOVaadinUIProvider;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.awplab.core.vaadin.service.VaadinUIProvider;
import org.apache.felix.ipojo.annotations.*;

import javax.security.auth.Subject;

/**
 * Created by andyphillips404 on 8/12/16.
 */
@Component(immediate = true, managedservice = AdminServletConfiguration.CONFIG_MANAGED_SERVICE_NAME)
@Instantiate
@Provides(specifications = VaadinProvider.class)
public class AdminVaadinProvider implements VaadinProvider, BasicAuthRequired {

    @ServiceController
    boolean serviceController;

    @Property(name = AdminServletConfiguration.PROPERTY_URL_PATH)
    private void updatePath(String path) {
        this.path = path;
        serviceController = false;
        serviceController = true;
    }

    private String path = "/admin";

    @Property(name = AdminServletConfiguration.PROPERTY_LOGIN_ENABLED, value = "true")
    private boolean loginEnabled;

    @Property(name = AdminServletConfiguration.PROPERTY_REQUIRE_SECURE, value = "true")
    private boolean requireSecure;

    @Property(name = AdminServletConfiguration.PROPERTY_LOGIN_LIMIT_TO_ROLES)
    private String[] loginLimitToRoles;

    @Property(name = AdminServletConfiguration.PROPERTY_LOGIN_KARAF_REALM, value = "karaf")
    private String karafRealm;

    @Property(name = AdminServletConfiguration.PROPERTY_LOGIN_LIMIT_TO_GROUPS)
    private String[] loginLimitToGroups;

    @Override
    public String karafRealm() {
        return karafRealm;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean requireSecure() {
        return requireSecure;
    }

    @Override
    public boolean allowSubject(Subject subject) {

        if (loginEnabled) {
            if (loginLimitToRoles != null && loginLimitToRoles.length > 0) {
                if (!BasicAuthRequired.isUserInRole(subject, loginLimitToRoles)) return false;
            }
            if (loginLimitToGroups != null && loginLimitToGroups.length > 0) {
                if (!BasicAuthRequired.isUserInGroup(subject, loginLimitToGroups)) return false;
            }
        }
        return true;
    }

    @Override
    public VaadinUIProvider createUIProvider() {
        return new IPOJOVaadinUIProvider(AdminUI.class);
    }
}
