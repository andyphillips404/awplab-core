package com.awplab.core.mongodb.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by andyphillips404 on 5/29/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BeanCodecProperties {
    boolean ignoreUnknown() default true;
    boolean autoDetect() default true;
    BeanCodecInclude defaultInclude() default BeanCodecInclude.NOT_EMPTY;
    boolean ignoreInherited() default false;
    boolean ignoreReadOnly() default true;
}
