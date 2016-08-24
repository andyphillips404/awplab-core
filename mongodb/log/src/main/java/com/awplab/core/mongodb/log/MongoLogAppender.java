package com.awplab.core.mongodb.log;


import com.awplab.core.common.TemporaryFile;
import com.awplab.core.mongodb.service.MongoService;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.InsertOneOptions;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.*;
import org.apache.log4j.MDC;
import org.bson.types.ObjectId;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by andyphillips404 on 3/6/15.
 */

@Component(immediate = true, managedservice = MongoLogAppender.CONFIG_MANAGED_SERVICE_NAME)
@Provides(properties = {
        @StaticServiceProperty(name = PaxLoggingService.APPENDER_NAME_PROPERTY, value = "MongoDbLogAppender", type = "java.lang.String")
})
@Instantiate
public class MongoLogAppender implements PaxAppender {

    public static final String CONFIG_MANAGED_SERVICE_NAME = "com.awplab.core.log.appender";

    public static final String PROPERTY_DEFAULT_DATABASE = "com.awplab.core.log.appender.defaultDatabase";
    @ServiceProperty(name = PROPERTY_DEFAULT_DATABASE)
    private String defaultDatabase;

    public static final String PROPERTY_DEFAULT_COLLECTION = "com.awplab.core.log.appender.defaultCollection";
    @ServiceProperty(name = PROPERTY_DEFAULT_COLLECTION, value = "log")
    private String defaultCollection;

    public static final String PROPERTY_DEFAULT_GRIDFS_COLLECTION = "com.awplab.core.log.appender.defaultGridFSCollection";
    @ServiceProperty(name = PROPERTY_DEFAULT_GRIDFS_COLLECTION, value = "log-fs")
    private String defaultGridFSCollection;

    @Requires
    MongoService mongoService;

    private static final String PROPERTY_INTERNAL_DISABLE = "mongodb.appender.internalDisable";





    @Override
    public synchronized void doAppend(PaxLoggingEvent paxLoggingEvent) {

        MDC.put(PROPERTY_INTERNAL_DISABLE, true);
        if (paxLoggingEvent.getLoggerName().equals(this.getClass().getName())) return;

        try {
            Map props = paxLoggingEvent.getProperties();
            if ((Boolean) props.getOrDefault(PROPERTY_INTERNAL_DISABLE, Boolean.FALSE)) return;
            if ((Boolean) props.getOrDefault(Log.MDC_KEY_DISABLE, Boolean.FALSE)) return;

            String database = (props.containsKey(Log.MDC_KEY_DATABASE) && props.get(Log.MDC_KEY_DATABASE) != null ? props.get(Log.MDC_KEY_DATABASE).toString() : defaultDatabase);
            if (database == null) return;

            String collection = (props.containsKey(Log.MDC_KEY_COLLECTION) && props.get(Log.MDC_KEY_COLLECTION) != null ? props.get(Log.MDC_KEY_COLLECTION).toString() : defaultCollection);
            String gridFSCollection = (props.containsKey(Log.MDC_KEY_GRIDFS_COLLECTION) && props.get(Log.MDC_KEY_GRIDFS_COLLECTION) != null ? props.get(Log.MDC_KEY_GRIDFS_COLLECTION).toString() : defaultGridFSCollection);

            MongoDatabase mongoDatabase = mongoService.getMongoClient().getDatabase(database);

            MongoCollection<Log> logCollection = mongoDatabase.getCollection(collection, Log.class);
            logCollection = logCollection.withWriteConcern(WriteConcern.UNACKNOWLEDGED);
            Log log = new Log(paxLoggingEvent);

            Set<String> fileKeys = new HashSet<>();
            for (Object key : log.getProperties().keySet()) {
                Object value = log.getProperties().get(key);
                if (value instanceof TemporaryFile) {
                    GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, gridFSCollection);
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream((TemporaryFile) value);
                        ObjectId objectId = gridFSBucket.uploadFromStream(((TemporaryFile) value).getName(), fileInputStream);
                        log.getLogFiles().add(new LogFiles(key.toString(), objectId, gridFSCollection));
                        fileKeys.add(key.toString());
                    }
                    catch (IOException ex) {
                        LoggerFactory.getLogger(MongoLogAppender.class).error("Exception attempting to write to grid fs log from file: " + ((TemporaryFile) value).getAbsolutePath() + "!", ex);
                    }
                    finally {
                        IOUtils.closeQuietly(fileInputStream);
                    }

                }
            }

            fileKeys.forEach(log.getProperties()::remove);

            logCollection.insertOne(log);

        }
        catch (Exception ex) {
            LoggerFactory.getLogger(MongoLogAppender.class).error("Exception attempting to write to log!", ex);
        }
        finally {
            MDC.remove(PROPERTY_INTERNAL_DISABLE);
        }
    }
}
