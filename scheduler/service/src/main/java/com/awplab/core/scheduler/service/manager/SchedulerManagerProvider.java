package com.awplab.core.scheduler.service.manager;

import com.awplab.core.scheduler.service.AbstractJobProvider;
import com.awplab.core.scheduler.service.JobService;
import com.awplab.core.scheduler.service.SchedulerManagerService;
import com.awplab.core.scheduler.service.events.EventAdminListener;
import com.awplab.core.scheduler.service.events.SchedulerEventTopics;
import com.google.common.collect.Maps;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleWiring;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.Calendar;

/**
 * Created by andyphillips404 on 12/23/15.
 */

@Component(publicFactory = false, immediate = true)
@Provides
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
    }

    @Invalidate
    public void stop() {
        bundleContext.removeBundleListener(this);
    }

    static Set<String> getClassNames(Bundle bundle) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null)
            return Collections.emptySet();
        Collection<String> resources = bundleWiring.listResources("/", "*.class", BundleWiring.LISTRESOURCES_RECURSE);
        Set<String> classNamesOfCurrentBundle = new HashSet<>();
        for (String resource : resources) {
            URL localResource = bundle.getEntry(resource);
            // Bundle.getEntry() returns null if the resource is not located in the specific bundle
            if (localResource != null) {
                String className = resource.replaceAll("/", ".").replaceAll("^(.*?)(\\.class)$", "$1");
                classNamesOfCurrentBundle.add(className);
            }
        }

        return classNamesOfCurrentBundle;
    }



    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.STOPPING) {
            Set<String> classNames = getClassNames(bundleEvent.getBundle());

            for (Scheduler scheduler : getSchedulers()) {
                try {
                    for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
                        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                        if (classNames.contains(jobDetail.getJobClass().getName())) {
                            try {
                                forceShutdownAndDelete(scheduler, jobKey);
                            }
                            catch (SchedulerException ex) {
                                logger.error("Unable to shut down and delete job with jobkey: " + jobKey.toString(), ex);
                            }
                        }
                    }
                }
                catch (SchedulerException ex) {
                    logger.error("Exception attempting to remove jobs with classes from bundle", ex);
                }
            }
        }


    }

    private void forceShutdownAndDelete(Scheduler scheduler, JobKey jobKey) throws SchedulerException {

        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail.getJobClass().isAssignableFrom(JobService.class)) interruptThread(scheduler.getSchedulerName(), jobKey);
        if (jobDetail.getJobClass().isAssignableFrom(InterruptableJob.class)) scheduler.interrupt(jobKey);
        scheduler.deleteJob(jobKey);


    }

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
        return new HashSet<>(schedulers);
    }

    @Override
    public Scheduler getScheduler(String name) throws SchedulerException {
        for (Scheduler schedulerService : schedulers) {
            if (schedulerService.getSchedulerName().equals(name)) {
                return schedulerService;
            }
        }
        throw new SchedulerException("Unknown scheduler: " + name);

    }

    @Override
    public Set<String> getSchedulerNames() throws SchedulerException {
        HashSet<String> names = new HashSet<>();
        for (Scheduler scheduler : schedulers) {
            names.add(scheduler.getSchedulerName());
        }
        return names;
    }

    @Override
    public String runJob(String schedulerName, Class<? extends AbstractJobProvider> jobClass, JobDataMap jobDataMap, String jobGroup) throws SchedulerException {

        return runJob(schedulerName, jobClass, jobDataMap, jobGroup, new Date());

    }

    @Override
    public String runJob(String schedulerName, Class<? extends AbstractJobProvider> jobClass, JobDataMap jobDataMap, String jobGroup, int inMinutes) throws SchedulerException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, inMinutes);
        return runJob(schedulerName, jobClass, jobDataMap, jobGroup, calendar.getTime());
    }

    @Override
    public String runJob(String schedulerName, Class<? extends AbstractJobProvider> jobClass, JobDataMap jobDataMap, String jobGroup, Date triggerDate) throws SchedulerException {
        Scheduler scheduler = getScheduler(schedulerName);

        String id = UUID.randomUUID().toString();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(id, jobGroup)
                .setJobData((jobDataMap == null ? new JobDataMap() : jobDataMap))
                .build();

        Trigger jobTrigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(id, jobGroup)
                .startAt(triggerDate)
                .build();

        scheduler.scheduleJob(jobDetail, jobTrigger);

        return id;
    }

    @Override
    public void scheduleJob(String schedulerName, Class<? extends AbstractJobProvider> jobClass, JobDataMap jobDataMap, JobKey jobKey, ScheduleBuilder scheduleBuilder) throws SchedulerException {
        if (schedulerName == null) throw new IllegalArgumentException("Must give scheduler name");
        if (jobKey == null) throw new IllegalArgumentException("Must give jobKey");
        if (scheduleBuilder == null) throw new IllegalArgumentException("Must give Scheduler Builder");

        Scheduler scheduler = getScheduler(schedulerName);
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .setJobData((jobDataMap == null ? new JobDataMap() : jobDataMap))
                .build();

        Trigger jobTrigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobKey.getName(), jobKey.getGroup())
                .withSchedule(scheduleBuilder)
                .build();

        scheduler.scheduleJob(jobDetail, jobTrigger);

    }

    @Override
    public boolean isJobRunning(Class<? extends AbstractJobProvider> jobClass) throws SchedulerException {
        return isJobRunning(jobClass, null);
    }

    @Override
    public boolean isJobRunning(Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        return isJobRunning(jobClass, requiredMatchingJobDataMapEntries, null);
    }

    @Override
    public boolean isJobRunning(Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException {

        for (Scheduler scheduler : getSchedulers()) {
            if (isJobRunning(scheduler.getSchedulerName(), jobClass, requiredMatchingJobDataMapEntries, ignoreThisInstance)) return true;
        }

        return false;
    }

    @Override
    public boolean isJobRunning(String schedulerName, Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries, Job ignoreThisInstance) throws SchedulerException {
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
    public boolean isWaitingOrRunning(Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
        for (Scheduler scheduler : getSchedulers()) {
            if (isWaitingOrRunning(scheduler.getSchedulerName(), jobClass, requiredMatchingJobDataMapEntries)) return true;
        }

        return false;
    }

    @Override
    public boolean isWaitingOrRunning(String schedulerName, Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
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
    public void deleteJobs(Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
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
    public void interruptJobs(Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
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
    public void deleteAndInterruptJob(Class<? extends AbstractJobProvider> jobClass, JobDataMap requiredMatchingJobDataMapEntries) throws SchedulerException {
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

    @Override
    public void interruptThread(String schedulerName, JobKey jobKey) throws SchedulerException {
        JobExecutionContext context = getRunningJob(schedulerName, jobKey);
        if (context.getJobInstance() instanceof JobService) {
            ((JobService) context.getJobInstance()).getExecuteThread().interrupt();
        }
        else {
            throw new SchedulerException("Job is not instance of JobService");
        }
    }
}
