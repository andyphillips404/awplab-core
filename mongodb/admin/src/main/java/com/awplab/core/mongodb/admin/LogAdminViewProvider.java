package com.awplab.core.mongodb.admin;

import com.awplab.core.admin.AdminViewProvider;
import com.awplab.core.mongodb.log.Log;
import com.awplab.core.mongodb.log.events.LogEventData;
import com.awplab.core.mongodb.log.events.LogEventTopics;
import com.awplab.core.mongodb.service.MongoService;
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
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.bson.conversions.Bson;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import javax.security.auth.Subject;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

/**
 * Created by andyphillips404 on 8/19/16.
 */
@Component(immediate = true)
@Provides(specifications = {EventHandler.class})
public class LogAdminViewProvider extends AdminViewProvider implements EventHandler {

    @Requires
    MongoService mongoService;

    private String database;

    private String collection;

    @ServiceProperty(name = EventConstants.EVENT_TOPIC)
    String[] topics = new String[]{LogEventTopics.ANY};

    @Override
    public void handleEvent(Event event) {
        if (database != null && collection != null && database.equals(event.getProperty(LogEventData.DATABASE)) && collection.equals(event.getProperty(LogEventData.COLLECTION))) {
            doAccessCurrentView(component -> {
                component.logViewer.getLogMongoDataProvider().refreshAll();
            }, LogAdminView.class);
        }
    }

    @Override
    public String getName() {
        return "log-" + database + "-" + collection;
    }

    @Override
    protected View createView(Subject subject) {
        return new LogAdminView();
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

    private class LogAdminView extends VerticalLayout implements View {

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            logViewer.getLogMongoDataProvider().refreshAll();
        }





        private LogViewer logViewer = null;

        private LogAdminView() {

            MongoDatabase mongoDatabase = mongoService.getMongoClient().getDatabase(database);
            logViewer = new LogViewer(mongoDatabase, mongoDatabase.getCollection(collection, Log.class), null);

            MenuBar refreshBar = new MenuBar();
            //refreshBar.setImmediate(true);
            refreshBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> logViewer.getLogMongoDataProvider().refreshAll());
            refreshBar.setSizeUndefined();

            MenuBar fault = new MenuBar();
            refreshBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            MenuBar.MenuItem faultItem = refreshBar.addItem("Fault Detail", FontAwesome.AMBULANCE, (MenuBar.Command) selectedItem -> logViewer.showFaultWindow());
            refreshBar.setSizeUndefined();

            MenuBar file = new MenuBar();
            refreshBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            MenuBar.MenuItem fileItem = refreshBar.addItem("Files", FontAwesome.FILE, (MenuBar.Command) selectedItem -> logViewer.showFilesWindow());
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

            logViewer.getGrid().addSelectionListener(event -> {
                Log log = event.getAllSelectedItems().iterator().next();
                faultItem.setEnabled(log != null && log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0);
                fileItem.setEnabled(log != null && log.getLogFiles() != null && log.getLogFiles().size() > 0);
            });

            CssLayout spacer1 = new CssLayout();
            spacer1.setWidth(100, Unit.PERCENTAGE);

            HorizontalLayout toolbar = new HorizontalLayout();
            toolbar.addComponents(refreshBar, file, fault, spacer1, clearBefore, date, clearAll); //date, clearBefore);
            toolbar.setExpandRatio(spacer1, 1);
            toolbar.setWidth(100, Unit.PERCENTAGE);
            toolbar.setHeightUndefined();

            this.addComponent(toolbar);
            this.addComponent(logViewer);

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
