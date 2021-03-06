package com.awplab.core.mongodb.service;

import com.awplab.core.mongodb.service.codec.BeanCodecProvider;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.apache.felix.ipojo.annotations.*;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 5/27/16.
 */

@Component(immediate = true, name = MongoService.CONFIG_FACTORY_NAME)
@Provides(specifications = MongoService.class)
public class MongoProvider implements MongoService, CodecProvider {


    private MongoClient mongoClient;

    @Property(name = PROPERTY_CONNECTION_STRING, mandatory = true)
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

    @ServiceController(value = false, specification = MongoService.class)
    private boolean serviceController;

    @Validate
    private void start() {
        try {
            if (mongoClient == null) {
                serviceController = false;

                CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                        CodecRegistries.fromProviders(this),
                        MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(new BeanCodecProvider()));

                MongoClientOptions.Builder clientOptions = MongoClientOptions.builder()
                        .codecRegistry(codecRegistry);

                mongoClient = new MongoClient(new MongoClientURI(connectionString, clientOptions));

                MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
                Document document = adminDatabase.runCommand(new Document("serverStatus", 1));

                logger.debug("Server Started: \n" + document.toJson());

                logger.info("Started up MongoClient!");

                serviceController = true;
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

            serviceController = false;

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

    @Updated
    private void updated() {
        // don't update if the service has not been started already
        if (mongoClient != null) {
            logger.info("Restarting mongo client due to reconfiguration!");
            stop();
            start();
        }
    }

    @Override
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @Override
    public <T> void registerCodec(Class<T> clazz, Codec<T> codec) {
        codecs.put(clazz, codec);
        updated();
    }

    @Override
    public void unregisterCodec(Class clazz) {
        codecs.remove(clazz);
        updated();
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return (Codec<T>)codecs.get(clazz);
    }
}
