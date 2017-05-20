package com.awplab.core.vaadin.service.events;

/**
 * Created by andyphillips404 on 3/8/15.
 */
public final class VaadinEventTopics {
    private VaadinEventTopics() {
    }

    public static final String BASE = "com/awplab/core/vaadin/events";

    public static final String ANY = BASE + "/*";

    public static final String PROVIDER_ADDED = BASE + "/PROVIDER_ADDED";
    public static final String PROVIDER_REMOVED = BASE + "/PROVIDER_REMOVED";

    public static final String MANAGER_STARTED = BASE + "/MANAGER_STARTED";
    public static final String MANAGER_STOPED = BASE + "/MANAGER_STOPED";



}
