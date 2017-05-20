package com.awplab.core.mongodb.log;

import com.awplab.core.common.TemporaryFile;
import com.awplab.core.mongodb.service.BeanCodecKey;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by andyphillips404 on 7/18/16.
 */
public class LogFile {
    private String key;
    private ObjectId fileObjectId;
    private String bucket;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ObjectId getFileObjectId() {
        return fileObjectId;
    }

    public void setFileObjectId(ObjectId fileObjectId) {
        this.fileObjectId = fileObjectId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public LogFile(String key, ObjectId fileObjectId, String bucket) {
        this.key = key;
        this.fileObjectId = fileObjectId;
        this.bucket = bucket;
    }

    public LogFile() {
    }

    public LogFile(TemporaryFile temporaryFile) {
        this.temporaryFile = temporaryFile;
    }

    @BeanCodecKey(ignore = true)
    public GridFSFile getGridFSFile(MongoDatabase mongoDatabase) {
        return GridFSBuckets.create(mongoDatabase, bucket).find(Filters.eq("_id", fileObjectId)).first();
    }

    @BeanCodecKey(ignore = true)
    private TemporaryFile temporaryFile;

    @BeanCodecKey(ignore = true)
    public TemporaryFile getTemporaryFile() {
        return temporaryFile;
    }


    @BeanCodecKey(ignore = true)
    public boolean isSaved() {
        return (fileObjectId != null);
    }

    public void save(MongoDatabase database) throws IOException {
        if (temporaryFile == null) return;

        try (FileInputStream fileInputStream = new FileInputStream(temporaryFile)) {
            ObjectId objectId = GridFSBuckets.create(database, bucket).uploadFromStream(temporaryFile.getName(), fileInputStream);
            this.setFileObjectId(objectId);
            this.setBucket(bucket);
        }
        finally {
            temporaryFile.close();
        }

        temporaryFile = null;

    }


}
