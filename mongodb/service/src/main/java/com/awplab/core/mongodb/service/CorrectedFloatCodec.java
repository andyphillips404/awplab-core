package com.awplab.core.mongodb.service;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import static java.lang.String.format;

/**
 * Created by andyphillips404 on 5/3/17.
 */
@Instantiate
@Component(immediate = true)
@Provides(specifications = Codec.class)
/* NEEDED UNTIL BUG FIX IN MONGODB */
public class CorrectedFloatCodec implements Codec<Float> {

    /**
     * Encodes and decodes {@code Float} objects.
     *
     * @since 3.0
     */
    @Override
    public void encode(final BsonWriter writer, final Float value, final EncoderContext encoderContext) {
        writer.writeDouble(value);
    }

    @Override
    public Float decode(final BsonReader reader, final DecoderContext decoderContext) {
        double value = reader.readDouble();
        if (value < -Float.MAX_VALUE || value > Float.MAX_VALUE) {
            throw new BsonInvalidOperationException(format("%s can not be converted into a Float.", value));
        }
        return (float) value;
    }

    @Override
    public Class<Float> getEncoderClass() {
        return Float.class;
    }



}
