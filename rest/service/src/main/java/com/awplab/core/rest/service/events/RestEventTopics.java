package com.awplab.core.rest.service.events;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 3/8/15.
 */
public final class RestEventTopics {
    private RestEventTopics() {
    }

    public static final String BASE = "com/awplab/core/rest/events";

    public static final String MANAGER_STARTED = BASE + "/MANAGER_STARTED";
    public static final String MANAGER_STOPPED = BASE + "/MANAGER_STOPPED";

    public static final String ALIAS_STARTED = BASE + "/ALIAS_STARTED";
    public static final String ALIAS_RESTARTED = BASE + "/ALIAS_RESTARTED";
    public static final String ALIAS_STOPPED = BASE + "/ALIAS_STOPPED";

    public static final String PROVIDER_REGISTERED = BASE + "/PROVIDER_REGISTERED";
    public static final String PROVIDER_UNREGISTERED = BASE + "/PROVIDER_UNREGISTERED";


    public static void postEvent(String topic) {
        postEvent(null, topic, new HashMap<>());
    }

    public static void postEvent(String alias, String topic) {
        postEvent(alias, topic, new HashMap<>());
    }

    public static void postEvent(String alias, String topic, Map<String, Object> data) {
        BundleContext bundleContext = FrameworkUtil.getBundle(RestEventTopics.class).getBundleContext();

        ServiceReference ref = bundleContext.getServiceReference(EventAdmin.class.getName());
        if (ref != null)
        {
            EventAdmin eventAdmin = (EventAdmin) bundleContext.getService(ref);
            HashMap<String, Object> eventData = new HashMap<>(data);
            if (alias != null) eventData.put(RestEventData.ALIAS, alias);
            eventAdmin.postEvent(new Event(topic, eventData));
        }
    }

}
