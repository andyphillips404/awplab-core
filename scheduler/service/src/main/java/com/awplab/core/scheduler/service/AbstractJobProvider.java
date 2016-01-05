package com.awplab.core.scheduler.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 2/24/15.
 */
public abstract class AbstractJobProvider implements JobService {


    private final Object updateLock = new Object();

    private boolean cancelRequested = false;

    public boolean isCancelRequested() {
        synchronized (updateLock) {
            return cancelRequested;
        }
    }

    public void requestCancel() {
        synchronized (updateLock) {
            cancelRequested = true;
        }
    }

    private boolean running = true;

    public boolean isRunning() {
        synchronized (updateLock) {
            return running;
        }
    }

    private void setRunning(boolean running) {
        synchronized (updateLock) {
            this.running = running;
        }
    }

    public void interruptThread() {
        if (executeThread != null && executeThread.isAlive()) {
            executeThread.interrupt();
        }
    }

    @JsonIgnore
    private Thread executeThread = null;

    @JsonIgnore
    @Override
    public Thread getExecuteThread() {
        synchronized (updateLock) {
            return executeThread;
        }
    }

    @JsonIgnore
    private void setExecuteThread() {
        synchronized (updateLock) {
            executeThread = Thread.currentThread();
        }

    }


    @Override
    public void interrupt() throws UnableToInterruptJobException {


        requestCancel();

        final Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return AbstractJobProvider.this.isRunning();
            }
        };

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(Predicates.equalTo(true))
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterDelay(10, TimeUnit.MINUTES))
                .build();
        try {
            retryer.call(callable);
        }
        catch (Exception e) {
            throw new UnableToInterruptJobException(e);
        }

        if (isRunning()) throw new UnableToInterruptJobException("Unable to stop job....");

    }


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        setExecuteThread();
        setRunning(true);
        try {
            cancelableExecute(context);
        }
        finally {
            setRunning(false);
        }
    }


    public abstract void cancelableExecute(JobExecutionContext context) throws JobExecutionException;

}
