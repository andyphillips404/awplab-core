package com.awplab.core.scheduler.command;


import com.awplab.core.scheduler.service.SchedulerManagerService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "interrupt-all")
@Service
public class InterruptAllCommand implements Action {

    @Reference
    private SchedulerManagerService schedulerManagerService;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to interrupt all on", required = false, multiValued = false)
    String schedulerName = null;

    @Override
    public Object execute() throws Exception {
        if (schedulerName == null) schedulerManagerService.interruptJobs();
        else schedulerManagerService.interruptJobs(schedulerName);
        return null;
    }
}
