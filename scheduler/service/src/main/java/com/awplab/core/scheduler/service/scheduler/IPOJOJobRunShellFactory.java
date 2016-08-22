package com.awplab.core.scheduler.service.scheduler;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/31/15.
 */
public class IPOJOJobRunShellFactory implements JobRunShellFactory {

    protected Set<Factory> getFactories() {
        Set<Factory> factories = new HashSet<>();

        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(IPOJOJobRunShellFactory.class).getBundleContext();
            ServiceReference[] refs = bundleContext.getServiceReferences(Factory.class.getName(), null);
            if (refs != null) {
                for (ServiceReference serviceReference : refs) {
                    factories.add((Factory)bundleContext.getService(serviceReference));
                }
            }

            return factories;
        }
        catch (Exception ex) {
            LoggerFactory.getLogger(IPOJOJobRunShellFactory.class).error("Exception getting factories.", ex);
            return Collections.emptySet();
        }

    }

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
