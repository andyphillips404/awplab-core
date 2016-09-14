package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.log.LogFile;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.vaadin.server.StreamResource;
import org.bson.types.ObjectId;

import java.io.InputStream;

/**
 * Created by andyphillips404 on 8/27/16.
 */
public class BucketStreamResource implements StreamResource.StreamSource {
    private final MongoDatabase database;
    private final String bucket;
    private final ObjectId objectId;

    public BucketStreamResource(MongoDatabase database, String bucket, ObjectId objectId) {
        this.database = database;
        this.bucket = bucket;
        this.objectId = objectId;
    }

    @Override
    public InputStream getStream() {
        GridFSBucket gridFSBucket = GridFSBuckets.create(database, bucket);

        return gridFSBucket.openDownloadStream(objectId);
    }
}
