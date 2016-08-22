package com.awplab.core.mongodb.service.codec;

/**
 * Created by andyphillips404 on 5/30/16.
 */
public class PojoCodecEncodeException extends RuntimeException {
    public PojoCodecEncodeException() {
    }

    public PojoCodecEncodeException(String message) {
        super(message);
    }

    public PojoCodecEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PojoCodecEncodeException(Throwable cause) {
        super(cause);
    }

    public PojoCodecEncodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
