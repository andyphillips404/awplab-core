package com.awplab.core.scheduler.command;


import com.awplab.core.scheduler.service.JobService;
import com.awplab.core.scheduler.service.SchedulerManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "running")
@Service
public class RunningCommand implements Action {

    @Reference
    private SchedulerManagerService schedulerManagerService;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to show", required = false, multiValued = false)
    String schedulerName = null;

    @Override
    public Object execute() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        for (Scheduler scheduler : schedulerManagerService.getSchedulers()) {

            if (schedulerName != null && !scheduler.getSchedulerName().equals(schedulerName)) continue;

            for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
                String line = scheduler.getSchedulerName() + " - " + context.getJobInstance().getClass().getName();
                System.out.println(line);
                System.out.println(new String(new char[line.length()]).replace("\0", "-"));
                System.out.println("Job Key Name: " + context.getJobDetail().getKey().getName());
                System.out.println("Job Key Group: " + context.getJobDetail().getKey().getGroup());
                System.out.println("Interruptable: " + (context.getJobInstance() instanceof InterruptableJob ? "Yes" : "No"));
                System.out.println("Thread State: " + (context.getJobInstance() instanceof JobService ? ((JobService) context.getJobInstance()).getExecuteThread().getState().toString() : "N/A"));
                line = "Job Data";
                System.out.println(line);
                System.out.println(new String(new char[line.length()]).replace("\0", "-"));
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context.getJobDetail().getJobDataMap()));
                if (context.getJobInstance() instanceof JobService) {
                    line = "Job Status";
                    System.out.println(line);
                    System.out.println(new String(new char[line.length()]).replace("\0", "-"));
                    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(((JobService) context.getJobInstance()).getJobStatus()));
                }
                System.out.println();
            }
        }
        return null;

    }
}
