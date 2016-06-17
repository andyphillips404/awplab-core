package com.awplab.core.mongodb;

import org.bson.codecs.Codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by andyphillips404 on 5/29/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PojoCodecKey {
    String value() default DEFAULT_VALUE;
    PojoCodecInclude include() default PojoCodecInclude.DEFAULT;
    boolean ignore() default false;
    static final String DEFAULT_VALUE = "#FIELD_OR_METHOD_NAME#";
    
}
