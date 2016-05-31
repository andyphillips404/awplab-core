package com.awplab.core.mongodb.log;

import com.awplab.core.mongodb.PojoCodecKey;
import org.bson.types.ObjectId;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.util.Map;

/**
 * Created by andyphillips404 on 3/7/15.
 */
public class PaxLoggingEventWrapper {


    private PaxLoggingEvent event = null;

    public PaxLoggingEventWrapper(PaxLoggingEvent event) {
        this.event = event;
    }

    @PojoCodecKey(value = "_id")
    private ObjectId id;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLoggerName() {
        return event.getLoggerName();
    }

    public String getMessage() {
        return event.getMessage();
    }

    public String getRenderedMessage() {
        return event.getRenderedMessage();
    }

    public String getThreadName() {
        return event.getThreadName();
    }

    public String[] getThrowableStrRep() {
        return event.getThrowableStrRep();
    }

    public PaxLocationInfo getLocationInformation() {
         return event.getLocationInformation();
    }

    public long getTimeStamp() {
        return  event.getTimeStamp();
    }

    public String getFQNOfLoggerClass() {
        return event.getFQNOfLoggerClass();
    }

    public Map getProperties() {
        return event.getProperties();
    }



}
