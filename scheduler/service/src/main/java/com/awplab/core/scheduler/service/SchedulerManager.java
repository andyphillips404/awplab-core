package com.awplab.core.scheduler.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The scheduler manager service will manage all schedulers assigned to it.   The manager will also monitor
 * any registered service of class {@link Scheduler} registering these with the manager.
 *
 * Created by andyphillips404 on 2/24/15.
 */
public interface SchedulerManager {

    static SchedulerManager getProvider() {
        BundleContext bundleContext = FrameworkUtil.getBundle(SchedulerManager.class).getBundleContext();
        ServiceReference ref = bundleContext.getServiceReference(SchedulerManager.class.getName());
        if (ref != null) {
            return (SchedulerManager)bundleContext.getService(ref);
        }

        return null;
    }

    /**
     * Returns all schedulers managed my the manager
     * @return set of schedulers
     */
    Set<Scheduler> getSchedulers();

    /**
     * Returns a scheduler of the given name.
     * @param name the unique name of the scheduler
     * @return the scheduler with the given name
     * @throws SchedulerException if the scheduler cannot be found by the given name
     */
    Scheduler getScheduler(String name) throws SchedulerException;

    /**
     * Returns a set of all scheduler names.
     * @return set of scheduler names
     * @throws SchedulerException
     */
    Set<String> getSchedulerNames() throws SchedulerException;

    /**
     * Adds the given scheduler to the manager.   The scheduler, if not started, is started.   The scheduler should
     * NOT have been created with another quartz scheduler factory (DirectSchedulerFactory or StdSchedulerFactory).
     * @param scheduler scheduler to add
     * @throws SchedulerException if there is an issue starting the scheduelr
     */
    void addScheduler(Scheduler scheduler) throws SchedulerException;

    /**
     * Remove the scheduler from the manager.
     * If the scheduler has not been shut down, the scheduler will be shut
     * shut down as part of this process and all jobs will be terminated as part of such.
     * @param scheduler  scheduler to remove
     * @throws SchedulerException if the scheduler is not in the manager or cannot be shut down.
     */
    void removeScheduler(Scheduler scheduler) throws SchedulerException;

    /**
     * Remove the scheduler form the manager by the given scheduler name.
     * If the scheduler has not been shut down, the scheduler will be shut
     * shut down as part of this process and all jobs will be terminated as part of such.
     * @param schedulerName scheduler name of the scheduler to remove
     * @throws SchedulerException if a scheduler by the name cannot be found or there is an issue shuting down the scheduler
     */
    void removeScheduler(String schedulerName) throws SchedulerException;


    /**
     * Runs a job immediately on the scheduler identified by the scheduler name.
     * The job will be add to the scheduler with a random unique job id and matching trigger id and group null.
     * @param schedulerName name of scheduler to run the job
     * @param jobClass class of the job to run
     * @return the unique id of the job key and trigger key.
     * @throws SchedulerException if the scheduler is not found or an exception scheduling the job
     */
    String runJob(String schedulerName, Class<? extends Job> jobClass) throws SchedulerException;

