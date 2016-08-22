package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.log.Log;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.ui.*;
import org.bson.conversions.Bson;

import java.beans.IntrospectionException;

/**
 * Created by andyphillips404 on 8/15/16.
 */
public class LogViewer extends VerticalLayout {

    private final MongoClient mongoClient;

    private MongoCollectionContainer<Log> logMongoCollectionContainer;

    private Grid grid;

    Logger logger = LoggerFactory.getLogger(LogViewer.class);

    public LogViewer(MongoClient mongoClient, String database, String collection, Bson filter) throws IntrospectionException {
        this.mongoClient = mongoClient;

        grid = new Grid();

        setConnection(database, collection, filter);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        grid.setSizeFull();
        horizontalLayout.addComponent(grid);



    }

    public void setConnection(String database, String collection, Bson filter) throws IntrospectionException {
        if (logMongoCollectionContainer != null) {
            logMongoCollectionContainer.close();
        }
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        logMongoCollectionContainer = new MongoCollectionContainer<>(mongoDatabase.getCollection(collection, Log.class), filter);
        grid.setContainerDataSource(logMongoCollectionContainer);
    }

    public void refreshData() {
        logMongoCollectionContainer.fireItemSetChange(null);
    }

    @Override
    public void detach() {
        logMongoCollectionContainer.close();

        super.detach();
    }
}
