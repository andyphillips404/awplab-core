package com.awplab.core.mongodb.codec;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Created by andyphillips404 on 5/29/16.
 */
public class PojoCodecProvider implements CodecProvider {
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return new PojoCodec<>(clazz, registry.get(Document.class));
    }

}
