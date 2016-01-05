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

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "volatile-delete")
@Service
public class VolatileDeleteCommand implements Action {

    @Reference
    private ConfigurationAdmin configurationAdmin;

    @Reference
    private SchedulerManagerService schedulerManagerService;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to delete", required = true, multiValued = false)
    String schedulerName = null;

    @Override
    public Object execute() throws Exception {

        if (schedulerManagerService.getScheduler(schedulerName) == null) {
            throw new IllegalArgumentException("Scheduler with that name doesn't exists");
        }

        for (Configuration configuration : configurationAdmin.listConfigurations(VolatileSchedulerProvider.CONFIG_FACTORY_NAME)) {
            String name = (String)configuration.getProperties().get(VolatileSchedulerProvider.PROPERTY_NAME);
            if (name.equalsIgnoreCase(schedulerName)) {
                configuration.delete();
                return null;
            }
        }

        throw new IllegalArgumentException("Scheduler configuration not found!");

    }
}
