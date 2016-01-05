package com.awplab.core.test;

import com.awplab.core.scheduler.service.SchedulerManagerService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Created by andyphillips404 on 12/17/15.
 */

@Command(scope = "test", name = "test")
@Service
public class TestCommand implements Action {

    @Reference
    SchedulerManagerService schedulerManagerService;

    @Override
    public Object execute() throws Exception {

        return schedulerManagerService.runJob("SHIT", TestAbstractJob1.class, null, "TEST");
    }
}
