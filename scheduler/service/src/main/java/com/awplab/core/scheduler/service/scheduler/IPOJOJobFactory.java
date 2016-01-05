package com.awplab.core.scheduler.service.scheduler;

import org.apache.felix.ipojo.InstanceManager;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by andyphillips404 on 12/22/15.
 */
public class IPOJOJobFactory implements JobFactory {


    public static final String INSTANCE_MANAGER_KEY = "com.awplab.core.scheduler.service.scheduler.instanceManager";

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Logger logger = LoggerFactory.getLogger(IPOJOJobFactory.class);

        JobDetail jobDetail = bundle.getJobDetail();

        InstanceManager instanceManager = (InstanceManager) bundle.getTrigger().getJobDataMap().get(INSTANCE_MANAGER_KEY);
        if (instanceManager == null) {
            try {
                logger.info("Creating instance of Job '" + jobDetail.getKey() + "', from class=" + jobDetail.getJobClass().getName());
                return jobDetail.getJobClass().newInstance();
            } catch (Exception e) {
                throw new SchedulerException("Problem instantiating from class '" + jobDetail.getJobClass().getName() + "'", e);
            }
        }
        try {
            logger.info("Creating instance of Job '" + jobDetail.getKey() + "', from factory=" + instanceManager.getFactory().getFactoryName());
            return (Job) instanceManager.getPojoObject();
        } catch (Exception e) {
            throw new SchedulerException("Problem instantiating from factory '" + instanceManager.getFactory().getName() + "'", e);
        }


    }
}
