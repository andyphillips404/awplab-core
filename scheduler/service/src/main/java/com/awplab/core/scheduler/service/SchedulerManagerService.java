package com.awplab.core.scheduler.service;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Date;
import java.util.Set;

/**
 * Created by andyphillips404 on 2/24/15.
 */
public interface SchedulerManagerService {


    Set<Scheduler> getSchedulers();

    Scheduler getScheduler(String name) throws SchedulerException;

    Set<String> getSchedulerNames() throws SchedulerException;

    void addScheduler(Scheduler scheduler) throws SchedulerException;

    void removeScheduler(Scheduler scheduler) throws SchedulerException;

    void removeScheduler(String schedulerName) throws SchedulerException;


    /*  Helper Scheduling functions */

    String runJob(String schedulerName, Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap jobDataMap, String jobGroup) throws SchedulerException;

    public String runJob(String schedulerName, Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap jobDataMap, String jobGroup, int inMinutes) throws SchedulerException;

    public String runJob(String schedulerName, Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap jobDataMap, String jobGroup, Date triggerDate) throws SchedulerException;

    public void scheduleJob(String schedulerName, Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap jobDataMap, JobKey jobKey, ScheduleBuilder scheduleBuilder) throws SchedulerException;

    public boolean isJobRunning(Class<? extends AbstractStatusInterruptableJob> jobClass) throws SchedulerException;

    public boolean isJobRunning(Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException;

    public boolean isJobRunning(Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException;

    public boolean isJobRunning(String schedulerName, Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException;

    public boolean isWaitingOrRunning(Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException;

    public boolean isWaitingOrRunning(String schedulerName, Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException;

    public void deleteJobs(String schedulerName) throws SchedulerException;

    public void interruptJobs(String schedulerName) throws SchedulerException;

    public void deleteAndInterruptJobs(String schedulerName) throws SchedulerException;

    public void deleteJobs() throws SchedulerException;

    public void interruptJobs() throws SchedulerException;

    public void deleteAndInterruptJobs() throws SchedulerException;

    public boolean deleteJob(String schedulerName, JobKey jobKey) throws SchedulerException ;

    public void deleteJobs(Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException ;

    public boolean interruptJob(String schedulerName, JobKey jobKey) throws SchedulerException;

    public void interruptJobs(Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException ;

    public boolean deleteAndInterruptJob(String schedulerName, JobKey jobKey) throws SchedulerException;

    public void deleteAndInterruptJob(Class<? extends AbstractStatusInterruptableJob> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException ;

    public void deleteJobs(String schedulerName, GroupMatcher<JobKey> matcher) throws SchedulerException;

    public JobExecutionContext getRunningJob(String schedulerName, JobKey jobKey) throws SchedulerException;

    public void interruptThread(String schedulerName, JobKey jobKey) throws SchedulerException;

}
