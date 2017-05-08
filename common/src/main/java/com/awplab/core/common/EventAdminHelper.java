package com.awplab.core.common;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 5/8/17.
 */
public class EventAdminHelper {
    private EventAdminHelper() {
    }

    public static void postEvent(String topic) {
        postEvent(topic, new HashMap<>());
    }

    public static void postEvent(String topic, Object... keysAndData) {
        postEvent(topic, MapUtils.createDataMap(keysAndData));
    }

    public static void postEvent(String topic, Map<String, Object> data) {
        BundleContext bundleContext = FrameworkUtil.getBundle(EventAdminHelper.class).getBundleContext();

        ServiceReference ref = bundleContext.getServiceReference(EventAdmin.class.getName());
        if (ref != null)
        {
            EventAdmin eventAdmin = (EventAdmin) bundleContext.getService(ref);
            HashMap<String, Object> eventData = new HashMap<>(data);
            eventAdmin.postEvent(new Event(topic, eventData));
        }
    }
}
