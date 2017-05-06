package com.awplab.core.mongodb.log;


import com.awplab.core.common.TemporaryFile;
import com.awplab.core.mongodb.log.events.LogEventTopics;
import com.awplab.core.mongodb.service.MongoService;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.felix.ipojo.annotations.*;
import org.apache.log4j.MDC;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

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

            Map loggerLevelsMap = ((Map)props.getOrDefault(Log.MDC_KEY_LOGGER_LEVELS, new HashMap<>()));
            Optional<Object> loggerOptional = loggerLevelsMap.keySet().stream()
                    .filter(o -> o.toString().equals(paxLoggingEvent.getLoggerName()) || paxLoggingEvent.getLoggerName().startsWith(o.toString() + "."))
                    .sorted(Comparator.comparingInt(String::length)).findFirst();

            if (loggerOptional.isPresent()) {
                try {
                    Integer level = Integer.parseInt(loggerLevelsMap.get(loggerOptional.get()).toString());
                    if (paxLoggingEvent.getLevel().toInt() >= level)
                        return;
                }
                catch (Exception ignored) {

                }
            }

            String collection = (props.containsKey(Log.MDC_KEY_COLLECTION) && props.get(Log.MDC_KEY_COLLECTION) != null ? props.get(Log.MDC_KEY_COLLECTION).toString() : defaultCollection);
            String gridFSCollection = (props.containsKey(Log.MDC_KEY_GRIDFS_COLLECTION) && props.get(Log.MDC_KEY_GRIDFS_COLLECTION) != null ? props.get(Log.MDC_KEY_GRIDFS_COLLECTION).toString() : defaultGridFSCollection);

            MongoDatabase mongoDatabase = mongoService.getMongoClient().getDatabase(database);

            MongoCollection<Log> logCollection = mongoDatabase.getCollection(collection, Log.class);
            logCollection = logCollection.withWriteConcern(WriteConcern.UNACKNOWLEDGED);
            Log log = new Log(paxLoggingEvent);


            Set<Object> logFiles = new HashSet<>();

            for (Object key : log.getProperties().keySet()) {
                Object value = log.getProperties().get(key);

                if (value instanceof LogFile) {
                    try {
                        if (!((LogFile) value).isSaved()) ((LogFile) value).save(mongoDatabase, gridFSCollection);
                        ((LogFile) value).setKey((String)key);
                        log.getLogFiles().add(((LogFile) value));
                        logFiles.add(key);
                    }
                    catch (IOException ex) {
                        LoggerFactory.getLogger(MongoLogAppender.class).error("Exception attempting to write to grid fs log from file: " + ((TemporaryFile) ((LogFile) value).getTemporaryFile()).getAbsolutePath() + "!", ex);
                    }


                }
            }

            logFiles.forEach(o -> {log.getProperties().remove(o);});

            logCollection.insertOne(log);

            LogEventTopics.postEntryAdded(log, database, collection);

        }
        catch (Exception ex) {
            LoggerFactory.getLogger(MongoLogAppender.class).error("Exception attempting to write to log!", ex);
        }
        finally {
            MDC.remove(PROPERTY_INTERNAL_DISABLE);
        }
    }
}
