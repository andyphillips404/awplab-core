package com.awplab.core.mongodb.service;

import com.mongodb.MongoClient;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;

/**
 * Created by andyphillips404 on 5/27/16.
 */
public interface MongoService {

    MongoClient getMongoClient();

    <T> void registerCodec(Class<T> clazz, Codec<T> codec);

    void unregisterCodec(Class clazz);

    <T> void registerCodecProvider(CodecProvider codecProvider);

    void unregisterCodecProvider(CodecProvider codecProvider);


    String CONFIG_FACTORY_NAME = "com.awplab.core.mongodb";

    String PROPERTY_CONNECTION_STRING = "com.awplab.core.mongodb.connectionString";

}
