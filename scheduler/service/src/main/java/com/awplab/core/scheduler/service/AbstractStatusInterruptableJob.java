package com.awplab.core.scheduler.service;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 2/24/15.
 */
public abstract class AbstractStatusInterruptableJob implements StatusJob, InterruptableJob {


    private final Object updateLock = new Object();

    private boolean interruptRequested = false;

    public boolean isInterruptRequested() {
        synchronized (updateLock) {
            return interruptRequested;
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


    @Override
    public void interrupt() throws UnableToInterruptJobException {

        synchronized (updateLock) {
            interruptRequested = true;
        }

        final Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return AbstractStatusInterruptableJob.this.isRunning();
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
        setRunning(true);
        try {
            interruptableExecute(context);
        }
        finally {
            setRunning(false);
        }
    }


    public abstract void interruptableExecute(JobExecutionContext context) throws JobExecutionException;

}
