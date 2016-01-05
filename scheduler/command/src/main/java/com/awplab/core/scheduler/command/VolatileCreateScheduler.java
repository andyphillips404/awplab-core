package com.awplab.core.scheduler.command;

import com.awplab.core.scheduler.service.SchedulerManagerService;
import com.awplab.core.scheduler.service.scheduler.VolatileSchedulerProvider;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.*;

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "volatile-create")
@Service
public class VolatileCreateScheduler implements Action {

    @Reference
    private ConfigurationAdmin configurationAdmin;

    @Reference
    private SchedulerManagerService schedulerManagerService;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to show", required = true, multiValued = false)
    String schedulerName = null;

    @Argument(index = 1, name = "threads", description = "thread pool size", required = true, multiValued = false)
    int threads;

    @Argument(index = 2, name = "priority", description = "thread priority", required = false, multiValued = false)
    int priority = Thread.NORM_PRIORITY;


    @Override
    public Object execute() throws Exception {

        if (schedulerManagerService.getSchedulerNames().contains(schedulerName)) {
            throw new IllegalArgumentException("Scheduler with that name already exists");
        }

        Configuration configuration = configurationAdmin.createFactoryConfiguration(VolatileSchedulerProvider.CONFIG_FACTORY_NAME, "?");
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put(VolatileSchedulerProvider.PROPERTY_NAME, schedulerName);
        dict.put(VolatileSchedulerProvider.PROPERTY_THREADS, threads);
        dict.put(VolatileSchedulerProvider.PROPERTY_PRIORITY, priority);
        configuration.update(dict);

        return configuration.getPid();


    }
}
