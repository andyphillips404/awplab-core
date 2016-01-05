
package com.awplab.core.scheduler.service;

import org.quartz.Calendar;
import org.quartz.*;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.DefaultThreadExecutor;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by andyphillips404 on 12/23/15.
 */
public abstract class AbstractSchedulerProvider implements Scheduler {

    public abstract QuartzSchedulerResources getQuartzSchedulerResources();

    public abstract JobFactory getJobFactory();

    protected int getDefaultIdleWaitTime() {
        return -1;
    }

    protected ClassLoadHelper getJobStoreClassLoadHelper() {
        return new CascadingClassLoadHelper();
    }

    private QuartzScheduler sched = null;

    protected boolean isSchedulerCreated() {
        return !(sched == null);
    }

    // lazy instantiate the QuartzScheduler....
    protected QuartzScheduler getSched() {
        Logger logger = LoggerFactory.getLogger(AbstractSchedulerProvider.class);
        if (sched == null) {
            try {
                QuartzSchedulerResources qsr = getQuartzSchedulerResources();
                if (qsr.getThreadExecutor() == null) {
                    qsr.setThreadExecutor(new DefaultThreadExecutor());
                }

                qsr.getThreadPool().initialize();

                QuartzScheduler qs = new QuartzScheduler(qsr, getDefaultIdleWaitTime(), -1);

                ClassLoadHelper classLoadHelper = getJobStoreClassLoadHelper();
                classLoadHelper.initialize();

                qsr.getJobStore().initialize(classLoadHelper, qs.getSchedulerSignaler());

                qsr.getJobRunShellFactory().initialize(this);

                qs.initialize();

                qs.setJobFactory(getJobFactory());

                logger.info("Quartz scheduler '" + qsr.getName());
                logger.info("Quartz scheduler version: " + qs.getVersion());

                sched = qs;

            } catch (Exception ex) {
                logger.error("Exception creating scheduler!", ex);
            }
        }

        return sched;
    }



