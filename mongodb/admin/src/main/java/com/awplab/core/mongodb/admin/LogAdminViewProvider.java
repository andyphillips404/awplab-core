package com.awplab.core.mongodb.admin;

import com.awplab.core.admin.AdminView;
import com.awplab.core.admin.AdminViewProvider;
import com.awplab.core.mongodb.service.MongoService;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/19/16.
 */
@Component(immediate = true, name = LogAdminViewProvider.CONFIG_FACTORY_NAME)
@Provides(specifications = {AdminViewProvider.class})
public class LogAdminViewProvider implements AdminViewProvider {

    public static final String CONFIG_FACTORY_NAME = "com.awplab.core.mongodb.admin.log";

    public static final String PROPERTY_DATABASE = "com.awplab.core.mongodb.admin.log.database";

    public static final String PROPERTY_COLLECTION = "com.awplab.core.mongodb.admin.log.collection";

    @Requires
    MongoService mongoService;
    
    @Property(name = PROPERTY_DATABASE, mandatory = true)
    private String database;
    
    @Property(name = PROPERTY_COLLECTION, mandatory = true)
    private String collection;


    private Set<LogAdminView> views = Collections.synchronizedSet(new HashSet<>());

    private Logger logger = LoggerFactory.getLogger(LogAdminViewProvider.class);

    @Updated
    private void updated() {
        for (LogAdminView logAdminView : views) {
            try {
                logAdminView.logViewer.setConnection(database, collection, null);
            } catch (IntrospectionException e) {
                logger.error("Exception attempting to update log veiwer connection", e);
            }
        }
    }


    @Override
    public AdminView createView(Subject subject) {
        final LogAdminView logAdminView = new LogAdminView();
        logAdminView.addDetachListener(event -> {
            views.remove(logAdminView);
        });
        views.add(logAdminView);
        return logAdminView;

    }





    private class LogAdminView extends AdminView {

        @Override
        public String getMenuTitle() {
            return "Log";
        }

        @Override
        public Optional<Resource> getMenuIcon() {
            return Optional.of(FontAwesome.LIST);
        }


        private LogViewer logViewer = null;

        private Logger logger = LoggerFactory.getLogger(LogAdminView.class);

        public LogAdminView() {
            super("log");

            try {
                logViewer = new LogViewer(mongoService.getMongoClient(), database, collection, null);
            } catch (IntrospectionException e) {
                logger.error("Exception creating log viewer!", e);
            }

            logViewer.setSizeFull();
            this.addComponent(logViewer);
        }




    }
}
