package com.awplab.core.mongodb.log.events;

import com.awplab.core.common.EventAdminHelper;
import com.awplab.core.mongodb.log.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.HashMap;

/**
 * Created by andyphillips404 on 3/8/15.
 */
public final class LogEventTopics {
    private LogEventTopics() {
    }

    public static final String BASE = "com/awplab/core/log/events";

    public static final String ANY = BASE + "/*";

    public static final String ENTRY_ADDED = BASE + "/ENTRY_ADDED";



    public static void postEntryAdded(Log log, String database, String collection) {
        EventAdminHelper.postEvent(ENTRY_ADDED, LogEventData.LOG_ENTRY, log, LogEventData.COLLECTION, collection, LogEventData.DATABASE, database);
    }

}
