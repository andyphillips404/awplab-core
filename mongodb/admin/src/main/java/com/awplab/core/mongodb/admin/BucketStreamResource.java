package com.awplab.core.mongodb.admin;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.vaadin.server.StreamResource;
import org.bson.types.ObjectId;

import java.io.InputStream;

/**
 * Created by andyphillips404 on 9/27/16.
 */
public class BucketStreamResource extends StreamResource {

    private final MongoDatabase database;
    private final String bucket;
    private final ObjectId objectId;

    public BucketStreamResource(MongoDatabase database, String bucket, ObjectId objectId) {
        super((StreamSource) () -> {
            GridFSBucket gridFSBucket = GridFSBuckets.create(database, bucket);
            return gridFSBucket.openDownloadStream(objectId);
        }, gridFSFile(database, bucket, objectId).getFilename());

        this.database = database;
        this.bucket = bucket;
        this.objectId = objectId;
    }

    public static GridFSFile gridFSFile(MongoDatabase database, String bucket, ObjectId objectId) {
        return  GridFSBuckets.create(database, bucket).find(Filters.eq("_id", objectId)).first();

    }

    public GridFSFile gridFSFile() {
        return gridFSFile(database, bucket, objectId);
    }


}
