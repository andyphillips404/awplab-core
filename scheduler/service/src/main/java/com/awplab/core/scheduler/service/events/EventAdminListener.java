package com.awplab.core.scheduler.service.events;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.quartz.*;

import java.util.*;

/**
 * Created by andyphillips404 on 1/4/16.
 */
public class EventAdminListener implements SchedulerListener, JobListener, TriggerListener {

    private Scheduler scheduler = null;
    private String name = null;

    public EventAdminListener(Scheduler scheduler, String name) {
        this.scheduler = scheduler;
        this.name = name;
    }



    @Override
    public String getName() {
        return name;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.JOB_EXECUTION_CONTEXT, context);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_TO_BE_EXECUTED, params);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.JOB_EXECUTION_CONTEXT, context);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_EXECUTION_VETOED, params);

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.JOB_EXECUTION_CONTEXT, context);
        params.put(SchedulerEventData.JOB_EXECUTION_EXCEPTION, jobException);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_WAS_EXECUTED, params);
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.TRIGGER, trigger);
        params.put(SchedulerEventData.JOB_EXECUTION_CONTEXT, context);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGER_FIRED, params);

    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.TRIGGER, trigger);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGER_MIS_FIRED, params);
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.TRIGGER, trigger);
        params.put(SchedulerEventData.JOB_EXECUTION_CONTEXT, context);
        params.put(SchedulerEventData.TRIGGER_INSTRUCTION_CODE, triggerInstructionCode);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGER_COMPLETE, params);
    }

    @Override
    public void jobScheduled(Trigger trigger) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_SCHEDULED, Collections.singletonMap(SchedulerEventData.TRIGGER, trigger));
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_UNSCHEDULED, Collections.singletonMap(SchedulerEventData.TRIGGER_KEY, triggerKey));
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGER_FINALIZED, Collections.singletonMap(SchedulerEventData.TRIGGER, trigger));
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGER_PAUSED, Collections.singletonMap(SchedulerEventData.TRIGGER_KEY, triggerKey));
    }

    @Override
    public void triggersPaused(String triggerGroup) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGERS_PAUSED, Collections.singletonMap(SchedulerEventData.TRIGGER_GROUP, triggerGroup));

    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGER_RESUMED, Collections.singletonMap(SchedulerEventData.TRIGGER_KEY, triggerKey));

    }

    @Override
    public void triggersResumed(String triggerGroup) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.TRIGGERS_RESUMED, Collections.singletonMap(SchedulerEventData.TRIGGER_GROUP, triggerGroup));

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_ADDED, Collections.singletonMap(SchedulerEventData.JOB_DETAIL, jobDetail));
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_DELETED, Collections.singletonMap(SchedulerEventData.JOB_KEY, jobKey));

    }

    @Override
    public void jobPaused(JobKey jobKey) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_PAUSED, Collections.singletonMap(SchedulerEventData.JOB_KEY, jobKey));

    }

    @Override
    public void jobsPaused(String jobGroup) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOBS_PAUSED, Collections.singletonMap(SchedulerEventData.JOB_GROUP, jobGroup));

    }

    @Override
    public void jobResumed(JobKey jobKey) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOB_RESUMED, Collections.singletonMap(SchedulerEventData.JOB_KEY, jobKey));

    }

    @Override
    public void jobsResumed(String jobGroup) {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.JOBS_RESUMED, Collections.singletonMap(SchedulerEventData.JOB_GROUP, jobGroup));

    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        Map<String, Object> params = new HashMap<>();
        params.put(SchedulerEventData.MESSAGE, msg);
        params.put(SchedulerEventData.SCHEDULER_EXCEPTION, cause);
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_ERROR, params);
    }

    @Override
    public void schedulerInStandbyMode() {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_IN_STANDBY_MODE);
    }

    @Override
    public void schedulerStarted() {

        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_STARTED);
    }

    @Override
    public void schedulerStarting() {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_STARTING);

    }

    @Override
    public void schedulerShutdown() {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_SHUTDOWN);

    }

    @Override
    public void schedulerShuttingdown() {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULER_SHUTTING_DOWN);

    }

    @Override
    public void schedulingDataCleared() {
        SchedulerEventTopics.postEvent(scheduler, SchedulerEventTopics.SCHEDULING_DATA_CLEARED);

    }
}
