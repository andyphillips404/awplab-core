package com.awplab.core.mongodb.log;

/**
 * Created by andyphillips404 on 5/31/16.
 */
public class MDCLoggerAutoClosable extends MDCAutoClosable {

    public MDCLoggerAutoClosable(String database, String collection) {
        this.with(Log.MDC_KEY_DATABASE, database).with(Log.MDC_KEY_COLLECTION, collection);

    }

    public MDCLoggerAutoClosable(String database, String collection, String gridFSCollection) {
        this.with(Log.MDC_KEY_DATABASE, database).with(Log.MDC_KEY_COLLECTION, collection).with(Log.MDC_KEY_GRIDFS_COLLECTION, collection);

    }
}