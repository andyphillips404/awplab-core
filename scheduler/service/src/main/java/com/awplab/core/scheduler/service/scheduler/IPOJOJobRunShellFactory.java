package com.awplab.core.scheduler.service.scheduler;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Created by andyphillips404 on 12/31/15.
 */
public abstract class IPOJOJobRunShellFactory implements JobRunShellFactory {

    protected abstract Factory[] getFactories();

    private Scheduler scheduler;

    @Override
    public void initialize(Scheduler scheduler) throws SchedulerConfigException {
        this.scheduler = scheduler;
    }

    @Override
    public JobRunShell createJobRunShell(TriggerFiredBundle bundle) throws SchedulerException {
        Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();

        for (Factory factory : getFactories()) {

            PojoMetadata pojoMetadata = null;
            try {
                pojoMetadata = new PojoMetadata(factory.getComponentMetadata());
            } catch (Exception ex) {
                throw new SchedulerException("Exception getting meta data", ex);
            }


            if ((pojoMetadata.getClassName().equals(jobClass.getName()) || factory.getName().equals(jobClass.getName())) && factory.getState() == Factory.VALID) {
                try {
                    ComponentInstance componentInstance = factory.createComponentInstance(null);
                    if (componentInstance.getState() == ComponentInstance.VALID) {

                        return new IPOJOJobRunShell(scheduler, bundle, (InstanceManager)componentInstance);
                    } else {
                        componentInstance.dispose();
                        throw new SchedulerException("Unable to create InstanceManager from factory due to ComponentInstance state for factory: " + factory.getName());
                    }
                }
                catch (Exception ex) {
                    throw new SchedulerException("Exception trying to create InstanceManager from factory: " + factory.getName(), ex);
                }
            }

        }

        // must not be a factory, try ot instantiate as a standard class
        return new JobRunShell(scheduler, bundle);

        //throw new SchedulerException("Unable to create InstanceManager (not found?) from factory for class: " + jobClass.getName());

    }
}
