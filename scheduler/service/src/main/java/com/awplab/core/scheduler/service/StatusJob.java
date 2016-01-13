package com.awplab.core.scheduler.service;

import org.quartz.Job;

/**
 * Implementation interface allows for a standard way for a job to return a status during execution.
 *
 * The getJobStatus() will be called when status of the execution is requested.  For the command line and rest interface, this object
 * is serialized using Jackson JSON format.
 *
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
