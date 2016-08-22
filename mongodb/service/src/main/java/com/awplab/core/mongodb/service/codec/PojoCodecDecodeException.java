package com.awplab.core.mongodb.service.codec;

/**
 * Created by andyphillips404 on 5/30/16.
 */
public class PojoCodecDecodeException extends RuntimeException {
    public PojoCodecDecodeException(String message) {
        super(message);
    }

    public PojoCodecDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PojoCodecDecodeException(Throwable cause) {
        super(cause);
    }

    public PojoCodecDecodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PojoCodecDecodeException() {
    }
}
