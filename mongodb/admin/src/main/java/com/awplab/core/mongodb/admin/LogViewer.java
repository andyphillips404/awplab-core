package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.log.Log;
import com.awplab.core.mongodb.log.LogFile;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.bson.conversions.Bson;

import java.beans.IntrospectionException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

/**
 * Created by andyphillips404 on 8/15/16.
 */
public class LogViewer extends VerticalLayout {

    private final MongoClient mongoClient;

    private MongoCollectionContainer<Log, Log> logMongoCollectionContainer;

    private Table table;

    Logger logger = LoggerFactory.getLogger(LogViewer.class);

    public LogViewer(MongoClient mongoClient, String database, String collection, Bson filter) {
        this.mongoClient = mongoClient;

        table = new Table();

        setConnection(database, collection, filter);

        table.setSizeFull();

        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.addGeneratedColumn("log", new Table.ColumnGenerator() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Log log = logMongoCollectionContainer.getDatabaseObject(itemId);
                if (log == null) return new Label();

                VerticalLayout v = new VerticalLayout();

                StringBuilder stringBuilder = new StringBuilder();
                if (log.getTimeStamp() != null) stringBuilder.append(simpleDateFormat.format(log.getTimeStamp()));
                final String[] color = {"black"};
                if (log.getLevel() != null) {
                    if (log.getLevel().equalsIgnoreCase("error")) color[0] = "red";
                    if (log.getLevel().equalsIgnoreCase("warn")) color[0] = "orange";
                    String level = "<font color = \"" + color[0] + "\">" + log.getLevel() + "</font>";

                    if (stringBuilder.length() > 0) stringBuilder.append(" - ");
                    stringBuilder.append(level);
                }
                if (log.getMessage() != null && log.getMessage().trim().length() > 0) {
                    if (stringBuilder.length() > 0) stringBuilder.append(" - ");
                    stringBuilder.append(log.getMessage().replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
                }
                v.addComponent(new Label(stringBuilder.toString(), ContentMode.HTML));
                v.setSpacing(false);
                v.setMargin(false);

                if (log.getThrowableStrRep() != null && log.getThrowableStrRep().length > 0) {
                    Button stackTrace = new Button("See throwable details...");
                    stackTrace.addStyleName(ValoTheme.BUTTON_LINK);

                    stackTrace.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            StringBuilder throwableStringBuilder = new StringBuilder();
                            //throwableStringBuilder.append("<font color = \"");
                            //throwableStringBuilder.append(color[0]);
                            //throwableStringBuilder.append("\">");
                            for (int x = 0; x < log.getThrowableStrRep().length; x++) {
                                throwableStringBuilder.append(log.getThrowableStrRep()[x].replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
                                if (log.getThrowableStrRep().length - 1 != x) throwableStringBuilder.append("<br>");
                            }
                            //throwableStringBuilder.append("</font>");
                            VerticalLayout l = new VerticalLayout();
                            l.addComponent(new Label(throwableStringBuilder.toString(),ContentMode.HTML));
                            l.setMargin(true);
                            showWindow(l, "Throwable Details");

                        }
                    });
                    v.addComponent(stackTrace);
                }

                if (log.getLogFiles() != null && log.getLogFiles().size() > 0) {
                    for (LogFile logFile : log.getLogFiles()) {
                        GridFSFile gridFSFile = logFile.getGridFSFile(mongoClient.getDatabase(database));
                        StreamResource streamResource = new StreamResource(new BucketStreamResource(mongoClient.getDatabase(database), logFile.getBucket(), logFile.getFileObjectId()), gridFSFile.getFilename());

                        HorizontalLayout h = new HorizontalLayout();
                        h.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
                        h.addComponent(new Label("File Key: " + logFile.getKey() + " Name: " + gridFSFile.getFilename()));
                        h.setSpacing(true);
                        h.setMargin(true);
                        Link view = new Link(null, streamResource);
                        view.setIcon(FontAwesome.EXTERNAL_LINK);
                        h.addComponent(view);
                        v.addComponent(h);
                    }

                }

                return v;
            }
        });
        table.setVisibleColumns("log");
        table.sort(new String[]{"timeStamp"}, new boolean[]{false});

        this.addComponent(table);

    }

    private void showWindow(Component content, String caption) {
        Panel p = new Panel();
        p.setSizeFull();
        p.addStyleName(ValoTheme.PANEL_BORDERLESS);
        p.setContent(content);
        Window window = new Window(caption);
        window.setContent(p);
        window.setWidth("80%");
        window.setHeight("80%");
        window.setClosable(true);
        window.setResizable(true);
        getUI().addWindow(window);
        window.center();
        window.focus();

    }


    public void setConnection(String database, String collection, Bson filter)  {
        if (logMongoCollectionContainer != null) {
            logMongoCollectionContainer.close();
        }
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        logMongoCollectionContainer = MongoCollectionContainer.simpleContainer(mongoDatabase.getCollection(collection, Log.class), filter);
        table.setContainerDataSource(logMongoCollectionContainer);
    }

    public void refreshData() {
        logMongoCollectionContainer.refreshData();
    }

}
