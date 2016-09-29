package com.awplab.core.scheduler.admin;

import com.awplab.core.admin.AdminViewProvider;
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
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.SystemError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import javax.security.auth.Subject;
import java.text.SimpleDateFormat;
import java.util.Optional;

/**
 * Created by andyphillips404 on 8/31/16.
 */
@Component(immediate = true)
@Provides(specifications = {EventHandler.class})
public class SchedulerAdminViewProvider extends AdminViewProvider implements EventHandler {

    @Requires
    SchedulerManager schedulerManager;

    @ServiceProperty(name = EventConstants.EVENT_TOPIC)
    String[] topics = new String[]{SchedulerEventTopics.ANY};

    @Override
    public void handleEvent(Event event) {
        doAccessCurrentView(SchedulerAdminView::refresh, SchedulerAdminView.class);
        pushUpdateMenuButton();
    }

    @Override
    public String getName() {
        return "scheduler";
    }

    @Override
    public String getMenuTitle() {
        return "Scheduler";
    }

    @Override
    public Optional<Resource> getMenuIcon() {
        return Optional.of(FontAwesome.CALENDAR);
    }

    @Override
    public Optional<String> getMenuBadge() {
        int ct = 0;
        try {
            for (String s : schedulerManager.getSchedulerNames()) {
                ct += schedulerManager.getScheduler(s).getCurrentlyExecutingJobs().size();
            }
        }
        catch (Exception ignored) {

        }
        if (ct == 0) return Optional.empty();
        return Optional.of(Integer.toString(ct));
    }

    @Override
    protected View createView(Subject subject) {
        return new SchedulerAdminView();
    }

    private class SchedulerAdminView extends VerticalLayout implements View {

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            refresh();
            updateMenuButton();
        }

        private MenuBar.MenuItem interrupt;
        private MenuBar.MenuItem delete;
        private MenuBar.MenuItem interruptAll;
        private MenuBar.MenuItem deleteAll;

        private MenuBar deleteBar;

        private MenuBar interruptBar;

        BeanItemContainer<SchedulerJobExecutionContext> running = new BeanItemContainer<SchedulerJobExecutionContext>(SchedulerJobExecutionContext.class);

        BeanItemContainer<SchedulerJobDetailTriggers> jobs = new BeanItemContainer<SchedulerJobDetailTriggers>(SchedulerJobDetailTriggers.class);

        ObjectMapper objectMapper = new ObjectMapper();