    /**
     * Runs a job immediately on the scheduler identified by the scheduler name.
     * The job will be add to the scheduler with a random unique job id and matching trigger id and group null.
     * @param schedulerName name of scheduler to run the job
     * @param jobClass class of the job to run
     * @param jobDataMap data map that will be added to the {@link JobDetail}
     * @return the unique id of the job key and trigger key
     * @throws SchedulerException if the scheduler is not found or an exception scheduling the job
     */
    String runJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap) throws SchedulerException;

    /**
     * Schedule a job on the scheduler identified by the scheduler name.
     * The job will be added to the scheduler with a random unique job id and matching trigger id and group null.
     * @param schedulerName name of the scheduler to run the job
     * @param jobClass class of the job to run
     * @param jobDataMap data map that will be added to the {@link JobDetail}
     * @param scheduleBuilder a scheduler builder identifying the schedule to run the job on
     * @return the unique id of the job key and trigger key
     * @throws SchedulerException if the scheduler is not found or an exception scheduling the job
     */
    String scheduleJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap, ScheduleBuilder scheduleBuilder) throws SchedulerException;

    /**
     * Schedule a job on the scheduler identified by the scheduler name with a cron schedule.
     * The job will be added to the scheduler with a random unique job id and matching trigger id and group null.
     * @param schedulerName name of the scheduler to run the job
     * @param jobClass class of the job to run
     * @param jobDataMap data map that will be added to the {@link JobDetail}
     * @param cronSchedule a quartz cron schedule string representing the schedule to run the job at
     * @return the unique id of the job key and trigger key
     * @throws SchedulerException if the scheduler is not found or an exception scheduling the job
     */
    String scheduleJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap, String cronSchedule) throws SchedulerException;

    /**
     * Schedule a job on the scheduler identified by the scheduler name, running once in a specified time from now.
     * The job will be added to the scheduler with a random unique job id and matching trigger id and group null.
     * @param schedulerName name of the scheduler to run the job
     * @param jobClass class of the job to run
     * @param jobDataMap data map that will be added to the {@link JobDetail}
     * @param fromNow number of TimeUnit increments to run the job in from now
     * @param timeUnit the time unit of fromNow
     * @return the unique id of the job key and trigger key
     * @throws SchedulerException if the scheduler is not found or an exception scheduling the job
     */
    String scheduleJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap, long fromNow, TimeUnit timeUnit) throws SchedulerException;


    /**
     * Looks to see if the following job class is currently running on any scheduler
     * @param jobClass job class to search for
     * @return true if a match is found, false if not
     * @throws SchedulerException
     */
    public boolean isRunning(Class<? extends Job> jobClass) throws SchedulerException;

    /**
     * Looks to see if the following job class is currently running on any scheduler, and the {@link JobDataMap}
     * contains all matching entries in the requiredMatchingJobDataMapEntries.
     * @param jobClass job class to search for
     * @param requiredMatchingJobDataMapEntries map of required matches in the {@link JobDataMap}
     * @return true if a match is found, false if not
     * @throws SchedulerException
     */
    public boolean isRunning(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException;

    /**
     * Looks to see if the following job class is currently running on any scheduler, and the {@link JobDataMap}
     * contains all matching entries in the requiredMatchingJobDataMapEntries ignoring any instance equaling to the
     * ignoreThisInstance.   The mathods .equals() is used to determine if equal
     * @param jobClass job class to search for
     * @param requiredMatchingJobDataMapEntries map of required matches in the {@link JobDataMap}, null if no match required
     * @param ignoreThisInstance specific instance to ignore, null if nothing to ignore
     * @return true if a match is found, false if not
     * @throws SchedulerException
     */
    public boolean isRunning(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException;

    /**
     * Looks to see if the following job class is currently running on scheduler identified by schedulerName,
     * and the {@link JobDataMap} contains all matching entries in the requiredMatchingJobDataMapEntries ignoring any
     * instance equaling to the ignoreThisInstance.   The mathods .equals() is used to determine if equal.
     * @param schedulerName scheduler name of the scheduler to search
     * @param jobClass job class to search for
     * @param requiredMatchingJobDataMapEntries map of required matches in the {@link JobDataMap}, null if no match required
     * @param ignoreThisInstance specific instance to ignore, null if nothing to ignore
     * @return true if a match is found, false if not
     * @throws SchedulerException
     */
    public boolean isRunning(String schedulerName, Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException;

    /**
     * Looks to see if the following job class is currently scheduled or running on any scheduler,
     * and the {@link JobDataMap} contains all matching entries in the requiredMatchingJobDataMapEntries.
     * @param jobClass job class to search for
     * @param requiredMatchingJobDataMapEntries map of required matches in the {@link JobDataMap}, null if no match required
     * @return true if a match if found, false if not
     * @throws SchedulerException
     */
    public boolean isScheduled(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException;

    /**
     * Looks to see if the following job class is currently scheduled or running on scheduler identified by schedulerName,
     * and the {@link JobDataMap} contains all matching entries in the requiredMatchingJobDataMapEntries.
     * @param schedulerName scheduler name of the scheduler to search
     * @param jobClass job class to search for
     * @param requiredMatchingJobDataMapEntries map of required matches in the {@link JobDataMap}, null if no match required
     * @return true if a match if found, false if not
     * @throws SchedulerException
     */
    public boolean isScheduled(String schedulerName, Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException;

    /**
     * Deletes all scheduled jobs on scheduler.  Equivalent to calling {@link Scheduler#deleteJob(JobKey)} for all JobKeys in
     * the scheduler.  Does not interrupt actively running jobs.
     * @param schedulerName scheduler name of the scheduler
     * @throws SchedulerException
     */
    public void deleteJobs(String schedulerName) throws SchedulerException;

    /**
     * Interrupts all running jobs on the scheduler.   Equivalent to calling {@link Scheduler#interrupt(JobKey)} for all
     * currently executing jobs of the scheduler.
     * @param schedulerName scheduler name of the scheduler
     * @throws SchedulerException
     */
    public void interruptJobs(String schedulerName) throws SchedulerException;

    /**
     * Interrupts and deletes all jobs and running jobs on the scheduler.   Equivalent to calling both {@link Scheduler#interrupt(JobKey)}
     * and {@link Scheduler#deleteJob(JobKey)} for all running and scheduled jobs.
     * @param schedulerName scheduler name of the scheduler
     * @throws SchedulerException
     */
    public void deleteAndInterruptJobs(String schedulerName) throws SchedulerException;

    /**
     * Deletes all scheduled jobs on all schedulers.  Equivalent to calling {@link Scheduler#deleteJob(JobKey)} for all JobKeys.
     * Does not interrupt actively running jobs.
     * @throws SchedulerException
     */
    public void deleteJobs() throws SchedulerException;

    /**
     * Interrupts all running jobs on all schedulers.   Equivalent to calling {@link Scheduler#interrupt(JobKey)} for all
     * currently executing jobs.
     * @throws SchedulerException
     */
    public void interruptJobs() throws SchedulerException;

    /**
     * Interrupts and deletes all jobs and running jobs on all schedulers.   Equivalent to calling both {@link Scheduler#interrupt(JobKey)}
     * and {@link Scheduler#deleteJob(JobKey)} for all running and scheduled jobs.
     * @throws SchedulerException
     */
    public void deleteAndInterruptJobs() throws SchedulerException;

    /**
     * Delete the scheduled job with the matching JobKey
     * @param schedulerName scheduler to search the job for
     * @param jobKey job key to search for
     * @return true if the job was found and deleted, false if not
     * @throws SchedulerException
     */
    public boolean deleteJob(String schedulerName, JobKey jobKey) throws SchedulerException ;


    /**
     * Delete jobs that are of jobClass and have matching job data map entries in the JobDataMap
     * @param jobClass class to match against
     * @param requiredMatchingJobDataMapEntries matching job data map entries to require.  Null if no matches required
     * @throws SchedulerException
     */
    public void deleteJobs(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException ;

    /**
     * Interrupt job with the JobKey on the scheduler identified by the scheduler name
     * @param schedulerName scheduler name of the scheduler to interrupt the job
     * @param jobKey job key of the running job to interrupt
     * @return true if interruption was successful, false if no running job was found
     * @throws SchedulerException
     */
    public boolean interruptJob(String schedulerName, JobKey jobKey) throws SchedulerException;

    /**
     * Interrupt all jobs of jobClass and have matching job data map entries in the JobDataMap
     * @param jobClass class to match against
     * @param requiredMatchingJobDataMapEntries matching job data map entries to require.  Null if no matches required
     * @throws SchedulerException
     */
    public void interruptJobs(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException ;

    /**
     * Delete all scheduled jobs and interrupt all running jobs matching job key on scheduler identified by scheduler name
     * @param schedulerName scheduler name of the scheduler to delete and interrupt the job
     * @param jobKey job key of the running job to interrupt
     * @return true if the interruption and/or deletion was success, false if no job was found
     * @throws SchedulerException
     */
    public boolean deleteAndInterruptJob(String schedulerName, JobKey jobKey) throws SchedulerException;

    /**
     * Delete all scheduled jobs and interrupt all running all jobs of jobClass and have matching job data map entries in the JobDataMap
     * @param jobClass class to match against
     * @param requiredMatchingJobDataMapEntries matching job data map entries to require.  Null if no matches required
     * @throws SchedulerException
     */
    public void deleteAndInterruptJob(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException ;

    /**
     * Delete all jobs on the scheduler that the group matches the group matcher
     * @param schedulerName scheduler name of the scheduler to delete the jobs on
     * @param matcher matcher to match the job key group against
     * @throws SchedulerException
     */
    public void deleteJobs(String schedulerName, GroupMatcher<JobKey> matcher) throws SchedulerException;

    /**
     * Get the JobExecutionContext of the running job identified by jobKey and in the scheduler of scheduler name
     * @param schedulerName scheduler name of the scheduler
     * @param jobKey job key of the running job
     * @return JobExecutionContext of the running job, null if not found
     * @throws SchedulerException
     */
    public JobExecutionContext getRunningJob(String schedulerName, JobKey jobKey) throws SchedulerException;


}
