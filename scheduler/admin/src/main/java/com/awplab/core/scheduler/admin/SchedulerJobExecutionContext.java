package com.awplab.core.scheduler.admin;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.Date;

/**
 * Created by andyphillips404 on 8/30/16.
 */
public class SchedulerJobExecutionContext extends AbstractSchedulerBean {
    private String scheduler;

    private JobExecutionContext jobExecutionContext;

    @Override
    public String getScheduler() {
        return scheduler;
    }

    public Date getFireTime() {
        return jobExecutionContext.getFireTime();
    }

    public String getFireInstanceId() {
        return jobExecutionContext.getFireInstanceId();
    }


    public JobDataMap getJobDataMap() {
        return jobExecutionContext.getMergedJobDataMap();
    }

    public JobExecutionContext getJobExecutionContext() {
        return jobExecutionContext;
    }

    @Override
    public JobDetail getJobDetail() {
        return jobExecutionContext.getJobDetail();
    }

    public SchedulerJobExecutionContext(String scheduler, JobExecutionContext jobExecutionContext) {
        this.scheduler = scheduler;
        this.jobExecutionContext = jobExecutionContext;
    }
}
