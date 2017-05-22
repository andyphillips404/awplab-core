package com.awplab.core.scheduler.admin;

import com.awplab.core.admin.AdminProvider;
import com.awplab.core.admin.events.AdminEventTopics;
import com.awplab.core.common.EventAdminHelper;
import com.awplab.core.scheduler.service.AbstractStatusInterruptableJob;
import com.awplab.core.scheduler.service.SchedulerManager;
import com.awplab.core.scheduler.service.StatusJob;
import com.awplab.core.scheduler.service.events.SchedulerEventTopics;
import com.awplab.core.scheduler.service.scheduler.IPOJOJobFactory;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.data.ValueProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.service.event.EventHandler;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by andyphillips404 on 8/31/16.
 */
@Instantiate
@Component(immediate = true)
@Provides
public class SchedulerAdminProvider implements AdminProvider {

    @Requires
    SchedulerManager schedulerManager;

    @Override
    public Optional<Integer> getPositionInCategory() {
        return Optional.of(0);
    }

    @Override
    public Optional<String> getCategory() {
        return Optional.of("System");
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
    public View createView(Subject subject) {
        return new SchedulerAdminView();
    }

    private class SchedulerAdminView extends VerticalLayout implements View, EventHandler {

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            refresh();
            //getAdminUI().updateMenuButton(SchedulerAdminProvider.this);
            EventAdminHelper.postEvent(AdminEventTopics.UPDATE_MENU_REQUESTED);
        }

        @Override
        public void handleEvent(org.osgi.service.event.Event event) {
            VaadinProvider.doAccess(getUI(), () -> {
                refresh();
                //getAdminUI().updateMenuButton(SchedulerAdminProvider.this);
                EventAdminHelper.postEvent(AdminEventTopics.UPDATE_MENU_REQUESTED);
            });
        }

        private MenuBar.MenuItem interrupt;
        private MenuBar.MenuItem delete;
        private MenuBar.MenuItem interruptAll;
        private MenuBar.MenuItem deleteAll;

        private MenuBar deleteBar;

        private MenuBar interruptBar;

        private Grid<SchedulerJobExecutionContext> runningGrid;

        private Grid<SchedulerJobDetailTriggers> jobsGrid;

        ObjectMapper objectMapper = new ObjectMapper();

        private Set<String> detailInstanceIds = new HashSet<>();

        private Panel detailsPanelRunning = new Panel("Details");
        private Panel detailsPanelScheduled = new Panel("Details");

        public SchedulerAdminView() {

            objectMapper.setDateFormat(new SimpleDateFormat());

            Logger logger = LoggerFactory.getLogger(this.getClass());

            EventAdminHelper.registerForEvent(this, SchedulerEventTopics.ANY);

            ValueProvider<AbstractSchedulerBean, String> jobDetailValueProvider = (ValueProvider<AbstractSchedulerBean, String>) schedulerJobDetailTriggers -> {
                String tags = "";
                Class jobClass = schedulerJobDetailTriggers.getJobDetail().getJobClass();
                if (StatusJob.class.isAssignableFrom(jobClass)) tags = "Provides Status";
                if (InterruptableJob.class.isAssignableFrom(jobClass)) tags += (tags.length() > 0 ? ",&nbsp;" : "") + "Interruptable";
                return "<b>" + jobClass.getName() + "</b><br>Scheduler:&nbsp;" + schedulerJobDetailTriggers.getScheduler() + "<br>Key:&nbsp;" + schedulerJobDetailTriggers.getJobKeyName() + "<br>Group:&nbsp;" + schedulerJobDetailTriggers.getJobKeyGroup() + "<br>Bundle:&nbsp;" + schedulerJobDetailTriggers.getBundleName() + "<br>Bundle Version:&nbsp;" + schedulerJobDetailTriggers.getBundleVersion() + (tags.length() > 0 ? "<br><font color=\"green\">" + tags + "</font>" : "");
            };


            runningGrid = new Grid<>();
            runningGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            runningGrid.addColumn(SchedulerJobExecutionContext::getFireTime, new DateRenderer(new SimpleDateFormat())).setCaption("Fired").setSortable(false);
            runningGrid.addColumn(SchedulerJobExecutionContext::getScheduler).setCaption("Scheduler").setSortable(false);
            runningGrid.addColumn(SchedulerJobExecutionContext::getJobKeyName).setCaption("Job Key").setSortable(false);
            runningGrid.addColumn(SchedulerJobExecutionContext::getJobKeyGroup).setCaption("Job Group").setSortable(false);
            runningGrid.addColumn(SchedulerJobExecutionContext::getBundleName).setCaption("Bundle").setSortable(false);
            runningGrid.addColumn(SchedulerJobExecutionContext::getBundleVersion).setCaption("Bundle Version").setSortable(false);
            runningGrid.addColumn(SchedulerJobExecutionContext::getJobClass).setCaption("Job Class").setSortable(false);
            runningGrid.addColumn(schedulerJobExecutionContext -> {
                String tags = "";
                Class jobClass = schedulerJobExecutionContext.getJobDetail().getJobClass();
                if (StatusJob.class.isAssignableFrom(jobClass)) tags = "Provides Status";
                if (InterruptableJob.class.isAssignableFrom(jobClass)) tags += (tags.length() > 0 ? ",&nbsp;" : "") + "Interruptable";
                return tags;
            }, new HtmlRenderer()).setCaption("Tags").setSortable(false);
            runningGrid.addSelectionListener(selectionEvent -> refreshDetails());

            MenuBar refreshRunningBar = new MenuBar();
            //refreshRunningBar.setImmediate(true);
            refreshRunningBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshRunningBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshRunningBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> refresh());
            refreshRunningBar.setSizeUndefined();

            interruptBar = new MenuBar();
            //interruptBar.setImmediate(true);
            interruptBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            interruptBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            interrupt = interruptBar.addItem("Interrupt", FontAwesome.REMOVE, (MenuBar.Command) selectedItem -> {
                if (runningGrid.getSelectedItems().size() == 0) return;
                SchedulerJobExecutionContext context = runningGrid.getSelectedItems().iterator().next();
                new Thread(() -> {
                    try {
                        schedulerManager.interruptJob(context.getScheduler(), context.getJobExecutionContext().getJobDetail().getKey());
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), this::refresh);
                    } catch (SchedulerException ex) {
                        logger.error("Scheduler exception interrupting job", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> Notification.show("Error performing command", "See log for details", Notification.Type.ERROR_MESSAGE));
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
                        logger.error("Scheduler exception interrupting jobs", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> Notification.show("Error performing command", "See log for details", Notification.Type.ERROR_MESSAGE));
                    }
                }).start();
                this.refresh();

            });
            interruptBar.setSizeUndefined();

            runningGrid.addSelectionListener(selectionEvent -> {
                interrupt.setEnabled(false);
                selectionEvent.getFirstSelectedItem().ifPresent(schedulerJobExecutionContext -> {
                    interrupt.setEnabled(schedulerJobExecutionContext.getJobExecutionContext().getJobInstance() instanceof InterruptableJob);
                });

            });

            CssLayout spacer1 = new CssLayout();
            spacer1.setWidth(100, Unit.PERCENTAGE);

            HorizontalLayout runningToolbar = new HorizontalLayout();
            runningToolbar.addComponents(refreshRunningBar, spacer1, interruptBar);
            runningToolbar.setExpandRatio(spacer1, 1);
            runningToolbar.setWidth(100, Unit.PERCENTAGE);
            runningToolbar.setHeightUndefined();

            runningGrid.setSizeFull();

            detailsPanelRunning.setSizeFull();
            VerticalSplitPanel runningSplitPanel = new VerticalSplitPanel(runningGrid, detailsPanelRunning);
            runningSplitPanel.setSizeFull();
            runningSplitPanel.setSplitPosition(70, Unit.PERCENTAGE);

            VerticalLayout runningHolder = new VerticalLayout(runningToolbar, runningSplitPanel);
            runningHolder.setSizeFull();
            runningHolder.setExpandRatio(runningSplitPanel, 1);
            runningHolder.setMargin(false);

            jobsGrid = new Grid<>();
            jobsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getNextFireTime, new DateRenderer(new SimpleDateFormat())).setCaption("Next Fire").setSortable(false);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getScheduler).setCaption("Scheduler").setSortable(false);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getJobKeyName).setCaption("Job Key").setSortable(false);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getJobKeyGroup).setCaption("Job Group").setSortable(false);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getBundleName).setCaption("Bundle").setSortable(false);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getBundleVersion).setCaption("Bundle Version").setSortable(false);
            jobsGrid.addColumn(SchedulerJobDetailTriggers::getJobClass).setCaption("Job Class").setSortable(false);
            jobsGrid.addColumn(schedulerJobDetailTriggers -> {
                String tags = "";
                Class jobClass = schedulerJobDetailTriggers.getJobDetail().getJobClass();
                if (StatusJob.class.isAssignableFrom(jobClass)) tags = "Provides Status";
                if (InterruptableJob.class.isAssignableFrom(jobClass)) tags += (tags.length() > 0 ? ",&nbsp;" : "") + "Interruptable";
                return tags;
            }, new HtmlRenderer()).setCaption("Tags").setSortable(false);
            jobsGrid.addSelectionListener(selectionEvent -> refreshDetails());

            MenuBar refreshJobsBar = new MenuBar();
            //refreshJobsBar.setImmediate(true);
            refreshJobsBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshJobsBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshJobsBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> refresh());
            refreshJobsBar.setSizeUndefined();

            deleteBar = new MenuBar();
            //deleteBar.setImmediate(true);
            deleteBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            deleteBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            delete = deleteBar.addItem("Delete", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                if (runningGrid.getSelectedItems().size() == 0) return;
                final SchedulerJobDetailTriggers context = jobsGrid.getSelectedItems().iterator().next();
                new Thread(() -> {
                    try {
                        schedulerManager.deleteJob(context.getScheduler(), context.getJobDetail().getKey());
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), this::refresh);
                    } catch (SchedulerException ex) {
                        logger.error("Scheduler exception deleting job", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> Notification.show("Error performing command", "See log for details", Notification.Type.ERROR_MESSAGE));
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
                        logger.error("Scheduler exception deleting all jobs", ex);
                        VaadinProvider.doAccess(SchedulerAdminView.this.getUI(), () -> Notification.show("Error performing command", "See log for details", Notification.Type.ERROR_MESSAGE));
                    }

                }).start();
                this.refresh();
            });
            deleteBar.setSizeUndefined();

            jobsGrid.addSelectionListener(selectionEvent -> {
                delete.setEnabled(selectionEvent.getAllSelectedItems().size() > 0);
            });

            HorizontalLayout jobsToolbar = new HorizontalLayout();
            jobsToolbar.setWidth(100, Unit.PERCENTAGE);
            jobsToolbar.setHeightUndefined();
            CssLayout spacer2 = new CssLayout();
            spacer1.setWidth(100, Unit.PERCENTAGE);
            jobsToolbar.addComponents(refreshJobsBar, spacer2, deleteBar);
            jobsToolbar.setExpandRatio(spacer2, 1);
            jobsGrid.setSizeFull();

            detailsPanelScheduled.setSizeFull();
            VerticalSplitPanel jobsSplitPanel = new VerticalSplitPanel(jobsGrid, detailsPanelScheduled);
            jobsSplitPanel.setSizeFull();
            jobsSplitPanel.setSplitPosition(70, Unit.PERCENTAGE);

            VerticalLayout jobsHolder = new VerticalLayout(jobsToolbar, jobsSplitPanel);
            jobsHolder.setExpandRatio(jobsSplitPanel, 1);
            jobsHolder.setSizeFull();
            jobsHolder.setMargin(false);

            TabSheet tabSheet = new TabSheet();
            tabSheet.addTab(runningHolder, "Running", FontAwesome.GEARS);
            tabSheet.addTab(jobsHolder, "Jobs", FontAwesome.CALENDAR);

            tabSheet.setSizeFull();

            this.addComponent(tabSheet);

            refresh();

            setSizeFull();
            setMargin(false);
        }

        private void refresh() {
            //runningGrid.setItems();
            //jobsGrid.setItems();

            String selectedFireInstanceId = runningGrid.getSelectedItems().stream().findFirst().map(SchedulerJobExecutionContext::getFireInstanceId).orElseGet(() -> null);
            JobKey selectedJobKey = jobsGrid.getSelectedItems().stream().findFirst().map(schedulerJobDetailTriggers -> schedulerJobDetailTriggers.getJobDetail().getKey()).orElseGet(() -> null);

            deleteBar.setComponentError(null);
            interruptBar.setComponentError(null);

            interrupt.setEnabled(false);
            delete.setEnabled(false);
            deleteAll.setEnabled(false);
            interruptAll.setEnabled(false);

            List<SchedulerJobExecutionContext> running = new ArrayList<>();
            List<SchedulerJobDetailTriggers> jobs = new ArrayList<>();

            HashMap<String, SchedulerJobExecutionContext> newDetailInstanceIds = new HashMap<>();

            try {
                for (String s : schedulerManager.getSchedulerNames()) {
                    Scheduler scheduler = schedulerManager.getScheduler(s);
                    for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
                        if (context.getJobInstance() instanceof InterruptableJob) interruptAll.setEnabled(true);
                        SchedulerJobExecutionContext newContext = new SchedulerJobExecutionContext(s, context);
                        running.add(newContext);
                        if (detailInstanceIds.contains(context.getFireInstanceId())) {
                            newDetailInstanceIds.put(context.getFireInstanceId(), newContext);
                        }
                    }

                    for (JobKey key : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
                        deleteAll.setEnabled(true);
                        jobs.add(new SchedulerJobDetailTriggers(s, scheduler.getJobDetail(key), scheduler.getTriggersOfJob(key)));

                    }
                }

                runningGrid.setItems(running);
                jobsGrid.setItems(jobs);

                detailInstanceIds.clear();
                for (String key: newDetailInstanceIds.keySet()) {
                    runningGrid.setDetailsVisible(newDetailInstanceIds.get(key), true);
                }
                detailInstanceIds.addAll(newDetailInstanceIds.keySet());
            }
            catch (SchedulerException ex) {
                throw new RuntimeException("Scheduler exception creating list!", ex);
            }

            if (selectedFireInstanceId != null) {
                runningGrid.deselectAll();
                running.stream().filter(schedulerJobExecutionContext -> schedulerJobExecutionContext.getFireInstanceId().equals(selectedFireInstanceId)).findFirst().ifPresent(runningGrid::select);
            }

            if (selectedJobKey != null) {
                jobsGrid.deselectAll();
                jobs.stream().filter(schedulerJobDetailTriggers -> schedulerJobDetailTriggers.getJobDetail().getKey().equals(selectedJobKey)).findFirst().ifPresent(jobsGrid::select);
            }
        }

        private void refreshDetails() {
            detailsPanelRunning.setContent(new Label(""));
            detailsPanelScheduled.setContent(new Label(""));

            runningGrid.getSelectedItems().stream().findFirst().ifPresent(schedulerJobExecutionContext -> {
                detailsPanelRunning.setContent(refreshDetails(schedulerJobExecutionContext));
            });
            jobsGrid.getSelectedItems().stream().findFirst().ifPresent(schedulerJobDetailTriggers -> {
                detailsPanelScheduled.setContent(refreshDetails(schedulerJobDetailTriggers));
            });

        }

        private Label refreshDetails(AbstractSchedulerBean context) {
            //VerticalLayout verticalLayout = new VerticalLayout();
            //verticalLayout.setMargin(false);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>Job Data:<b><br>");
            if (!(context.getJobDataMap() == null)) {
                try {
                    JobDataMap jobDataMap = new JobDataMap(context.getJobDataMap());
                    jobDataMap.remove(IPOJOJobFactory.INSTANCE_MANAGER_KEY);
                    stringBuilder.append(AdminProvider.jacksonHtml(objectMapper, jobDataMap, true, 0)); //objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jobDataMap).replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").replaceAll(" ", "&nbsp;"), ContentMode.HTML);
                } catch (JsonProcessingException ex) {
                    //throw new RuntimeException("Exception processing object job data map!", ex);
                    //return new Label("<font color=\"red\">Exception process job data map!</font>", ContentMode.HTML);
                    stringBuilder.append("<font color=\"red\">Exception processing job data map!</font>");
                }
            }
            stringBuilder.append("<br><br><b>Job Status:<b><br>");
            if (context instanceof SchedulerJobExecutionContext) {
                Job job = ((SchedulerJobExecutionContext) context).getJobExecutionContext().getJobInstance();
                if ((job instanceof StatusJob)) {
                    try {
                        String cancelRequested = "";
                        if (job instanceof AbstractStatusInterruptableJob && ((AbstractStatusInterruptableJob) job).isInterruptRequested())
                            cancelRequested = "<br><font color=\"red\">Interrupt Requested!</font>";
                        stringBuilder.append(AdminProvider.jacksonHtml(objectMapper, ((StatusJob) job).getJobStatus(), true, 0)).append(cancelRequested);
                    } catch (JsonProcessingException ex) {
                        //throw new RuntimeException("Exception processing object status!", ex);
                        stringBuilder.append("<font color=\"red\">Exception processing job status!</font>");
                    }
                }

                //verticalLayout.setHeight(25, Unit.EM);
            }

            Label label = new Label(stringBuilder.toString(), ContentMode.HTML);;
            label.setSizeFull();
            label.setHeightUndefined();
            return label;


        }


    }
}
