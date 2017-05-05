package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.log.Log;
import com.awplab.core.mongodb.log.LogFile;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Sorts;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ImageRenderer;
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

        grid.addColumn(Log::getTimeStamp, new DateRenderer(new SimpleDateFormat())).setCaption("Date").setSortProperty("timeStamp");
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

        }, new HtmlRenderer()).setCaption("Level").setSortProperty("level");

        /*
        grid.addColumn(log -> {
            if (log.getLogFiles() != null && log.getLogFiles().size() > 0) return log.getLogFiles().size();
            return "";
        }).setCaption("Files").setSortable(false);
        */

        grid.addColumn(log -> "Details", new ButtonRenderer<Log>(event -> {
            Log log = event.getItem();


            TabSheet tabSheet = new TabSheet();
            tabSheet.setSizeFull();

            Label messageLabel = new Label(log.getMessage().replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"),ContentMode.HTML);
            messageLabel.setWidth("100%");
            messageLabel.setHeightUndefined();
            Panel messageScroll = new Panel(messageLabel);
            messageScroll.setSizeFull();
            tabSheet.addTab(messageScroll, "Message");

            StringBuilder sourceString = new StringBuilder();
            sourceString.append("Logger: ").append(log.getLoggerName());
            sourceString.append("<br>Thread Name: ").append(log.getThreadName());
            sourceString.append("<br>Logger Class: ").append(log.getFQNOfLoggerClass());
            if (log.getLocationInfo() != null) {
                sourceString.append("<br><br>Location Info:");
                sourceString.append("<br>Class: ").append(log.getLocationInfo().getClassName());
                sourceString.append("<br>Line: ").append(log.getLocationInfo().getLineNumber());
                sourceString.append("<br>Method: ").append(log.getLocationInfo().getMethodName());
                sourceString.append("<br>File Name: ").append(log.getLocationInfo().getFileName());
            }
            if (log.getProperties() != null && log.getProperties().size() > 0) {
                sourceString.append("<br><br>Properties:");
                log.getProperties().forEach((o, o2) -> {
                    sourceString.append("<br>").append(o).append(": ").append(o2);
                });
            }
            Label source = new Label(sourceString.toString(),ContentMode.HTML);
            messageLabel.setWidth("100%");
            messageLabel.setHeightUndefined();
            Panel sourceScroll = new Panel(source);
            sourceScroll.setSizeFull();
            tabSheet.addTab(sourceScroll, "Source");

            if (log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) {
                StringBuilder throwableStringBuilder = new StringBuilder();
                //throwableStringBuilder.append("<small>");
                for (int x = 0; x < log.getThrowableStrRep().length; x++) {
                    throwableStringBuilder.append(log.getThrowableStrRep()[x].replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
                    if (log.getThrowableStrRep().length - 1 != x) throwableStringBuilder.append("<br>");
                }
                //throwableStringBuilder.append("</small>");
                Label l = new Label(throwableStringBuilder.toString(),ContentMode.HTML);
                l.setWidthUndefined();
                l.setHeightUndefined();
                Panel scroll = new Panel(l);
                scroll.setSizeFull();
                //scroll.setCaption("Throwable");

                tabSheet.addTab(scroll, "Exception");

            }
            if (log.getLogFiles() != null && log.getLogFiles().size() > 0) {
                VerticalLayout filesHolder = new VerticalLayout();
                for (LogFile logFile : log.getLogFiles()) {
                    GridFSFile gridFSFile = logFile.getGridFSFile(mongoDatabase);
                    StreamResource streamResource = new BucketStreamResource(mongoDatabase, logFile.getBucket(), logFile.getFileObjectId());

                    VerticalLayout v = new VerticalLayout();
                    v.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
                    v.addComponent(new Label("<u>File Key: " + logFile.getKey() + "</u><br>Name: " + gridFSFile.getFilename(), ContentMode.HTML));
                    v.setSpacing(true);
                    v.setMargin(false);
                    Link view = new Link("Open", streamResource);
                    view.setIcon(FontAwesome.EXTERNAL_LINK);
                    view.setTargetName("_blank");
                    v.addComponent(view);
                    filesHolder.addComponent(v);
                }
                filesHolder.setSizeFull();
                filesHolder.setHeightUndefined();
                Panel scroll = new Panel(filesHolder);
                scroll.setSizeFull();
                //scroll.setCaption("Files");
                tabSheet.addTab(scroll, "Files");

            }

            VerticalLayout holder = new VerticalLayout();
            holder.setSizeFull();
            holder.addComponent(tabSheet);

            Window window = new Window("Log Entry Details");
            window.setWidth(80, Unit.PERCENTAGE);
            window.setHeight(80, Unit.PERCENTAGE);
            window.setContent(holder);
            window.setResizable(true);
            window.setDraggable(true);
            window.setModal(true);
            window.setClosable(true);


            getUI().addWindow(window);

        })).setSortable(false);

        grid.addColumn(Log::getMessage).setSortProperty("message");

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
