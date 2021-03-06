package com.awplab.core.mongodb.service.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Created by andyphillips404 on 5/29/16.
 */
public class BeanCodecProvider implements CodecProvider {
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return new BeanCodec<>(clazz, registry);
    }

}
