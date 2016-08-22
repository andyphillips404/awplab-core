package com.awplab.core.mongodb.log;

import org.apache.log4j.MDC;

/**
 * Created by andyphillips404 on 5/31/16.
 */
public class MDCLoggerAutoClosable implements AutoCloseable {
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