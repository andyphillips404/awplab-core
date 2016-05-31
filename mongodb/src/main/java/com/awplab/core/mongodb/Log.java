package com.awplab.core.mongodb;

import org.apache.log4j.MDC;
import org.bson.types.ObjectId;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 5/31/16.
 */
public class Log {
    public static String MDC_KEY_DISABLE = "mongodbDisable";
    public static String MDC_KEY_DATABASE = "mongodbDatabase";
    public static String MDC_KEY_COLLECTION = "mongodbCollection";

    @PojoCodecKey(value = "_id")
    private ObjectId id;

    private String loggerName;
    private String message;
    private String renderedMessage;
    private String threadName;
    private String[] throwableStrRep;
    private PaxLocationInfo locationInfo;
    private Date timeStamp;
    private String FQNOfLoggerClass;
    private Map properties;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Log() {
    }

    public Log(PaxLoggingEvent loggingEvent) {
        this.loggerName = loggingEvent.getLoggerName();
        this.message = loggingEvent.getMessage();
        this.renderedMessage = loggingEvent.getRenderedMessage();
        this.threadName = loggingEvent.getThreadName();
        this.throwableStrRep = loggingEvent.getThrowableStrRep();
        this.locationInfo = loggingEvent.getLocationInformation();
        this.timeStamp = new Date(loggingEvent.getTimeStamp());
        this.FQNOfLoggerClass = loggingEvent.getFQNOfLoggerClass();
        this.properties = new HashMap<>();
        for (Object key : loggingEvent.getProperties().keySet()) {
            if (key instanceof String) {
                properties.put(((String) key).replaceAll("\\.", "\\-"), loggingEvent.getProperties().get(key));
            }
        }




    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRenderedMessage() {
        return renderedMessage;
    }

    public void setRenderedMessage(String renderedMessage) {
        this.renderedMessage = renderedMessage;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String[] getThrowableStrRep() {
        return throwableStrRep;
    }

    public void setThrowableStrRep(String[] throwableStrRep) {
        this.throwableStrRep = throwableStrRep;
    }

    public PaxLocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(PaxLocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFQNOfLoggerClass() {
        return FQNOfLoggerClass;
    }

    public void setFQNOfLoggerClass(String FQNOfLoggerClass) {
        this.FQNOfLoggerClass = FQNOfLoggerClass;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }



    public static class MDCLoggerAutoClosable implements AutoCloseable {
        private String oldDatabase;
        private String oldCollection;

        public MDCLoggerAutoClosable(String database, String collection) {
            if (MDC.get(Log.MDC_KEY_DATABASE) != null) oldDatabase = MDC.get(Log.MDC_KEY_DATABASE).toString();
            MDC.put(Log.MDC_KEY_DATABASE, database);

            if (MDC.get(Log.MDC_KEY_COLLECTION) != null) oldCollection = MDC.get(Log.MDC_KEY_COLLECTION).toString();
            MDC.put(Log.MDC_KEY_COLLECTION, collection);

        }


        @Override
        public void close() throws Exception {
            if (oldDatabase == null) MDC.remove(Log.MDC_KEY_DATABASE);
            else MDC.put(Log.MDC_KEY_DATABASE, oldDatabase);
            if (oldCollection == null) MDC.remove(Log.MDC_KEY_COLLECTION);
            else MDC.put(Log.MDC_KEY_COLLECTION, oldCollection);

        }
    }

    public static class MDCAutoClosable implements AutoCloseable {
        private final String key;

        private final Object oldValue;

        public MDCAutoClosable(String key, Object value) {
            oldValue = MDC.get(key);
            MDC.put(key, value);
            this.key = key;
        }

        @Override
        public void close() throws Exception {
            if (oldValue != null) MDC.put(key, oldValue);
            else MDC.remove(key);
        }
    }
}
