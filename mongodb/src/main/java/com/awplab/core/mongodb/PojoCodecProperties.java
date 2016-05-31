package com.awplab.core.mongodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by andyphillips404 on 5/29/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PojoCodecProperties {
    boolean ignoreUnknown() default true;
    boolean autoDetectFields() default true;
    boolean autoDetectMethods() default true;
    PojoCodecInclude defaultInclude() default PojoCodecInclude.NOT_EMPTY;
    boolean ignoreInherited() default false;
}
