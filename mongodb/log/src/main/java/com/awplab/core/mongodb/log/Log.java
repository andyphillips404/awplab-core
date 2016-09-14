package com.awplab.core.mongodb.log;

import com.awplab.core.mongodb.service.BeanCodecKey;
import org.bson.types.ObjectId;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.util.*;

/**
 * Created by andyphillips404 on 5/31/16.
 */
public class Log {
    public static String MDC_KEY_DISABLE = "mongodb-disable";
    public static String MDC_KEY_DATABASE = "mongodb-database";
    public static String MDC_KEY_COLLECTION = "mongodb-collection";
    public static String MDC_KEY_GRIDFS_COLLECTION = "mongodb-gridfs-collection";

    @BeanCodecKey(value = "_id")
    private ObjectId id;

    private String loggerName;
    private String message;
    private String renderedMessage;
    private String threadName;
    private String[] throwableStrRep;
    private LocationInfo locationInfo;
    private Date timeStamp;
    private String FQNOfLoggerClass;
    private Map properties;
    private Set<LogFile> logFiles = new HashSet<>();

    private String level;

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
        this.locationInfo = new LocationInfo(loggingEvent.getLocationInformation());
        this.timeStamp = new Date(loggingEvent.getTimeStamp());
        this.FQNOfLoggerClass = loggingEvent.getFQNOfLoggerClass();
        this.properties = new HashMap<>();
        for (Object key : loggingEvent.getProperties().keySet()) {
            if (key instanceof String) {
                properties.put(((String) key).replaceAll("\\.", "\\-"), loggingEvent.getProperties().get(key));
            }
        }

        level = loggingEvent.getLevel().toString();

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

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfo locationInfo) {
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


    public Set<LogFile> getLogFiles() {
        return logFiles;
    }

    public void setLogFiles(Set<LogFile> logFiles) {
        this.logFiles = logFiles;
    }


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public static class LocationInfo implements PaxLocationInfo {

        private String fileName;
        private String className;
        private String lineNumber;
        private String methodName;

        public LocationInfo() {
        }

        public LocationInfo(PaxLocationInfo source) {
            if (source == null) return;

            this.fileName = source.getFileName();
            this.className = source.getClassName();
            this.lineNumber = source.getLineNumber();
            this.methodName = source.getMethodName();
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setLineNumber(String lineNumber) {
            this.lineNumber = lineNumber;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public String getLineNumber() {
            return lineNumber;
        }

        @Override
        public String getMethodName() {
            return methodName;
        }
    }
}
