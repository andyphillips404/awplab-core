
package com.awplab.core.scheduler.service;

import org.quartz.*;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.DefaultThreadExecutor;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/23/15.
 */

/**
 * Base abstract class for custom schedulers used in the library.   This class is a wrapper scheduler,
 * lifted from StdSchedulerImpl of the Quartz library and modified to allow for better external configuration
 * of the scheduler.
 */
public abstract class AbstractSchedulerProvider implements Scheduler {

    /**
     * Called when the scheduler is created to return a QuartzSchedulerResources that
     * is used to configure the scheduler.  This is called during scheduler creation only.
     *
     * The Quartz scheduler resource should have following set, at a minimum, to create a scheduler:
     * <ul>
     *     <li>Name of the scheduler (setName)</li>
     *     <li>Instance name (setInstanceName)</li>
     *     <li>Job run shell factory (setJobRunShellFactory)</li>
     *     <li>Thread executor (setThreadExecutor)</li>
     *     <li>Job store (setJobStore)</li>
     *     <li>Thread pool (setThreadPool)</li>
     * </ul>
     *
     * When the scheduler is created in the manager, the thread pool and job store will
     * be initialized, so no need to do so in this function.
     *
     * Below is an example of creating and returning a scheduler resource:
     * {@code
     *  if (qsr == null) {
     *          qsr = new QuartzSchedulerResources();
     *          qsr.setName(name);
     *          qsr.setJobRunShellFactory(new IPOJOJobRunShellFactory());
     *          qsr.setThreadExecutor(new DefaultThreadExecutor());
     *          qsr.setBatchTimeWindow(0l);
     *          qsr.setInstanceId(name + "_" + UUID.randomUUID().toString());
     *          qsr.setInterruptJobsOnShutdown(true);
     *          qsr.setInterruptJobsOnShutdownWithWait(true);
     *          qsr.setMaxBatchSize(1);
     *          qsr.setJobStore(new RAMJobStore());
     *          threadPool = new ResizableThreadPool(initialThreadPoolSize, threadPriority);
     *          qsr.setThreadPool(threadPool);
     *  }
     *  return qsr;
     * }
     *
     * @see #getInitialJobFactory()
     * @see #getJobStoreClassLoadHelper()
     * @return QuartzSchedulerResource scheduler configuration
     */
    protected abstract QuartzSchedulerResources getQuartzSchedulerResources();

    /**
     * Called during scheduler creation to return the initial job factory that should be
     * assigned during scheduler creation.
     *
     * @return JobFactory used during scheduler creation
     */
    protected abstract JobFactory getInitialJobFactory();

    /**
     * Get scheduler default idle wait time used during scheduler creation
     *
     * @return default idle wait time, defaults to -1
     */
    protected int getDefaultIdleWaitTime() {
        return -1;
    }

    /**
     * Gets the job store class load helper used during scheduler scheduler creation to
     * set up the job store that is assigned in the S
     * @return
     */
    protected ClassLoadHelper getJobStoreClassLoadHelper() {
        return new CascadingClassLoadHelper();
    }

    private QuartzScheduler scheduler = null;

    /**
     * Returns true or false if the scheduler has been created.
     * @return true if the scheduler has been created, false if not
     */
    public boolean isSchedulerCreated() {
        return !(scheduler == null);
    }


    private QuartzScheduler getScheduler() {

        Logger logger = LoggerFactory.getLogger(this.getClass());
        if (scheduler == null) {
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

                qs.setJobFactory(getInitialJobFactory());

                logger.info("Quartz scheduler '" + qsr.getName());
                logger.info("Quartz scheduler version: " + qs.getVersion());

                scheduler = qs;

            } catch (Exception ex) {
                logger.error("Exception creating scheduler!", ex);
            }
        }

