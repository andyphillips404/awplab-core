package com.awplab.core.scheduler.service.manager;

import com.awplab.core.scheduler.service.SchedulerManagerService;
import com.awplab.core.scheduler.service.events.EventAdminListener;
import com.awplab.core.scheduler.service.events.SchedulerEventTopics;
import com.google.common.collect.Maps;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.*;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 12/23/15.
 */

@Component(publicFactory = false, immediate = true)
@Provides(specifications = SchedulerManagerService.class)
@Instantiate
public class SchedulerManagerProvider implements SchedulerManagerService, BundleListener {


    private Set<Scheduler> schedulers = Collections.synchronizedSet(new HashSet<>());

    private Map<String, EventAdminListener> listeners = Collections.synchronizedMap(new HashMap<>());

    private Logger logger = LoggerFactory.getLogger(SchedulerManagerProvider.class);

    @Context
    BundleContext bundleContext;

    
    @Validate
    private void start() {
        bundleContext.addBundleListener(this);
        logger.info("Scheduler Manager Started");

        SchedulerEventTopics.postEvent(SchedulerEventTopics.MANAGER_STARTED);
    }

    private String getSchedulerNameIgnoreException(Scheduler scheduler) {
        try {
            return scheduler.getSchedulerName();
        }
        catch (SchedulerException ignored) {
            return null;
        }

    }


