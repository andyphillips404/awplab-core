package com.awplab.core.scheduler.command;

import com.awplab.core.scheduler.service.SchedulerManagerService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.quartz.JobKey;

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "interrupt-thread")
@Service
public class InterruptThreadCommand implements Action {

    @Reference
    private SchedulerManagerService schedulerManagerService;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to show", required = true, multiValued = false)
    String schedulerName = null;

    @Argument(index = 1, name = "job key name", description = "Job Key: name", required = true, multiValued = false)
    String jobKeyName = null;

    @Argument(index = 2, name = "job key group", description = "Job Key: group", required = true, multiValued = false)
    String jobKeyGroup = null;

    @Override
    public Object execute() throws Exception {

        JobKey jobKey = new JobKey(jobKeyName, jobKeyGroup);

        schedulerManagerService.interruptThread(schedulerName, jobKey);

        return null;


    }
}
