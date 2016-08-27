package com.awplab.core.mongodb.service.codec;

/**
 * Created by andyphillips404 on 5/30/16.
 */
public class BeanCodecEncodeException extends RuntimeException {
    public BeanCodecEncodeException() {
    }

    public BeanCodecEncodeException(String message) {
        super(message);
    }

    public BeanCodecEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCodecEncodeException(Throwable cause) {
        super(cause);
    }

    public BeanCodecEncodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
