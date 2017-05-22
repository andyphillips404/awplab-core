package com.awplab.core.admin.provider;

import com.awplab.core.admin.AdminConfiguration;
import com.awplab.core.admin.AdminProvider;
import com.awplab.core.admin.AdminUI;
import com.awplab.core.admin.events.AdminEventData;
import com.awplab.core.admin.events.AdminEventTopics;
import com.awplab.core.common.EventAdminHelper;
import com.awplab.core.vaadin.service.BasicAuthRequired;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.ui.UI;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/12/16.
 */
@Component(immediate = true, managedservice = AdminConfiguration.CONFIG_MANAGED_SERVICE_NAME)
@Instantiate
@Provides(specifications = {VaadinProvider.class})
public class AdminVaadinProvider implements VaadinProvider, BasicAuthRequired {

    @ServiceController
    boolean serviceController;

    @Property(name = AdminConfiguration.PROPERTY_URL_PATH)
    private void updatePath(String path) {
        this.path = path;
        serviceController = false;
        serviceController = true;
    }

    private String path = "/admin";

    @Property(name = AdminConfiguration.PROPERTY_REQUIRE_SECURE, value = "true")
    private boolean requireSecure;

    @Property(name = AdminConfiguration.PROPERTY_LOGIN_LIMIT_TO_ROLES)
    private String[] loginLimitToRoles;

    @Property(name = AdminConfiguration.PROPERTY_LOGIN_KARAF_REALM, value = "karaf")
    private String karafRealm;

    @Property(name = AdminConfiguration.PROPERTY_LOGIN_LIMIT_TO_GROUPS)
    private String[] loginLimitToGroups;

    @Property(name = AdminConfiguration.PROPERTY_TITLE, value = "Admin Portal")
    private String title;

    @Property(name = AdminConfiguration.PROPERTY_CATEGORIES)
    private String[] categories;

    public String getTitle() {
        return title;
    }

    public String[] getCategories() {
        return categories;
    }

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

        if (loginLimitToRoles != null && loginLimitToRoles.length > 0) {
            if (!BasicAuthRequired.isUserInRole(subject, loginLimitToRoles)) return false;
        }
        if (loginLimitToGroups != null && loginLimitToGroups.length > 0) {
            if (!BasicAuthRequired.isUserInGroup(subject, loginLimitToGroups)) return false;
        }

        return true;
    }


    @Validate
    private  void started() {
        EventAdminHelper.postEvent(AdminEventTopics.ADMIN_PROVIDER_STARTED);
    }

    @Invalidate
    private  void stop() {
        EventAdminHelper.postEvent(AdminEventTopics.ADMIN_PROVIDER_STOPED);
    }


    private Set<AdminProvider> providers = Collections.synchronizedSet(new HashSet<>());

    @Bind(optional = true, aggregate = true)
    private void bindAdminProvider(AdminProvider adminProvider) {

        if (providers.stream().anyMatch(adminProvider2 -> adminProvider2.getName().equals(adminProvider.getName()))) {
            LoggerFactory.getLogger(AdminUI.class).error("Unable to add provider, duplicate name: " + adminProvider.getName());
            return;
        }

        providers.add(adminProvider);

        EventAdminHelper.postEvent(AdminEventTopics.PROVIDER_ADDED, AdminEventData.ADMIN_PROVIDER, adminProvider);


    }

    @Unbind(optional = true, aggregate = true)
    private void unbindAdminProvider(AdminProvider adminProvider) {
        if (providers.remove(adminProvider)) {
            EventAdminHelper.postEvent(AdminEventTopics.PROVIDER_REMOVED, AdminEventData.ADMIN_PROVIDER, adminProvider);
        }
    }


    @Override
    public Class<? extends UI> getUIClass() {
        return AdminUI.class;
    }

    public Set<AdminProvider> getProviders() {
        return Collections.unmodifiableSet(providers);
    }
}
