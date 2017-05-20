package com.awplab.core.mongodb.log;

import org.apache.log4j.MDC;

import java.util.Stack;

/**
 * Created by andyphillips404 on 5/31/16.
 */
public class MDCAutoClosable implements AutoCloseable {

    private class Key {
        private String key;
        private Object oldValue;

        public Key(String key, Object oldValue) {
            this.key = key;
            this.oldValue = oldValue;
        }

        public String getKey() {
            return key;
        }

        public Object getOldValue() {
            return oldValue;
        }
    }

    private final Stack<Key> keys =  new Stack<>();

    public MDCAutoClosable(String key, Object value) {
        this.with(key, value);
    }

    public MDCAutoClosable() {
    }

    @Override
    public void close()  {
        while (!keys.empty()) {
            Key key = keys.pop();
            if (key.getOldValue() != null) MDC.put(key.getKey(), key.getOldValue());
            else MDC.remove(key.getKey());

        }
    }


    public MDCAutoClosable with(String key, Object value) {
        Object oldValue = MDC.get(key);
        MDC.put(key, value);
        keys.push(new Key(key, oldValue));
        return this;
    }

}