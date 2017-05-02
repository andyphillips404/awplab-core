package com.awplab.core.mongodb.admin;

import com.awplab.core.admin.AdminProvider;
import com.awplab.core.admin.IPOJOAdminProvider;
import org.apache.felix.ipojo.annotations.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by andyphillips404 on 8/31/16.
 */
@Component(immediate = true, name = LogAdminProvider.CONFIG_FACTORY_NAME)
@Provides(specifications = {AdminProvider.class})
public class LogAdminProvider extends IPOJOAdminProvider<LogAdminViewProvider> {

    public static final String CONFIG_FACTORY_NAME = "com.awplab.core.mongodb.admin.log";

    public static final String PROPERTY_DATABASE = "com.awplab.core.mongodb.admin.log.database";

    public static final String PROPERTY_COLLECTION = "com.awplab.core.mongodb.admin.log.collection";

    @Property(name = PROPERTY_DATABASE, mandatory = true)
    private String database;

    @Property(name = PROPERTY_COLLECTION, mandatory = true)
    private String collection;

    @Override
    public Class<LogAdminViewProvider> getAdminViewProviderClass() {
        return LogAdminViewProvider.class;
    }

    @Override
    public void viewProviderCreated(LogAdminViewProvider viewProvider) {
        viewProvider.setConnection(database, collection);
    }

    @ServiceController(value = false, specification = AdminProvider.class)
    private boolean serviceController;

    Timer timer = new Timer();

    @Updated
    public void updated() {
        if (serviceController) serviceController = false;

        timer.purge();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (database != null && collection != null && !serviceController) {
                    serviceController = true;
                }
            }
        }, 3000);
    }
}
