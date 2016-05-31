package com.awplab.core.mongodb.log;


import com.awplab.core.common.TemporaryFile;
import com.awplab.core.mongodb.LoggerProperties;
import com.awplab.core.mongodb.MongoService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.apache.felix.ipojo.annotations.*;
import org.apache.log4j.MDC;
import org.bson.types.ObjectId;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by andyphillips404 on 3/6/15.
 */

@Component(immediate = true, managedservice = StagingLogAppender.CONFIG_MANAGED_SERVICE_NAME)
@Provides(properties = {
        @StaticServiceProperty(name = PaxLoggingService.APPENDER_NAME_PROPERTY, value = "MongoDbLogAppender", type = "java.lang.String")
})
@Instantiate
public class StagingLogAppender implements PaxAppender {

    public static final String CONFIG_MANAGED_SERVICE_NAME = "com.hdscores.v3.log.appender";

    public static final String PROPERTY_DEFAULT_DATABASE = "com.hdscores.v3.log.appender.defaultDatabase";
    @ServiceProperty(name = PROPERTY_DEFAULT_DATABASE)
    private String defaultDatabase;

    public static final String PROPERTY_DEFAULT_COLLECTION = "com.hdscores.v3.log.appender.defaultCollection";
    @ServiceProperty(name = PROPERTY_DEFAULT_COLLECTION, value = "log")
    private String defaultCollection;

    public static final String PROPERTY_DEFAULT_GRIDFS_COLLECTION = "com.hdscores.v3.log.appender.defaultGridFSCollection";
    @ServiceProperty(name = PROPERTY_DEFAULT_GRIDFS_COLLECTION, value = "log-fs")
    private String defaultGridFSCollection;

    @Requires
    MongoService mongoService;

    private static final String PROPERTY_INTERNAL_DISABLE = "mongodb.appender.internalDisable";


    @Override
    public void doAppend(PaxLoggingEvent paxLoggingEvent) {


        MDC.put(PROPERTY_INTERNAL_DISABLE, true);
        try {
            Map props = paxLoggingEvent.getProperties();
            if ((Boolean) props.getOrDefault(PROPERTY_INTERNAL_DISABLE, Boolean.FALSE)) return;
            if ((Boolean) props.getOrDefault(LoggerProperties.PROPERTY_DISABLE, Boolean.FALSE)) return;

            String database = (props.containsKey(LoggerProperties.PROPERTY_DATABASE) && props.get(LoggerProperties.PROPERTY_DATABASE) != null ? props.get(LoggerProperties.PROPERTY_DATABASE).toString() : defaultDatabase);
            if (database == null) return;

            String collection = (props.containsKey(LoggerProperties.PROPERTY_COLLECTION) && props.get(LoggerProperties.PROPERTY_COLLECTION) != null ? props.get(LoggerProperties.PROPERTY_COLLECTION).toString() : defaultCollection);
            String gridFSCollection = (props.containsKey(LoggerProperties.PROPERTY_GRIDFS_COLLECTION) && props.get(LoggerProperties.PROPERTY_GRIDFS_COLLECTION) != null ? props.get(LoggerProperties.PROPERTY_GRIDFS_COLLECTION).toString() : defaultGridFSCollection);


            MongoDatabase mongoDatabase = mongoService.getMongoClient().getDatabase(database);

            if (props.containsKey(LoggerProperties.PROPERTY_GRIDFS_FILES) && props.get(LoggerProperties.PROPERTY_GRIDFS_FILES) != null) {
                if (props.get(LoggerProperties.PROPERTY_GRIDFS_FILES) instanceof Collection) {
                    Collection<TemporaryFile> temporaryFiles = (Collection<TemporaryFile>) props.get(LoggerProperties.PROPERTY_GRIDFS_FILES);

                    GridFSBucket bucket = GridFSBuckets.create(mongoDatabase, gridFSCollection);

                    HashSet<String> objectIds = new HashSet<>();

                    for (TemporaryFile temporaryFile : temporaryFiles) {

                        try (FileInputStream inputStream = new FileInputStream(temporaryFile)) {
                            objectIds.add(bucket.uploadFromStream(temporaryFile.getName(), inputStream).toHexString());
                        } catch (IOException ex) {
                            LoggerFactory.getLogger(StagingLogAppender.class).error("Exception writing files to database", ex);
                        } finally {
                            temporaryFile.close();
                        }
                    }

                    paxLoggingEvent.getProperties().put(LoggerProperties.PROPERTY_GRIDFS_FILES, objectIds);
                }

            }

            MongoCollection<PaxLoggingEventWrapper> logCollection = mongoDatabase.getCollection(collection, PaxLoggingEventWrapper.class);
            logCollection.insertOne(new PaxLoggingEventWrapper(paxLoggingEvent));

        }
        catch (Exception ex) {
            LoggerFactory.getLogger(StagingLogAppender.class).error("Exception attempting to write to log!", ex);
        }
        finally {
            MDC.remove(PROPERTY_INTERNAL_DISABLE);
        }
    }
}
