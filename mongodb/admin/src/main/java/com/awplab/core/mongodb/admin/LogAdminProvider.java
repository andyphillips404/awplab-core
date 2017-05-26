package com.awplab.core.mongodb.admin;

import com.awplab.core.admin.AdminProvider;
import com.awplab.core.common.EventAdminHelper;
import com.awplab.core.mongodb.log.Log;
import com.awplab.core.mongodb.log.events.LogEventData;
import com.awplab.core.mongodb.log.events.LogEventTopics;
import com.awplab.core.mongodb.service.MongoService;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.*;
import org.bson.conversions.Bson;
import org.osgi.service.event.EventHandler;

import javax.security.auth.Subject;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by andyphillips404 on 8/31/16.
 */
@Component(immediate = true, managedservice = LogAdminProvider.CONFIG_MANAGED_SERVICE)
@Provides(specifications = {AdminProvider.class})
@Instantiate
public class LogAdminProvider implements AdminProvider {

    public static final String CONFIG_MANAGED_SERVICE= "com.awplab.core.mongodb.admin.log";

    public static final String PROPERTY_DATABASE = "com.awplab.core.mongodb.admin.log.database";

    public static final String PROPERTY_COLLECTION = "com.awplab.core.mongodb.admin.log.collection";

    @Property(name = PROPERTY_DATABASE, mandatory = true)
    private String database;

    @Property(name = PROPERTY_COLLECTION, mandatory = true)
    private String collection;

    @ServiceController(value = false, specification = AdminProvider.class)
    private boolean serviceController;

    @Requires
    MongoService mongoService;

    private Timer timer = new Timer();
    @Updated
    public void updated() {
        if (serviceController) serviceController = false;

        timer.purge();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (database != null && collection != null && !serviceController) {
                    serviceController = true;
                }
            }
        }, 3000);
    }


    @Override
    public Optional<Integer> getPositionInCategory() {
        return Optional.of(1);
    }

    @Override
    public Optional<String> getCategory() {
        return Optional.of("System");
    }

    @Override
    public String getName() {
        return "log-" + database + "-" + collection;
    }

    @Override
    public Optional<Resource> getMenuIcon() {
        return Optional.of(FontAwesome.LIST);
    }

    @Override
    public String getMenuTitle() {
        return "Log<br>(" + database + "." + collection + ")";
    }

    public void setConnection(String database, String collection) {
        this.database = database;
        this.collection = collection;
    }

    @Override
    public View createView(Subject subject) {
        return new LogAdminView();
    }


    private class LogAdminView extends VerticalLayout implements View, EventHandler {

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            logViewer.getLogMongoDataProvider().refreshAll();
        }

        @Override
        public void handleEvent(org.osgi.service.event.Event event) {
            VaadinProvider.doAccess(getUI(), () -> {
                if (database != null && collection != null && database.equals(event.getProperty(LogEventData.DATABASE)) && collection.equals(event.getProperty(LogEventData.COLLECTION))) {
                    logViewer.getLogMongoDataProvider().refreshAll();
                }
            });
        }

        private LogViewer logViewer = null;

        private LogAdminView() {

            EventAdminHelper.registerForEvent(this, LogEventTopics.ANY);

            MongoDatabase mongoDatabase = mongoService.getMongoClient().getDatabase(database);
            logViewer = new LogViewer(mongoDatabase, mongoDatabase.getCollection(collection, Log.class), null);

            MenuBar refreshBar = new MenuBar();
            //refreshBar.setImmediate(true);
            refreshBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> logViewer.getLogMongoDataProvider().refreshAll());
            refreshBar.setSizeUndefined();


            final DateField date = new DateField();
            date.setValue(LocalDate.now().minusDays(180));
            date.addStyleName(ValoTheme.DATEFIELD_BORDERLESS);
            date.addStyleName(ValoTheme.DATEFIELD_SMALL);
            date.setResolution(DateResolution.DAY);

            MenuBar clearBefore = new MenuBar();
            clearBefore.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            clearBefore.addStyleName(ValoTheme.MENUBAR_SMALL);
            clearBefore.addItem("Clear Before", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                deleteLog(new Date(date.getValue().toEpochDay()));
                //logViewer.refreshData();
                logViewer.getLogMongoDataProvider().refreshAll();
            });
            clearBefore.setSizeUndefined();

            MenuBar clearAll = new MenuBar();
            //clearAll.setImmediate(true);
            clearAll.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            clearAll.addStyleName(ValoTheme.MENUBAR_SMALL);
            clearAll.addItem("Clear All", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                deleteLog(new Date());
                logViewer.getLogMongoDataProvider().refreshAll();
            });
            clearAll.setSizeUndefined();

            CssLayout spacer1 = new CssLayout();
            spacer1.setWidth(100, Unit.PERCENTAGE);

            HorizontalLayout toolbar = new HorizontalLayout();
            toolbar.addComponents(refreshBar, spacer1, clearBefore, date, clearAll); //date, clearBefore);
            toolbar.setExpandRatio(spacer1, 1);
            toolbar.setWidth(100, Unit.PERCENTAGE);
            toolbar.setHeightUndefined();

            this.addComponent(toolbar);
            this.addComponent(logViewer);
            this.setMargin(false);

            logViewer.setSizeFull();
            setExpandRatio(logViewer, 1);

            setSizeFull();
        }


        private void deleteLog(Date olderThan) {
            MongoCollection<Log> logCollection = mongoService.getMongoClient().getDatabase(database).getCollection(collection, Log.class);
            Bson filter = Filters.lt("timeStamp", olderThan);
            logCollection.find(filter).forEach((Block<? super Log>) log -> {
                log.getLogFiles().forEach(logFile -> {
                    GridFSBucket gridFSBucket = GridFSBuckets.create(mongoService.getMongoClient().getDatabase(database), logFile.getBucket());
                    gridFSBucket.delete(logFile.getFileObjectId());
                });
            });
            DeleteResult deleteResult = logCollection.deleteMany(filter);
        }

    }



}
