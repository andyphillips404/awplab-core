package com.awplab.core.jsoup;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Map;

/**
 * Created by andyphillips404 on 2/27/15.
 */
public interface JsoupService {

    JsoupSession getSession();

    JsoupSession getSession(int timeoutMilliSeconds, int retryMaxExponentialWaitMinutes, int retryTimeoutMinutes);

    int getDefaultTimeoutMilliSeconds();

    int getDefaultRetryMaxExponentialWaitMinutes();

    int getDefaultRetryTimeoutMinutes();


}
