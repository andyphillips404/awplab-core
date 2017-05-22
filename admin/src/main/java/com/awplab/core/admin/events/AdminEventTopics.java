package com.awplab.core.admin.events;

/**
 * Created by andyphillips404 on 3/8/15.
 */
public final class AdminEventTopics {
    private AdminEventTopics() {
    }

    public static final String BASE = "com/awplab/core/admin/events";

    public static final String ANY = BASE + "/*";

    public static final String ADMIN_PROVIDER_STARTED = BASE + "/ADMIN_PROVIDER_STARTED";
    public static final String ADMIN_PROVIDER_STOPED = BASE + "/ADMIN_PROVIDER_STOPED";

    public static final String PROVIDER_ADDED = BASE + "/PROVIDER_ADDED";
    public static final String PROVIDER_REMOVED = BASE + "/PROVIDER_REMOVED";


    public static final String UPDATE_MENU_REQUESTED = BASE + "/UPDATE_MENU_REQUESTED";


}
