package com.awplab.core.scheduler.command;


import com.awplab.core.scheduler.service.SchedulerManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "delete-all")
@Service
public class DeleteAllCommand implements Action {

    @Reference
    private SchedulerManager schedulerManager;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to clear all, null to clear all jobs", required = false, multiValued = false)
    String schedulerName = null;

    @Override
    public Object execute() throws Exception {
        if (schedulerName == null) schedulerManager.deleteJobs();
        else schedulerManager.deleteJobs(schedulerName);
        return null;
    }

}
