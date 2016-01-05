package com.awplab.core.scheduler.service.events;

/**
 * Created by andyphillips404 on 3/8/15.
 */
public final class SchedulerEventData {
    private SchedulerEventData() {
    }

    public static final String SCHEDULER =  "Scheduler";
    public static final String TRIGGER =  "Trigger";
    public static final String JOB_EXECUTION_CONTEXT =  "JobExecutionContext";
    public static final String JOB_EXECUTION_EXCEPTION =  "JobExecutionException";
    public static final String JOB_DETAIL =  "JobDetail";
    public static final String JOB_KEY =  "JobKey";
    public static final String MESSAGE =  "Message";
    public static final String SCHEDULER_EXCEPTION =  "SchedulerException";
    public static final String JOB_GROUP =  "JobGroup";
    public static final String TRIGGER_KEY =  "TriggerKey";
    public static final String TRIGGER_GROUP =  "TriggerGroup";
    public static final String TRIGGER_INSTRUCTION_CODE = "triggerInstructionCode";

}
