package com.awplab.core.scheduler.service.scheduler;

import org.quartz.SchedulerConfigException;
import org.quartz.core.QuartzSchedulerThread;
import org.quartz.spi.ThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 12/16/15.
 */
public class ResizableThreadPool implements ThreadPool {

    private final Object nextRunnableLock = new Object();

    private class ResizableThreadPoolFactory implements ThreadFactory {

        ThreadFactory defaultThreadFactory = Executors.privilegedThreadFactory();
        @Override
        public Thread newThread(Runnable r) {
            Thread t = defaultThreadFactory.newThread(r);
            t.setPriority(threadPriority);
            return t;
        }
    }


    private final int initialThreadPoolSize;
    private final int threadPriority;

    public ResizableThreadPool(int initialThreadPoolSize, int threadPriority) {
        this.initialThreadPoolSize = initialThreadPoolSize;
        this.threadPriority = threadPriority;
    }

    boolean handoffPending = false;
    boolean isShutdown = false;

    @Override
    public boolean runInThread(Runnable runnable) {

        synchronized (nextRunnableLock) {

            handoffPending = true;

            // Wait until a worker thread is available
            while (threadPoolExecutor.getActiveCount() >= threadPoolExecutor.getMaximumPoolSize() && !isShutdown) {
                try {
                    nextRunnableLock.wait(500);
                } catch (InterruptedException ignore) {
                }
            }

            if (!isShutdown) {
                threadPoolExecutor.submit(runnable);
            }
            nextRunnableLock.notifyAll();
            handoffPending = false;
        }

        return true;
    }

    @Override
    public int blockForAvailableThreads() {
        synchronized(nextRunnableLock) {

            while((threadPoolExecutor.getActiveCount() >= threadPoolExecutor.getMaximumPoolSize()|| handoffPending) && !isShutdown) {
                try {
                    nextRunnableLock.wait(500);
                } catch (InterruptedException ignore) {
                }
            }

            return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
        }
    }


    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void initialize() throws SchedulerConfigException {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(initialThreadPoolSize, new ResizableThreadPoolFactory());
        }

    }

    public void setThreadPoolExecutorThreads(int nThreads) {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.setCorePoolSize(nThreads);
            threadPoolExecutor.setMaximumPoolSize(nThreads);

        }
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        isShutdown = true;

        synchronized (nextRunnableLock) {

            threadPoolExecutor.shutdownNow();

            nextRunnableLock.notifyAll();

            if (waitForJobsToComplete) {
                try {
                    if (!threadPoolExecutor.awaitTermination(1, TimeUnit.MINUTES)) {

                    }
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public int getPoolSize() {
        return threadPoolExecutor.getCorePoolSize();
    }

    private String schedInstId;

    private String schedName;

    @Override
    public void setInstanceId(String schedInstId) {
        this.schedInstId = schedInstId;
    }

    @Override
    public void setInstanceName(String schedName) {
        this.schedName = schedName;
    }
}
