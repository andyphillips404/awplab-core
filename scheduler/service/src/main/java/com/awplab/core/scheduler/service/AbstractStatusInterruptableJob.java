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
 * This is the base abstract class that can be used to help support interruption gracefully and
 * implementation of the status interface.   This will allow the job to better fully utilize the
 * library and manager services on the command line and rest interface.
 *
 * Created by andyphillips404 on 2/24/15.
 */
public abstract class AbstractStatusInterruptableJob implements StatusJob, InterruptableJob {


    private final Object updateLock = new Object();

    private boolean interruptRequested = false;

    private Thread executeThread = null;

    public Thread getExecuteThread() {
        synchronized (updateLock) {
            return executeThread;
        }
    }


    /**
     * Returns true if interruption of the currently executing job has been requested, i.e. the
     * interrupt() method has been called and execution has not finalized.
     *
     * @return true if interrupt() has been called, false if not.
     */
    public boolean isInterruptRequested() {
        synchronized (updateLock) {
            return interruptRequested;
        }
    }

    private boolean running = true;

    /**
     * Returns true if the job is currently running.
     *
     * @return true if the job is running, false if not.
     */
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

        if (!isRunning()) throw new UnableToInterruptJobException("Job is not running!");

        synchronized (updateLock) {
            interruptRequested = true;

            executeThread.interrupt();
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
        executeThread = Thread.currentThread();
        try {
            interruptableExecute(context);
        }
        finally {

            executeThread = null;

            setRunning(false);
            synchronized (updateLock) {
                interruptRequested = false;
            }
        }
    }

    /**
     * New override point for the Job.   JobExecutionContext is the context passed form the
     * execute method in the traditional job interface
     *
     * @param context JobExecutionContext of the job
     * @throws JobExecutionException
     */
    public abstract void interruptableExecute(JobExecutionContext context) throws JobExecutionException;

}