        return scheduler;
    }



    @Override
    public String getSchedulerName() {
        return getScheduler().getSchedulerName();
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public String getSchedulerInstanceId() {
        return getScheduler().getSchedulerInstanceId();
    }



    @Override
    public SchedulerMetaData getMetaData() {
        return new SchedulerMetaData(getSchedulerName(),
                getSchedulerInstanceId(), getClass(), false, isStarted(),
                isInStandbyMode(), isShutdown(), getScheduler().runningSince(),
                getScheduler().numJobsExecuted(), getScheduler().getJobStoreClass(),
                getScheduler().supportsPersistence(), getScheduler().isClustered(), getScheduler().getThreadPoolClass(),
                getScheduler().getThreadPoolSize(), getScheduler().getVersion());

    }

    /**
     * <p>
     * Returns the <code>SchedulerContext</code> of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public SchedulerContext getContext() throws SchedulerException {
        return getScheduler().getSchedulerContext();
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
        getScheduler().start();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void startDelayed(int seconds) throws SchedulerException {
        getScheduler().startDelayed(seconds);
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void standby() {
        getScheduler().standby();
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
        return (getScheduler().runningSince() != null);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isInStandbyMode() {
        return getScheduler().isInStandbyMode();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown() {
        getScheduler().shutdown();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        getScheduler().shutdown(waitForJobsToComplete);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isShutdown() {
        return getScheduler().isShutdown();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<JobExecutionContext> getCurrentlyExecutingJobs() {
        return getScheduler().getCurrentlyExecutingJobs();
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
        getScheduler().clear();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger)
            throws SchedulerException {
        return getScheduler().scheduleJob(jobDetail, trigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        return getScheduler().scheduleJob(trigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addJob(JobDetail jobDetail, boolean replace)
            throws SchedulerException {
        getScheduler().addJob(jobDetail, replace);
    }

    @Override
    public void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling)
            throws SchedulerException {
        getScheduler().addJob(jobDetail, replace, storeNonDurableWhileAwaitingScheduling);
    }


    @Override
    public boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException {
        return getScheduler().deleteJobs(jobKeys);
    }

    @Override
    public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException {
        getScheduler().scheduleJobs(triggersAndJobs, replace);
    }

    @Override
    public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException {
        getScheduler().scheduleJob(jobDetail,  triggersForJob, replace);
    }

    @Override
    public boolean unscheduleJobs(List<TriggerKey> triggerKeys)
            throws SchedulerException {
        return getScheduler().unscheduleJobs(triggerKeys);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteJob(JobKey jobKey)
            throws SchedulerException {
        return getScheduler().deleteJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean unscheduleJob(TriggerKey triggerKey)
            throws SchedulerException {
        return getScheduler().unscheduleJob(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date rescheduleJob(TriggerKey triggerKey,
                              Trigger newTrigger) throws SchedulerException {
        return getScheduler().rescheduleJob(triggerKey, newTrigger);
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
        getScheduler().triggerJob(jobKey, data);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        getScheduler().pauseTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        getScheduler().pauseTriggers(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJob(JobKey jobKey)
            throws SchedulerException {
        getScheduler().pauseJob(jobKey);
    }

    /**
     * @see Scheduler#getPausedTriggerGroups()
     */
    @Override
    public Set<String> getPausedTriggerGroups() throws SchedulerException {
        return getScheduler().getPausedTriggerGroups();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        getScheduler().pauseJobs(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        getScheduler().resumeTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        getScheduler().resumeTriggers(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJob(JobKey jobKey)
            throws SchedulerException {
        getScheduler().resumeJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        getScheduler().resumeJobs(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseAll() throws SchedulerException {
        getScheduler().pauseAll();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeAll() throws SchedulerException {
        getScheduler().resumeAll();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getJobGroupNames() throws SchedulerException {
        return getScheduler().getJobGroupNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<? extends Trigger> getTriggersOfJob(JobKey jobKey)
            throws SchedulerException {
        return getScheduler().getTriggersOfJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
        return getScheduler().getJobKeys(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getTriggerGroupNames() throws SchedulerException {
        return getScheduler().getTriggerGroupNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        return getScheduler().getTriggerKeys(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public JobDetail getJobDetail(JobKey jobKey)
            throws SchedulerException {
        return getScheduler().getJobDetail(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Trigger getTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        return getScheduler().getTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Trigger.TriggerState getTriggerState(TriggerKey triggerKey)
            throws SchedulerException {
        return getScheduler().getTriggerState(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers)
            throws SchedulerException {
        getScheduler().addCalendar(calName, calendar, replace, updateTriggers);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteCalendar(String calName) throws SchedulerException {
        return getScheduler().deleteCalendar(calName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Calendar getCalendar(String calName) throws SchedulerException {
        return getScheduler().getCalendar(calName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getCalendarNames() throws SchedulerException {
        return getScheduler().getCalendarNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(JobKey jobKey) throws SchedulerException {
        return getScheduler().checkExists(jobKey);
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(TriggerKey triggerKey) throws SchedulerException {
        return getScheduler().checkExists(triggerKey);
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
        getScheduler().setJobFactory(factory);
    }

    /**
     * @see Scheduler#getListenerManager()
     */
    @Override
    public ListenerManager getListenerManager() throws SchedulerException {
        return getScheduler().getListenerManager();
    }

    @Override
    public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
        return getScheduler().interrupt(jobKey);
    }

    @Override
    public boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException {
        return getScheduler().interrupt(fireInstanceId);
    }



}


