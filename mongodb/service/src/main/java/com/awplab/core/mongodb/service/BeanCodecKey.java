package com.awplab.core.mongodb.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by andyphillips404 on 5/29/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface BeanCodecKey {
    String value() default DEFAULT_VALUE;
    BeanCodecInclude include() default BeanCodecInclude.DEFAULT;
    boolean ignore() default false;
    static final String DEFAULT_VALUE = "#FIELD_OR_METHOD_NAME#";
    
}
