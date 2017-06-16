package com.awplab.core.jsoup.impl;

import com.awplab.core.jsoup.JsoupService;
import com.awplab.core.jsoup.JsoupSession;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;

/**
 * Created by andyphillips404 on 2/27/15.
 */
@SuppressWarnings("PackageAccessibility")
@Component(immediate = true, publicFactory=false, managedservice = "com.awplab.core.jsoup")
@Instantiate
@Provides
public class JsoupProvider implements JsoupService {

    @ServiceProperty(name = "defaultTimeoutMilliSeconds", value = "240000")
    private int defaultTimeoutMilliSeconds = 240000;

    @ServiceProperty(name = "defaultRetryMaxExponentialWaitMinutes", value = "2")
    private int defaultRetryMaxExponentialWaitMinutes = 2;


    @ServiceProperty(name = "defaultRetryTimeoutMinutes", value = "10")
    private int defaultRetryTimeoutMinutes = 10;

    @Override
    public int getDefaultTimeoutMilliSeconds() {
        return defaultTimeoutMilliSeconds;
    }

    @Override
    public int getDefaultRetryMaxExponentialWaitMinutes() {
        return defaultRetryMaxExponentialWaitMinutes;
    }

    @Override
    public int getDefaultRetryTimeoutMinutes() {
        return defaultRetryTimeoutMinutes;
    }


    @Override
    public JsoupSession getSession() {
        return new JsoupSession(defaultTimeoutMilliSeconds, defaultRetryMaxExponentialWaitMinutes, defaultRetryTimeoutMinutes);
    }

    @Override
    public JsoupSession getSession(int timeoutMilliSeconds, int retryMaxExponentialWaitMinutes, int retryTimeoutMinutes) {
        return new JsoupSession(timeoutMilliSeconds, retryMaxExponentialWaitMinutes, retryTimeoutMinutes);
    }
}
