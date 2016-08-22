package com.awplab.core.scheduler.service.scheduler;


import com.awplab.core.scheduler.service.AbstractSchedulerProvider;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
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

    /**
     * Configuration Admin factory name to use when creating instances using the configuration admin
     */
    public final static String CONFIG_FACTORY_NAME = "com.awplab.core.scheduler.volatile";

    public final static String PROPERTY_NAME = "com.awplab.core.scheduler.volatile.name";
    public final static String PROPERTY_THREADS = "com.awplab.core.scheduler.volatile.threads";
    public final static String PROPERTY_PRIORITY = "com.awplab.core.scheduler.volatile.priority";


    private QuartzSchedulerResources qsr;
    private IPOJOJobFactory ipojoJobFactory;


    private ResizableThreadPool threadPool;
    private int initialThreadPoolSize = -1;
    private int threadPriority = Thread.NORM_PRIORITY;
    private String name = null;

    private VolatileSchedulerProvider() {

    }

    public VolatileSchedulerProvider(String name, int initialThreadPoolSize, int threadPriority) {
        this.initialThreadPoolSize = initialThreadPoolSize;
        this.threadPriority = threadPriority;
        this.name = name;
    }

    public VolatileSchedulerProvider(String name, int initialThreadPoolSize) {
        this.initialThreadPoolSize = initialThreadPoolSize;
        this.name = name;
    }


    @Property(name = PROPERTY_NAME, mandatory = true, immutable = true)
    public void setName(String name) {
        if (isSchedulerCreated()) {
            throw new RuntimeException(new OperationNotSupportedException("Cannot change scheduler name after scheduler creation"));
        }
        this.name = name;

    }

    @Property(name = PROPERTY_THREADS, mandatory = true)
    public void setThreadPoolSize(int poolSize)  {
        if (threadPool == null) {
            initialThreadPoolSize = poolSize;
        }
        else {
            threadPool.setThreadPoolExecutorThreads(poolSize);
        }
    }

    @Property(name = PROPERTY_PRIORITY, immutable = true)
    public void setThreadPriority(int priority) {
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
            qsr.setJobRunShellFactory(new IPOJOJobRunShellFactory());
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
    public JobFactory getInitialJobFactory() {
        if (ipojoJobFactory == null) {
            ipojoJobFactory = new IPOJOJobFactory();
        }
        return ipojoJobFactory;
    }
}


