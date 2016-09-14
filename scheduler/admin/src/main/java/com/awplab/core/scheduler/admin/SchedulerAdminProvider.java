package com.awplab.core.scheduler.admin;

import com.awplab.core.admin.AdminProvider;
import com.awplab.core.admin.AdminViewProvider;
import com.awplab.core.admin.IPOJOAdminProvider;
import com.awplab.core.scheduler.service.AbstractStatusInterruptableJob;
import com.awplab.core.scheduler.service.SchedulerManager;
import com.awplab.core.scheduler.service.StatusJob;
import com.awplab.core.scheduler.service.events.SchedulerEventTopics;
import com.awplab.core.scheduler.service.scheduler.IPOJOJobFactory;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.SystemError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import javax.security.auth.Subject;
import java.text.SimpleDateFormat;
import java.util.Optional;

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