        public SchedulerAdminView() {

            objectMapper.setDateFormat(SimpleDateFormat.getDateTimeInstance());

            Table.ColumnGenerator jobDetailColumnGenerator = (source, itemId, columnId) -> {
                AbstractSchedulerBean schedulerJobDetailTriggers = ((AbstractSchedulerBean)itemId);
                String tags = "";
                Class jobClass = schedulerJobDetailTriggers.getJobDetail().getJobClass();
                if (StatusJob.class.isAssignableFrom(jobClass)) tags = "Provides Status";
                if (InterruptableJob.class.isAssignableFrom(jobClass)) tags += (tags.length() > 0 ? ",&nbsp;" : "") + "Interruptable";
                return new Label("<b>" + jobClass.getName() + "</b><br>Scheduler:&nbsp;" + schedulerJobDetailTriggers.getScheduler() + "<br>Key:&nbsp;" + schedulerJobDetailTriggers.getJobKeyName() + "<br>Group:&nbsp;" + schedulerJobDetailTriggers.getJobKeyGroup() + "<br>Bundle:&nbsp;" + schedulerJobDetailTriggers.getBundleName() + "<br>Bundle Version:&nbsp;" + schedulerJobDetailTriggers.getBundleVersion() + (tags.length() > 0 ? "<br><font color=\"green\">" + tags + "</font>" : "") , ContentMode.HTML);
            };

            Table.ColumnGenerator jobDataColumnGenerator = (source, itemId, columnId) -> {
                AbstractSchedulerBean context = ((AbstractSchedulerBean) itemId);
                if (context.getJobDataMap() == null) return new Label();
                try {
                    JobDataMap jobDataMap = new JobDataMap(context.getJobDataMap());
                    jobDataMap.remove(IPOJOJobFactory.INSTANCE_MANAGER_KEY);
                    return new Label(AdminViewProvider.jacksonHtml(objectMapper, jobDataMap, true, 0), ContentMode.HTML); //objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jobDataMap).replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").replaceAll(" ", "&nbsp;"), ContentMode.HTML);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Exception processing object job data map!", ex);
                    //return new Label("<font color=\"red\">Exception process job data map!</font>", ContentMode.HTML);
                }
            };

            final Table runningTable = new Table(null, running);
            runningTable.setSelectable(true);
            runningTable.setImmediate(true);

            runningTable.addGeneratedColumn("status", (source, itemId, columnId) -> {
                Job job = ((SchedulerJobExecutionContext)itemId).getJobExecutionContext().getJobInstance();
                if (!(job instanceof StatusJob)) return new Label();
                try {
                    String cancelRequested = "";
                    if (job instanceof AbstractStatusInterruptableJob && ((AbstractStatusInterruptableJob) job).isInterruptRequested()) cancelRequested = "<br><font color=\"red\">Interrupt Requested!</font>";
                    return new Label(AdminViewProvider.jacksonHtml(objectMapper, ((StatusJob) job).getJobStatus(), true, 0) + cancelRequested, ContentMode.HTML);
                }
                catch (JsonProcessingException ex) {
                    throw new RuntimeException("Exception processing object status!", ex);
                }
            });
            runningTable.addGeneratedColumn("jobDetail", jobDetailColumnGenerator);

            runningTable.addGeneratedColumn("trigger", (source, itemId, columnId) -> {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
                SchedulerJobExecutionContext context = ((SchedulerJobExecutionContext)itemId);
                return new Label("<b>" + simpleDateFormat.format(context.getFireTime()).replaceAll(" ", "&nbsp;") + "</b><br>" + context.getFireInstanceId(), ContentMode.HTML);
            });
            runningTable.addGeneratedColumn("jobData", jobDataColumnGenerator);

            runningTable.setVisibleColumns("trigger", "jobDetail", "jobData", "status");
            runningTable.setColumnHeaders("Trigger", "Job Detail", "Job Data", "Status");




            MenuBar refreshRunningBar = new MenuBar();
            refreshRunningBar.setImmediate(true);
            refreshRunningBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshRunningBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshRunningBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> refresh());
            refreshRunningBar.setSizeUndefined();

