package com.awplab.core.scheduler.service.scheduler;


import com.awplab.core.scheduler.service.AbstractSchedulerProvider;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.quartz.Scheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.DefaultThreadExecutor;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.JobFactory;

import javax.naming.OperationNotSupportedException;
import java.util.UUID;

/**
 * Created by andyphillips404 on 12/23/15.
 */
@Component(name = VolatileSchedulerProvider.CONFIG_FACTORY_NAME)
@Provides(specifications = Scheduler.class)
public class VolatileSchedulerProvider extends AbstractSchedulerProvider {


    @Requires(optional = true)
    Factory[] factories;

    public final static String CONFIG_FACTORY_NAME = "com.awplab.core.scheduler.volatile";

    public final static String PROPERTY_NAME = "com.awplab.core.scheduler.volatile.name";
    public final static String PROPERTY_THREADS = "com.awplab.core.scheduler.volatile.threads";
    public final static String PROPERTY_PRIORITY = "com.awplab.core.scheduler.volatile.priority";


    private QuartzSchedulerResources qsr;
    private IPOJOJobFactory ipojoJobFactory;


    private class RunShellFactory extends IPOJOJobRunShellFactory {
            @Override
            protected Factory[] getFactories() {
                return factories;
            }
    }


    private ResizableThreadPool threadPool;
    private int initialThreadPoolSize = -1;
    private int threadPriority = Thread.NORM_PRIORITY;
    private String name = null;


    @Property(name = PROPERTY_NAME, mandatory = true, immutable = true)
    private void setName(String name) {
        if (isSchedulerCreated()) {
            throw new RuntimeException(new OperationNotSupportedException("Cannot change scheduler name after scheduler creation"));
        }
        this.name = name;

    }

    @Property(name = PROPERTY_THREADS, mandatory = true)
    private void setThreadPoolSize(int poolSize)  {
        if (threadPool == null) {
            initialThreadPoolSize = poolSize;
        }
        else {
            threadPool.setThreadPoolExecutorThreads(poolSize);
        }
    }

    @Property(name = PROPERTY_PRIORITY, immutable = true)
    private void setThreadPriority(int priority) {
        if (isSchedulerCreated()) {
            throw new RuntimeException(new OperationNotSupportedException("Cannot change scheduler thread priority after scheduler creation"));
        }
        threadPriority = priority;
    }



    @Override
    public QuartzSchedulerResources getQuartzSchedulerResources() {


        if (qsr == null) {
            qsr = new QuartzSchedulerResources();
            qsr.setName(name);
            qsr.setJobRunShellFactory(new RunShellFactory());
            qsr.setThreadExecutor(new DefaultThreadExecutor());
            qsr.setBatchTimeWindow(0l);
            qsr.setInstanceId(name + "_" + UUID.randomUUID().toString());
            qsr.setInterruptJobsOnShutdown(true);
            qsr.setInterruptJobsOnShutdownWithWait(true);
            qsr.setMaxBatchSize(1);
            qsr.setJobStore(new RAMJobStore());
            threadPool = new ResizableThreadPool(initialThreadPoolSize, threadPriority);
            qsr.setThreadPool(threadPool);
        }

        return qsr;
    }

    @Override
    public JobFactory getJobFactory() {
        if (ipojoJobFactory == null) {
            ipojoJobFactory = new IPOJOJobFactory();
        }
        return ipojoJobFactory;
    }
}


