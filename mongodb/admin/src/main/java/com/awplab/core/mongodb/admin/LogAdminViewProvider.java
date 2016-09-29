package com.awplab.core.mongodb.admin;

import com.awplab.core.admin.AdminViewProvider;
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
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.annotations.Component;
import org.bson.conversions.Bson;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.beans.IntrospectionException;
import java.util.*;
import java.util.Calendar;

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
                component.logViewer.refreshData();
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
            logViewer.refreshData();
        }





        private LogViewer logViewer = null;

        private LogAdminView() {

            MongoDatabase mongoDatabase = mongoService.getMongoClient().getDatabase(database);
            logViewer = new LogViewer(mongoDatabase, mongoDatabase.getCollection(collection, Log.class), null);

            MenuBar refreshBar = new MenuBar();
            refreshBar.setImmediate(true);
            refreshBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            refreshBar.addStyleName(ValoTheme.MENUBAR_SMALL);
            refreshBar.addItem("Refresh", FontAwesome.REFRESH, (MenuBar.Command) selectedItem -> logViewer.refreshData());
            refreshBar.setSizeUndefined();


            final DateField date = new DateField();
            java.util.Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -180);
            date.setValue(calendar.getTime());
            date.addStyleName(ValoTheme.DATEFIELD_BORDERLESS);
            date.addStyleName(ValoTheme.DATEFIELD_SMALL);
            date.setResolution(Resolution.SECOND);
            //date.setWidth(5, Unit.EM);

            MenuBar clearBefore = new MenuBar();
            clearBefore.setImmediate(true);
            clearBefore.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            clearBefore.addStyleName(ValoTheme.MENUBAR_SMALL);
            clearBefore.addItem("Clear Before", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                deleteLog(date.getValue());
                logViewer.refreshData();
            });
            clearBefore.setSizeUndefined();

            MenuBar clearAll = new MenuBar();
            clearAll.setImmediate(true);
            clearAll.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
            clearAll.addStyleName(ValoTheme.MENUBAR_SMALL);
            clearAll.addItem("Clear All", FontAwesome.TRASH, (MenuBar.Command) selectedItem -> {
                deleteLog(new Date());
                logViewer.refreshData();
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
