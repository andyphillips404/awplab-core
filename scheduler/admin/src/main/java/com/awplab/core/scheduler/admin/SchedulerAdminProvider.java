package com.awplab.core.scheduler.admin;

import com.awplab.core.admin.AdminProvider;
import com.awplab.core.admin.IPOJOAdminProvider;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

/**
 * Created by andyphillips404 on 8/30/16.
 */
@Instantiate
@Component(immediate = true)
@Provides(specifications = {AdminProvider.class})
public class SchedulerAdminProvider extends IPOJOAdminProvider<SchedulerAdminViewProvider> {

    @Override
    public Class<SchedulerAdminViewProvider> getAdminViewProviderClass() {
        return SchedulerAdminViewProvider.class;
    }

    @Override
    public void viewProviderCreated(SchedulerAdminViewProvider viewProvider) {

    }
}
