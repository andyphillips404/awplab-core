package com.awplab.core.scheduler.admin;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Created by andyphillips404 on 8/31/16.
 */
public abstract class AbstractSchedulerBean {

    public abstract String getScheduler();

    public abstract JobDetail getJobDetail();

    public String getJobKeyName() {
        return getJobDetail().getKey().getName();
    }

    public String getJobKeyGroup() {
        return getJobDetail().getKey().getGroup();
    }

    public String getJobClass() {
        return getJobDetail().getJobClass().getName();
    }


    public String getBundleName() {
        Bundle bundle = FrameworkUtil.getBundle(getJobDetail().getJobClass());
        return bundle.getSymbolicName();
    }

    public Version getBundleVersion() {
        Bundle bundle = FrameworkUtil.getBundle(getJobDetail().getJobClass());
        return bundle.getVersion();
    }

    public JobDataMap getJobDataMap() {
        return getJobDetail().getJobDataMap();
    }


}