    @Invalidate
    public void stop() {
        bundleContext.removeBundleListener(this);

        for (Scheduler scheduler : getSchedulers()) {
            try {
                removeScheduler(scheduler);
            } catch (SchedulerException ex) {
                logger.error("Exception trying to remove scheduler: " + getSchedulerNameIgnoreException(scheduler));
            }
        }

        logger.info("Scheduler Manager Shut Down");

        SchedulerEventTopics.postEvent(SchedulerEventTopics.MANAGER_STOPPED);

    }



    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.STOPPED) {

            for (Scheduler scheduler : getSchedulers()) {

                if (FrameworkUtil.getBundle(scheduler.getClass()).equals(bundleEvent.getBundle())) {
                    try {
                        removeScheduler(scheduler);
                    } catch (SchedulerException ex) {
                        logger.error("Excpetion removing scheduler: " + getSchedulerNameIgnoreException(scheduler), ex);
                    }
                } else {
                    try {
                        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
                            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                            if (bundleEvent.getBundle().equals(FrameworkUtil.getBundle(jobDetail.getClass()))) {
                                try {
                                    //forceShutdownAndDelete(scheduler, jobKey);
                                    deleteAndInterruptJob(scheduler.getSchedulerName(), jobKey);
                                } catch (SchedulerException ex) {
                                    logger.error("Unable to shut down and delete job with jobkey: " + jobKey.toString(), ex);
                                }
                            }
                        }
                    } catch (SchedulerException ex) {
                        logger.error("Exception attempting to remove jobs with classes from bundle", ex);
                    }
                }
            }
        }


    }

    /*
    private void forceShutdownAndDelete(Scheduler scheduler, JobKey jobKey) throws SchedulerException {

        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        //if (jobDetail.getJobClass().isAssignableFrom(StatusJob.class)) interruptThread(scheduler.getSchedulerName(), jobKey);
        if (jobDetail.getJobClass().isAssignableFrom(InterruptableJob.class)) scheduler.interrupt(jobKey);
        scheduler.deleteJob(jobKey);


    }
    */

    @Override
    public synchronized void addScheduler(Scheduler scheduler) throws SchedulerException {
        if (getSchedulerNames().contains(scheduler.getSchedulerName())) {
            throw new SchedulerException("Scheduler name alrady exists.   Must be unique");
        }

        EventAdminListener eventAdminListener = new EventAdminListener(scheduler, scheduler.getSchedulerName() + "_EventAdminListener");
        scheduler.getListenerManager().addSchedulerListener(eventAdminListener);
        scheduler.getListenerManager().addJobListener(eventAdminListener);
        scheduler.getListenerManager().addTriggerListener(eventAdminListener);
        listeners.put(scheduler.getSchedulerName(), eventAdminListener);

        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        schedulers.add(scheduler);

        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_REGISTERED);
    }

    @Override
    public synchronized void removeScheduler(Scheduler scheduler) throws SchedulerException {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown(true);
        }

        schedulers.remove(scheduler);

        EventAdminListener eventAdminListener = listeners.get(scheduler.getSchedulerName());
        if (eventAdminListener != null) {
            scheduler.getListenerManager().removeSchedulerListener(eventAdminListener);
            scheduler.getListenerManager().removeJobListener(eventAdminListener.getName());
            scheduler.getListenerManager().removeTriggerListener(eventAdminListener.getName());
        }

        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_UNREGISTERED);
    }

    @Override
    public void removeScheduler(String schedulerName) throws SchedulerException {
        removeScheduler(getScheduler(schedulerName));
    }


    @Bind(aggregate = true, optional = true)
    private void bindScheduler(Scheduler scheduler) {
        String schedulerName = null;
        try {
            schedulerName = scheduler.getSchedulerName();
            addScheduler(scheduler);
        }
        catch (SchedulerException ex) {

            logger.error("Exception trying to bind new scheduler: " + (schedulerName == null ? "NULL" : schedulerName), ex);
        }
    }

    @Unbind(aggregate = true, optional = true)
    private void unbindScheduler(Scheduler scheduler) {
        String schedulerName = null;
        try {
            schedulerName = scheduler.getSchedulerName();
            removeScheduler(scheduler);
        }
        catch (SchedulerException ex) {
            logger.error("Exception trying to unbind new scheduler: " + (schedulerName == null ? "NULL" : schedulerName), ex);
        }

    }


    @Override
    public Set<Scheduler> getSchedulers() {
        return Collections.unmodifiableSet(schedulers);
    }

    @Override
    public Scheduler getScheduler(String name) throws SchedulerException {
        for (Scheduler schedulerService : getSchedulers()) {
            if (schedulerService.getSchedulerName().equals(name)) {
                return schedulerService;
            }
        }
        throw new SchedulerException("Unknown scheduler: " + name);

    }

    @Override
    public Set<String> getSchedulerNames() throws SchedulerException {
        HashSet<String> names = new HashSet<>();
        for (Scheduler scheduler : getSchedulers()) {
            names.add(scheduler.getSchedulerName());
        }
        return names;
    }


    @Override
    public String runJob(String schedulerName, Class<? extends Job> jobClass) throws SchedulerException {
        return runJob(schedulerName, jobClass, null);
    }

    @Override
    public String runJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);

        String id = UUID.randomUUID().toString();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(id)
                .setJobData((jobDataMap == null ? new JobDataMap() : jobDataMap))
                .build();

        Trigger jobTrigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(id)
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, jobTrigger);

        return id;
    }

    @Override
    public String scheduleJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap, ScheduleBuilder scheduleBuilder) throws SchedulerException {
        if (scheduleBuilder == null) throw new IllegalArgumentException("Must give Scheduler Builder");

        Scheduler scheduler = getScheduler(schedulerName);
        String id = UUID.randomUUID().toString();

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(id)
                .setJobData((jobDataMap == null ? new JobDataMap() : jobDataMap))
                .build();

        Trigger jobTrigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(id)
                .withSchedule(scheduleBuilder)
                .build();

        scheduler.scheduleJob(jobTrigger);

        return id;
    }

    @Override
    public String scheduleJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap, String cronSchedule) throws SchedulerException {
        return scheduleJob(schedulerName, jobClass, jobDataMap, CronScheduleBuilder.cronSchedule(cronSchedule));
    }

    @Override
    public String scheduleJob(String schedulerName, Class<? extends Job> jobClass, JobDataMap jobDataMap, long fromNow, TimeUnit timeUnit) throws SchedulerException {

        Scheduler scheduler = getScheduler(schedulerName);
        String id = UUID.randomUUID().toString();

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(id)
                .setJobData((jobDataMap == null ? new JobDataMap() : jobDataMap))
                .build();

        Trigger jobTrigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(id)
                .startAt(new Date(new Date().getTime() + timeUnit.toMillis(fromNow)))
                .build();

        scheduler.scheduleJob(jobTrigger);

        return id;

    }


    @Override
    public boolean isRunning(Class<? extends Job> jobClass) throws SchedulerException {
        return isRunning(jobClass, null);
    }

    @Override
    public boolean isRunning(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        return isRunning(jobClass, requiredMatchingJobDataMapEntries, null);
    }

    @Override
    public boolean isRunning(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException {

        for (Scheduler scheduler : getSchedulers()) {
            if (isRunning(scheduler.getSchedulerName(), jobClass, requiredMatchingJobDataMapEntries, ignoreThisInstance)) return true;
        }

        return false;
    }

    @Override
    public boolean isRunning(String schedulerName, Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException {
        for (JobExecutionContext jobContext : getScheduler(schedulerName).getCurrentlyExecutingJobs()) {
            if (jobContext.getJobInstance().getClass().isAssignableFrom(jobClass)) {
                if (ignoreThisInstance != null && jobContext.getJobInstance().equals(ignoreThisInstance)) continue;

                if (requiredMatchingJobDataMapEntries != null) {
                    if (Maps.difference(requiredMatchingJobDataMapEntries.getWrappedMap(), jobContext.getMergedJobDataMap().getWrappedMap()).entriesInCommon().equals(requiredMatchingJobDataMapEntries.getWrappedMap())) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

        }

        return false;
    }

    @Override
    public boolean isScheduled(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        for (Scheduler scheduler : getSchedulers()) {
            if (isScheduled(scheduler.getSchedulerName(), jobClass, requiredMatchingJobDataMapEntries)) return true;
        }

        return false;
    }

    @Override
    public boolean isScheduled(String schedulerName, Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup())) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail.getJobClass().isAssignableFrom(jobClass)) {
                if (requiredMatchingJobDataMapEntries != null) {
                    if (Maps.difference(requiredMatchingJobDataMapEntries.getWrappedMap(), jobDetail.getJobDataMap().getWrappedMap()).entriesInCommon().equals(requiredMatchingJobDataMapEntries.getWrappedMap())) {
                        return true;
                    }
                } else {
                    return true;
                }

            }
        }


        return false;
    }

    @Override
    public void deleteJobs(String schedulerName) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
            scheduler.deleteJob(jobKey);
        }
    }

    @Override
    public void interruptJobs(String schedulerName) throws SchedulerException {
        for (JobExecutionContext jobContext : getScheduler(schedulerName).getCurrentlyExecutingJobs()) {
            getScheduler(schedulerName).interrupt(jobContext.getFireInstanceId());
        }
    }

    @Override
    public void deleteAndInterruptJobs(String schedulerName) throws SchedulerException {
        deleteJobs(schedulerName);
        interruptJobs(schedulerName);
    }

    @Override
    public void deleteJobs() throws SchedulerException {
        for (String schedulerName : getSchedulerNames()) {
            deleteJobs(schedulerName);
        }
    }

    @Override
    public void interruptJobs() throws SchedulerException {
        for (String schedulerName : getSchedulerNames()) {
            interruptJobs(schedulerName);
        }
    }

    @Override
    public void deleteAndInterruptJobs() throws SchedulerException {
        for (String schedulerName : getSchedulerNames()) {
            deleteAndInterruptJobs(schedulerName);
        }
    }

    @Override
    public boolean deleteJob(String schedulerName, JobKey jobKey) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);
        return scheduler.deleteJob(jobKey);
    }

    @Override
    public void deleteJobs(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        for (Scheduler scheduler : getSchedulers()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup())) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (jobDetail.getJobClass().isAssignableFrom(jobClass)) {
                    if (requiredMatchingJobDataMapEntries != null) {
                        if (Maps.difference(requiredMatchingJobDataMapEntries.getWrappedMap(), jobDetail.getJobDataMap().getWrappedMap()).entriesInCommon().equals(requiredMatchingJobDataMapEntries.getWrappedMap())) {
                            scheduler.deleteJob(jobKey);
                        }
                    }
                }
            }


        }

    }

    @Override
    public boolean interruptJob(String schedulerName, JobKey jobKey) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);
        return scheduler.interrupt(jobKey);

    }

    @Override
    public void interruptJobs(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        for (Scheduler scheduler : getSchedulers()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup())) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (jobDetail.getJobClass().isAssignableFrom(jobClass)) {
                    if (requiredMatchingJobDataMapEntries != null) {
                        if (Maps.difference(requiredMatchingJobDataMapEntries.getWrappedMap(), jobDetail.getJobDataMap().getWrappedMap()).entriesInCommon().equals(requiredMatchingJobDataMapEntries.getWrappedMap())) {
                            scheduler.interrupt(jobKey);
                        }
                    }
                }
            }


        }

    }

    @Override
    public boolean deleteAndInterruptJob(String schedulerName, JobKey jobKey) throws SchedulerException {
        interruptJob(schedulerName, jobKey);
        return deleteJob(schedulerName, jobKey);
    }

    @Override
    public void deleteAndInterruptJob(Class<? extends Job> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        for (Scheduler scheduler : getSchedulers()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup())) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (jobDetail.getJobClass().isAssignableFrom(jobClass)) {
                    if (requiredMatchingJobDataMapEntries != null) {
                        if (Maps.difference(requiredMatchingJobDataMapEntries.getWrappedMap(), jobDetail.getJobDataMap().getWrappedMap()).entriesInCommon().equals(requiredMatchingJobDataMapEntries.getWrappedMap())) {
                            scheduler.interrupt(jobKey);
                            scheduler.deleteJob(jobKey);
                        }
                    }
                }
            }


        }

    }

    @Override
    public void deleteJobs(String schedulerName, GroupMatcher<JobKey> matcher) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);
        for (JobKey jobkey : scheduler.getJobKeys(matcher)) {
            scheduler.deleteJob(jobkey);
        }
    }

    @Override
    public JobExecutionContext getRunningJob(String schedulerName, JobKey jobKey) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);
        for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
            if (context.getJobDetail().getKey().equals(jobKey)) {
                return context;
            }
        }

        throw new SchedulerException("Job Key not found in executing jobs");
    }

}
