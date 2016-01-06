package com.awplab.core.scheduler.service;

import org.quartz.Job;

/**
 * Created by andyphillips404 on 2/26/15.
 */
public interface StatusJob extends Job {
    Object getJobStatus();
}
