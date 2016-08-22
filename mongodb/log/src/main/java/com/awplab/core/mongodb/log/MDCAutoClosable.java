package com.awplab.core.mongodb.log;

import org.apache.log4j.MDC;

/**
 * Created by andyphillips404 on 5/31/16.
 */
public class MDCAutoClosable implements AutoCloseable {
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