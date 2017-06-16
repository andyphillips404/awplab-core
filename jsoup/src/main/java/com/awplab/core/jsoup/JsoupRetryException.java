package com.awplab.core.jsoup;

/**
 * Created by andyphillips404 on 6/12/17.
 */
public class JsoupRetryException extends JsoupException {
    public JsoupRetryException() {
    }

    public JsoupRetryException(String message) {
        super(message);
    }

    public JsoupRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsoupRetryException(Throwable cause) {
        super(cause);
    }

    public JsoupRetryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
