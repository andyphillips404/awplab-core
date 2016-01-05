package com.awplab.core.test;

import com.awplab.core.scheduler.service.AbstractJobProvider;
import org.apache.felix.ipojo.annotations.Component;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by andyphillips404 on 12/17/15.
 */
@Component
@ProvidesJob
public class TestAbstractJob1 extends AbstractJobProvider {

    @Override
    public void cancelableExecute(JobExecutionContext context) throws JobExecutionException {
        while (!isCancelRequested()) {
            try {
                Thread.sleep(1000);
                //System.out.print("Hello: " + context.getJobDetail().getKey());
            }
            catch (Exception ignored) {

            }
        }
    }

    @Override
    public Object getJobStatus() {
        return "Running";
    }
}