            interruptBar = new MenuBar();
            interruptBar.setImmediate(true);
            interruptBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            interruptBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            interrupt = interruptBar.addItem("Interrupt", FontAwesome.REMOVE, (MenuBar.Command) selectedItem -> {
                SchedulerJobExecutionContext context = (SchedulerJobExecutionContext)runningTable.getValue();
                if (context == null) return;
                new Thread(() -> {
                    try {
                        schedulerManager.interruptJob(context.getScheduler(), context.getJobExecutionContext().getJobDetail().getKey());
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), this::refresh);
                    } catch (SchedulerException ex) {
                        //throw new RuntimeException("Exception attempting to interrupt job!", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> interruptBar.setComponentError(new SystemError("Exception interrupting job!", ex)));
                    }

                }).start();
                this.refresh();

            });
            interruptAll = interruptBar.addItem("Interrupt All", FontAwesome.REMOVE, (MenuBar.Command) selectedItem -> {
                new Thread(() -> {
                    try {
                        schedulerManager.interruptJobs();
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), this::refresh);
                    } catch (SchedulerException ex) {
                        //throw new RuntimeException("Exception attempting to interrupt job!", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> interruptBar.setComponentError(new SystemError("Exception interrupting jobs!", ex)));
                    }
                }).start();
                this.refresh();

            });
            interruptBar.setSizeUndefined();

            runningTable.addValueChangeListener(event -> interrupt.setEnabled((runningTable.getValue() != null && ((SchedulerJobExecutionContext)runningTable.getValue()).getJobExecutionContext().getJobInstance() instanceof InterruptableJob)));

            CssLayout spacer1 = new CssLayout();
            spacer1.setWidth(100, Unit.PERCENTAGE);

            HorizontalLayout runningToolbar = new HorizontalLayout();
            runningToolbar.addComponents(refreshRunningBar, spacer1, interruptBar);
            runningToolbar.setExpandRatio(spacer1, 1);
            runningToolbar.setWidth(100, Unit.PERCENTAGE);
            runningToolbar.setHeightUndefined();

            runningTable.setSizeFull();

            VerticalLayout runningHolder = new VerticalLayout(runningToolbar, runningTable);
            runningHolder.setSizeFull();
            runningHolder.setExpandRatio(runningTable, 1);

            Table jobsTable = new Table(null, jobs);
            jobsTable.setSelectable(true);
            jobsTable.setImmediate(true);
            jobsTable.addGeneratedColumn("jobDetail", jobDetailColumnGenerator);
            jobsTable.addGeneratedColumn("jobData", jobDataColumnGenerator);
            jobsTable.setVisibleColumns("nextFireTime", "jobDetail", "jobData");
            jobsTable.setColumnHeaders("Next Fire", "Job Detail", "Job Data");

            MenuBar refreshJobsBar = new MenuBar();
            refreshJobsBar.setImmediate(true);
            refreshJobsBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshJobsBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshJobsBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> refresh());
            refreshJobsBar.setSizeUndefined();


            deleteBar = new MenuBar();
            deleteBar.setImmediate(true);
            deleteBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            deleteBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            delete = deleteBar.addItem("Delete", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                final SchedulerJobExecutionContext context = (SchedulerJobExecutionContext) runningTable.getValue();
                if (context == null) return;
                new Thread(() -> {
                    try {
                        schedulerManager.deleteJob(context.getScheduler(), context.getJobExecutionContext().getJobDetail().getKey());
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), this::refresh);
                    } catch (SchedulerException ex) {
                        //throw new RuntimeException("Exception attempting to interrupt job!", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> deleteBar.setComponentError(new SystemError("Exception deleting job!", ex)));

                    }

                }).start();
                this.refresh();

            });
            deleteAll = deleteBar.addItem("Delete All", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                new Thread(() -> {
                    try {
                        schedulerManager.deleteJobs();
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), this::refresh);
                    } catch (SchedulerException ex) {
                        //throw new RuntimeException("Exception attempting to interrupt job!", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> deleteBar.setComponentError(new SystemError("Exception deleting jobs!", ex)));

                    }

                }).start();
                this.refresh();
            });
            deleteBar.setSizeUndefined();

            runningTable.addValueChangeListener(event -> delete.setEnabled((runningTable.getValue() != null)));

            HorizontalLayout jobsToolbar = new HorizontalLayout();
            jobsToolbar.setWidth(100, Unit.PERCENTAGE);
            jobsToolbar.setHeightUndefined();
            CssLayout spacer2 = new CssLayout();
            spacer1.setWidth(100, Unit.PERCENTAGE);
            jobsToolbar.addComponents(refreshJobsBar, spacer2, deleteBar);
            jobsToolbar.setExpandRatio(spacer2, 1);
            jobsTable.setSizeFull();

            VerticalLayout jobsHolder = new VerticalLayout(jobsToolbar, jobsTable);
            jobsHolder.setExpandRatio(jobsTable, 1);

            jobsHolder.setSizeFull();

            TabSheet tabSheet = new TabSheet();
            tabSheet.addTab(runningHolder, "Running", FontAwesome.GEARS);
            tabSheet.addTab(jobsHolder, "Jobs", FontAwesome.CALENDAR);

            tabSheet.setSizeFull();

            this.addComponent(tabSheet);

            refresh();

            setSizeFull();
        }

        private void refresh() {
            running.removeAllItems();
            jobs.removeAllItems();

            deleteBar.setComponentError(null);
            interruptBar.setComponentError(null);

            interrupt.setEnabled(false);
            delete.setEnabled(false);
            deleteAll.setEnabled(false);
            interruptAll.setEnabled(false);

            try {
                for (String s : schedulerManager.getSchedulerNames()) {
                    Scheduler scheduler = schedulerManager.getScheduler(s);
                    for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
                        if (context.getJobInstance() instanceof InterruptableJob) interruptAll.setEnabled(true);
                        running.addItem(new SchedulerJobExecutionContext(s, context));
                    }

                    for (JobKey key : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
                        deleteAll.setEnabled(true);
                        jobs.addItem(new SchedulerJobDetailTriggers(s, scheduler.getJobDetail(key), scheduler.getTriggersOfJob(key)));

                    }
                }
            }
            catch (SchedulerException ex) {
                throw new RuntimeException("Scheduler exception creating list!", ex);
            }

        }


    }
}
