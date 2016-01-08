package com.awplab.core.scheduler.service;

import org.quartz.Job;

/**
 * Created by andyphillips404 on 2/26/15.
 */
public interface StatusJob extends Job {
    /**
     * Implementation of this interface allows for a standard way for a status to be returned from the job while
     * executing.
     *
     * @return Object representing the status of the executing job.   Can be a custom class.
     */
    Object getJobStatus();
}
