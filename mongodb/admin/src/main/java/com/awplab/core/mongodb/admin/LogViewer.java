package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.log.Log;
import com.awplab.core.mongodb.log.LogFile;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Sorts;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;

/**
 * Created by andyphillips404 on 8/15/16.
 */
public class LogViewer extends VerticalLayout {

    private final MongoDatabase mongoDatabase;
    private final MongoDataProvider<Log> logMongoDataProvider;
    private final Grid<Log> grid;

    Logger logger = LoggerFactory.getLogger(LogViewer.class);

    public LogViewer(MongoDatabase mongoDatabase, MongoCollection<Log> collection, Bson filter) {
        this.mongoDatabase = mongoDatabase;
        this.logMongoDataProvider = new MongoDataProvider<Log>(collection, filter);
        grid = new Grid<>(logMongoDataProvider);
        start();

    }

    private void start() {
        grid.setSizeFull();

        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(Log::getTimeStamp, new DateRenderer(new SimpleDateFormat())).setCaption("Date");
        grid.addColumn(log -> {
            StringBuilder stringBuilder = new StringBuilder();
            final String[] color = {"black"};
            if (log.getLevel() != null) {
                if (log.getLevel().equalsIgnoreCase("error")) color[0] = "red";
                if (log.getLevel().equalsIgnoreCase("warn")) color[0] = "orange";
                String level = "<font color = \"" + color[0] + "\">" + log.getLevel() + "</font>";
                if (stringBuilder.length() > 0) stringBuilder.append(" - ");
                stringBuilder.append(level);
            }
            return stringBuilder.toString();

        }, new HtmlRenderer()).setCaption("Level");
        ButtonRenderer<Log> buttonRenderer = new ButtonRenderer<Log>(event -> {
            Log log = event.getItem();
            if (((log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) || (log.getLogFiles() != null && log.getLogFiles().size() > 0))) {
                grid.setDetailsVisible(log, !grid.isDetailsVisible(log));
            }
        });
        buttonRenderer.setHtmlContentAllowed(true);
        grid.addColumn(log -> {
            if ((log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) || (log.getLogFiles() != null && log.getLogFiles().size() > 0)) {
                return "Details";
            }
            return "<font color=\"LIGHTGRAY\">Details</font>";
        }, buttonRenderer);

        grid.addColumn(log -> log.getMessage().replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"), new HtmlRenderer()).setCaption("Message");


        //grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.setDetailsGenerator(log -> {
            //if (!((log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) || (log.getLogFiles() != null && log.getLogFiles().size() > 0))) {
            //    return null;
            //}

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setMargin(false);
            verticalLayout.setSizeFull();
            ComponentContainer mainComponent = verticalLayout;
            if ((log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) && (log.getLogFiles() != null && log.getLogFiles().size() > 0)) {
                TabSheet tabSheet = new TabSheet();
                tabSheet.setSizeFull();
                verticalLayout.addComponent(tabSheet);
                mainComponent = tabSheet;
            }
            if (log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) {
                StringBuilder throwableStringBuilder = new StringBuilder();
                throwableStringBuilder.append("<small>");
                for (int x = 0; x < log.getThrowableStrRep().length; x++) {
                    throwableStringBuilder.append(log.getThrowableStrRep()[x].replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
                    if (log.getThrowableStrRep().length - 1 != x) throwableStringBuilder.append("<br>");
                }
                throwableStringBuilder.append("</small>");
                Label l = new Label(throwableStringBuilder.toString(),ContentMode.HTML);
                l.setSizeFull();
                l.setHeightUndefined();
                Panel scroll = new Panel(l);
                scroll.setSizeFull();
                //scroll.setCaption("Throwable");

                mainComponent.addComponent(scroll);
            }
            if (log.getLogFiles() != null && log.getLogFiles().size() > 0) {
                VerticalLayout filesHolder = new VerticalLayout();
                for (LogFile logFile : log.getLogFiles()) {
                    GridFSFile gridFSFile = logFile.getGridFSFile(mongoDatabase);
                    StreamResource streamResource = new BucketStreamResource(mongoDatabase, logFile.getBucket(), logFile.getFileObjectId());

                    HorizontalLayout h = new HorizontalLayout();
                    h.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
                    h.addComponent(new Label("File Key: " + logFile.getKey() + " Name: " + gridFSFile.getFilename()));
                    h.setSpacing(true);
                    h.setMargin(false);
                    Link view = new Link(null, streamResource);
                    view.setIcon(FontAwesome.EXTERNAL_LINK);
                    view.setTargetName("_blank");
                    h.addComponent(view);
                    filesHolder.addComponent(h);
                }
                filesHolder.setSizeFull();
                filesHolder.setHeightUndefined();
                Panel scroll = new Panel(filesHolder);
                scroll.setSizeFull();
                scroll.setCaption("Files");
                mainComponent.addComponent(scroll);
            }
            mainComponent.setSizeFull();
            verticalLayout.setHeight(25, Unit.EM);
            return mainComponent;
        });

        logMongoDataProvider.setDefaultSort(Sorts.descending("timeStamp"));

        this.addComponent(grid);

        this.setMargin(false);
    }






    public MongoDataProvider<Log> getLogMongoDataProvider() {
        return logMongoDataProvider;
    }

    public Grid<Log> getGrid() {
        return grid;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }


}
