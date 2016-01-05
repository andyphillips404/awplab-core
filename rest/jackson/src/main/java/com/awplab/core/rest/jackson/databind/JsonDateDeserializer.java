package com.awplab.core.rest.jackson.databind;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: andyphillips404
 * Date: 8/15/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonDateDeserializer extends JsonDeserializer<Date> {
    //private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
        String dateString = jsonParser.getText();
        try {
            return dateFormat.parse(dateString);
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
