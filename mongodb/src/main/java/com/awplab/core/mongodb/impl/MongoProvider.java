package com.awplab.core.mongodb.impl;

import com.awplab.core.mongodb.MongoService;
import com.awplab.core.mongodb.codec.PojoCodecProvider;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.apache.felix.ipojo.annotations.*;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 5/27/16.
 */

// TODO:  Allow multiple instances or connections to database.   Don't make this a singleton?  Not Urgent, worth looking at
@Instantiate
@Component(immediate = true, publicFactory=false, managedservice = MongoProvider.CONFIG_MANAGED_SERVICE_NAME)
@Provides(specifications = MongoService.class)
public class MongoProvider implements MongoService, CodecProvider {

    public static final String CONFIG_MANAGED_SERVICE_NAME = "com.hdscores.v3.mongodb.provider";

    private MongoClient mongoClient;

    public static final String PROPERTY_CONNECTION_STRING = "com.hdscores.v3.mongodb.provider.connectionString";
    @ServiceProperty(name = PROPERTY_CONNECTION_STRING)
    private String connectionString;

    private Logger logger = LoggerFactory.getLogger(MongoProvider.class);

    private Map<Class, Codec> codecs = Collections.synchronizedMap(new HashMap<>());

    @Bind(aggregate = true, optional = true)
    private void bindCodec(Codec codec) {

        if (codecs.containsKey(codec.getEncoderClass())) {
            logger.warn("Codec for class: " + codec.getEncoderClass() + " already exists!  Overwriting!");
        }
        registerCodec(codec.getEncoderClass(), codec);

        logger.info("Registered codec service for class: " + codec.getEncoderClass());
    }

    @Unbind(aggregate = true, optional = true)
    private void unbindCodec(Codec codec) {
        unregisterCodec(codec.getEncoderClass());

        logger.info("Unregistered codec service for class: " + codec.getEncoderClass());
    }

    @Validate
    private void start() {
        try {
            if (mongoClient == null) {


                CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                        CodecRegistries.fromProviders(this),
                        MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(new PojoCodecProvider()));

                MongoClientOptions.Builder clientOptions = MongoClientOptions.builder()
                        .codecRegistry(codecRegistry);

                if (connectionString == null) {
                    mongoClient = new MongoClient(new ServerAddress(), clientOptions.build());
                } else {
                    mongoClient = new MongoClient(new MongoClientURI(connectionString, clientOptions));
                }

                MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
                Document document = adminDatabase.runCommand(new Document("serverStatus", 1));

                logger.debug("Server Started: \n" + document.toJson());

                logger.info("Started up MongoClient!");
            }
            else {
                logger.warn("Unable to startup, MongoClient instance already exists?");

            }
        }
        catch (Exception ex) {
            logger.error("Unable to start MongoClient!", ex);
        }
    }

    @Invalidate
    private void stop() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
            }
            logger.info("Shut down MongoClient!");
        }
        catch (Exception ex) {
            logger.error("Unable to stop MongoClinet!", ex);
        }
    }

    @Override
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @Override
    public <T> void registerCodec(Class<T> clazz, Codec<T> codec) {
        codecs.put(clazz, codec);
    }

    @Override
    public void unregisterCodec(Class clazz) {
        codecs.remove(clazz);
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return (Codec<T>)codecs.get(clazz);
    }
}
