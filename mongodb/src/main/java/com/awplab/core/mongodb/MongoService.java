package com.awplab.core.mongodb;

import com.mongodb.MongoClient;
import org.bson.codecs.Codec;

import java.util.List;

/**
 * Created by andyphillips404 on 5/27/16.
 */
public interface MongoService {

    MongoClient getMongoClient();

    <T> void registerCodec(Class<T> clazz, Codec<T> codec);

    void unregisterCodec(Class clazz);

}
