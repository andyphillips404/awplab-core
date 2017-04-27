package com.awplab.core.scheduler.admin;

import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.util.Date;
import java.util.List;

/**
 * Created by andyphillips404 on 8/30/16.
 */
public class SchedulerJobDetailTriggers extends AbstractSchedulerBean {
    private String scheduler;
    private JobDetail jobDetail;
    private List<? extends Trigger> triggers;

    @Override
    public String getScheduler() {
        return scheduler;
    }

    public Date getNextFireTime() {
        return triggers.get(0).getNextFireTime();
    }

    @Override
    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public List<? extends Trigger> getTriggers() {
        return triggers;
    }

    public SchedulerJobDetailTriggers(String scheduler, JobDetail jobDetail, List<? extends Trigger> triggers) {
        this.scheduler = scheduler;
        this.jobDetail = jobDetail;
        this.triggers = triggers;
        triggers.sort((o1, o2) -> {return o1.getNextFireTime().compareTo(o2.getNextFireTime());});
    }


}