    @Override
    public String getSchedulerName() {
        return getSched().getSchedulerName();
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public String getSchedulerInstanceId() {
        return getSched().getSchedulerInstanceId();
    }



    @Override
    public SchedulerMetaData getMetaData() {
        return new SchedulerMetaData(getSchedulerName(),
                getSchedulerInstanceId(), getClass(), false, isStarted(),
                isInStandbyMode(), isShutdown(), getSched().runningSince(),
                getSched().numJobsExecuted(), getSched().getJobStoreClass(),
                getSched().supportsPersistence(), getSched().isClustered(), getSched().getThreadPoolClass(),
                getSched().getThreadPoolSize(), getSched().getVersion());

    }

    /**
     * <p>
     * Returns the <code>SchedulerContext</code> of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public SchedulerContext getContext() throws SchedulerException {
        return getSched().getSchedulerContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Schedululer State Management Methods
    ///
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void start() throws SchedulerException {
        getSched().start();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void startDelayed(int seconds) throws SchedulerException {
        getSched().startDelayed(seconds);
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void standby() {
        getSched().standby();
    }

    /**
     * Whether the scheduler has been started.
     *
     * <p>
     * Note: This only reflects whether <code>{@link #start()}</code> has ever
     * been called on this Scheduler, so it will return <code>true</code> even
     * if the <code>Scheduler</code> is currently in standby mode or has been
     * since shutdown.
     * </p>
     *
     * @see #start()
     * @see #isShutdown()
     * @see #isInStandbyMode()
     */
    @Override
    public boolean isStarted() {
        return (getSched().runningSince() != null);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isInStandbyMode() {
        return getSched().isInStandbyMode();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown() {
        getSched().shutdown();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        getSched().shutdown(waitForJobsToComplete);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isShutdown() {
        return getSched().isShutdown();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<JobExecutionContext> getCurrentlyExecutingJobs() {
        return getSched().getCurrentlyExecutingJobs();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Scheduling-related Methods
    ///
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void clear() throws SchedulerException {
        getSched().clear();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger)
            throws SchedulerException {
        return getSched().scheduleJob(jobDetail, trigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        return getSched().scheduleJob(trigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addJob(JobDetail jobDetail, boolean replace)
            throws SchedulerException {
        getSched().addJob(jobDetail, replace);
    }

    @Override
    public void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling)
            throws SchedulerException {
        getSched().addJob(jobDetail, replace, storeNonDurableWhileAwaitingScheduling);
    }


    @Override
    public boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException {
        return getSched().deleteJobs(jobKeys);
    }

    @Override
    public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException {
        getSched().scheduleJobs(triggersAndJobs, replace);
    }

    @Override
    public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException {
        getSched().scheduleJob(jobDetail,  triggersForJob, replace);
    }

    @Override
    public boolean unscheduleJobs(List<TriggerKey> triggerKeys)
            throws SchedulerException {
        return getSched().unscheduleJobs(triggerKeys);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteJob(JobKey jobKey)
            throws SchedulerException {
        return getSched().deleteJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean unscheduleJob(TriggerKey triggerKey)
            throws SchedulerException {
        return getSched().unscheduleJob(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date rescheduleJob(TriggerKey triggerKey,
                              Trigger newTrigger) throws SchedulerException {
        return getSched().rescheduleJob(triggerKey, newTrigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void triggerJob(JobKey jobKey)
            throws SchedulerException {
        triggerJob(jobKey, null);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void triggerJob(JobKey jobKey, JobDataMap data)
            throws SchedulerException {
        getSched().triggerJob(jobKey, data);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        getSched().pauseTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        getSched().pauseTriggers(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJob(JobKey jobKey)
            throws SchedulerException {
        getSched().pauseJob(jobKey);
    }

    /**
     * @see Scheduler#getPausedTriggerGroups()
     */
    @Override
    public Set<String> getPausedTriggerGroups() throws SchedulerException {
        return getSched().getPausedTriggerGroups();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        getSched().pauseJobs(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        getSched().resumeTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        getSched().resumeTriggers(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJob(JobKey jobKey)
            throws SchedulerException {
        getSched().resumeJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        getSched().resumeJobs(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseAll() throws SchedulerException {
        getSched().pauseAll();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeAll() throws SchedulerException {
        getSched().resumeAll();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getJobGroupNames() throws SchedulerException {
        return getSched().getJobGroupNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<? extends Trigger> getTriggersOfJob(JobKey jobKey)
            throws SchedulerException {
        return getSched().getTriggersOfJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
        return getSched().getJobKeys(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getTriggerGroupNames() throws SchedulerException {
        return getSched().getTriggerGroupNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        return getSched().getTriggerKeys(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public JobDetail getJobDetail(JobKey jobKey)
            throws SchedulerException {
        return getSched().getJobDetail(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Trigger getTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        return getSched().getTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Trigger.TriggerState getTriggerState(TriggerKey triggerKey)
            throws SchedulerException {
        return getSched().getTriggerState(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers)
            throws SchedulerException {
        getSched().addCalendar(calName, calendar, replace, updateTriggers);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteCalendar(String calName) throws SchedulerException {
        return getSched().deleteCalendar(calName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Calendar getCalendar(String calName) throws SchedulerException {
        return getSched().getCalendar(calName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getCalendarNames() throws SchedulerException {
        return getSched().getCalendarNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(JobKey jobKey) throws SchedulerException {
        return getSched().checkExists(jobKey);
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(TriggerKey triggerKey) throws SchedulerException {
        return getSched().checkExists(triggerKey);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Other Methods
    ///
    ///////////////////////////////////////////////////////////////////////////


    /**
     * @see Scheduler#setJobFactory(JobFactory)
     */
    @Override
    public void setJobFactory(JobFactory factory) throws SchedulerException {
        getSched().setJobFactory(factory);
    }

    /**
     * @see Scheduler#getListenerManager()
     */
    @Override
    public ListenerManager getListenerManager() throws SchedulerException {
        return getSched().getListenerManager();
    }

    @Override
    public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
        return getSched().interrupt(jobKey);
    }

    @Override
    public boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException {
        return getSched().interrupt(fireInstanceId);
    }



}


