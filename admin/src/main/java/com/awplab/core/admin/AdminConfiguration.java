package com.awplab.core.admin;

/**
 * Created by andyphillips404 on 8/10/16.
 */
public final class AdminConfiguration {
    public static final String CONFIG_MANAGED_SERVICE_NAME = "com.awplab.core.admin";

    public static final String PROPERTY_URL_PATH = "com.awplab.core.urlPath";

    public static final String PROPERTY_REQUIRE_SECURE = "com.awplab.core.requireSecure";

    public static final String PROPERTY_LOGIN_LIMIT_TO_GROUPS = "com.awplab.core.admin.login.limitToGroups";

    public static final String PROPERTY_LOGIN_LIMIT_TO_ROLES = "com.awplab.core.admin.login.limitToRoles";

    public static final String PROPERTY_LOGIN_KARAF_REALM = "com.awplab.core.admin.login.karafRealm";


    public static final String PROPERTY_TITLE = "com.awplab.core.admin.ui.title";

    public static final String PROPERTY_CATEGORIES = "com.awplab.core.admin.ui.categories";


    private AdminConfiguration() {

    }
}
