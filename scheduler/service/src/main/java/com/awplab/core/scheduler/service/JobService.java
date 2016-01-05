package com.awplab.core.scheduler.service;

import org.quartz.InterruptableJob;

/**
 * Created by andyphillips404 on 2/26/15.
 */
public interface JobService extends InterruptableJob {
    Object getJobStatus();
    Thread getExecuteThread();
}
