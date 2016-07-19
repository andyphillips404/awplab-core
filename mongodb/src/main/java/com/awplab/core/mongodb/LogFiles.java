package com.awplab.core.mongodb;

import org.bson.types.ObjectId;

/**
 * Created by andyphillips404 on 7/18/16.
 */
public class LogFiles {
    private String key;
    private ObjectId objectId;
    private String bucket;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public LogFiles(String key, ObjectId objectId, String bucket) {
        this.key = key;
        this.objectId = objectId;
        this.bucket = bucket;
    }
}
