package com.awplab.core.scheduler.command;

import com.awplab.core.scheduler.service.SchedulerManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by andyphillips404 on 2/24/15.
 */
@Command(scope = "scheduler", name = "jobs")
@Service
public class JobsCommand implements Action {


    @Reference
    private SchedulerManagerService schedulerManagerService;

    @Argument(index = 0, name = "scheduler", description = "scheduler name to show", required = false, multiValued = false)
    String schedulerName = null;

    @Override
    public Object execute() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        for (Scheduler scheduler : schedulerManagerService.getSchedulers()) {

            if (schedulerName != null && !scheduler.getSchedulerName().equals(schedulerName)) continue;

            //String localSchedulerName = schedulerManagerService.getSchedulerName(scheduler);

            String line = scheduler.getSchedulerName();
            System.out.println(line);
            System.out.println(new String(new char[line.length()]).replace("\0", "-"));

            for (String group : scheduler.getJobGroupNames()) {
                // enumerate each job in group


                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {

                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                    ArrayList<Trigger> triggers = new ArrayList<Trigger>(scheduler.getTriggersOfJob(jobKey));
                    Collections.sort(triggers, new Comparator<Trigger>() {
                        @Override
                        public int compare(Trigger o1, Trigger o2) {
                            return o2.getNextFireTime().compareTo(o1.getNextFireTime());
                        }
                    });
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/d/yy h:m");

                    System.out.println("Next Trigger: " + (triggers.size() > 0 && triggers.get(0).getNextFireTime() != null ? simpleDateFormat.format(triggers.get(0).getNextFireTime()) : ""));
                    System.out.println("Job Key Name: " + jobKey.getName());
                    System.out.println("Job Key Group: " + jobKey.getGroup());
                    System.out.println("Job Class: " + jobDetail.getJobClass().getName());
                    System.out.println("Running: " + (schedulerManagerService.getRunningJob(scheduler.getSchedulerName(), jobKey) != null ? "Yes" : "No"));
                    System.out.println("Interruptable: " + (jobDetail.getJobClass().isAssignableFrom(InterruptableJob.class) ? "Yes" : "No"));
                    System.out.println("Job Data Map:");
                    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jobDetail.getJobDataMap()));
                    System.out.println();
                }
            }

        }

        return null;


    }
}
