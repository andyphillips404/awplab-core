package com.awplab.core.jsoup;

/**
 * Created by andyphillips404 on 6/12/17.
 */
public class JsoupException extends Exception {
    public JsoupException() {
    }

    public JsoupException(String message) {
        super(message);
    }

    public JsoupException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsoupException(Throwable cause) {
        super(cause);
    }

    public JsoupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
