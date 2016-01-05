package com.awplab.core.scheduler.service.scheduler;

import org.apache.felix.ipojo.InstanceManager;
import org.quartz.Scheduler;
import org.quartz.core.JobRunShell;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Created by andyphillips404 on 12/22/15.
 */
public class IPOJOJobRunShell extends JobRunShell {

    private final InstanceManager instanceManager;

    public IPOJOJobRunShell(Scheduler scheduler, TriggerFiredBundle bundle, InstanceManager instanceManager) {
        super(scheduler, bundle);

        bundle.getTrigger().getJobDataMap().put(IPOJOJobFactory.INSTANCE_MANAGER_KEY, instanceManager);

        if (instanceManager == null) {
            throw new RuntimeException("InstanceManager cannot be null!");
        }

        this.instanceManager = instanceManager;
    }


    @Override
    public void run() {
        try {
            super.run();
        }
        finally {
            // lets destroy the instance, no matter what happens here...
            instanceManager.dispose();
        }
    }



}
