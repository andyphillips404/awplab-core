package com.awplab.core.mongodb.service.codec;

/**
 * Created by andyphillips404 on 5/30/16.
 */
public class BeanCodecDecodeException extends RuntimeException {
    public BeanCodecDecodeException(String message) {
        super(message);
    }

    public BeanCodecDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCodecDecodeException(Throwable cause) {
        super(cause);
    }

    public BeanCodecDecodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BeanCodecDecodeException() {
    }
}
