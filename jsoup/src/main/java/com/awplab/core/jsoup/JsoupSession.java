package com.awplab.core.jsoup;

import com.awplab.core.jsoup.impl.JsoupProvider;
import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Created by andyphillips404 on 6/14/17.
 */
public class JsoupSession  {

    private int timeoutMilliSeconds = 240000;

    private int retryMaxExponentialWaitMinutes = 2;

    private int retryTimeoutMinutes = 10;

    public void setTimeoutMilliSeconds(int timeoutMilliSeconds) {
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }

    public void setRetryMaxExponentialWaitMinutes(int retryMaxExponentialWaitMinutes) {
        this.retryMaxExponentialWaitMinutes = retryMaxExponentialWaitMinutes;
    }

    public void setRetryTimeoutMinutes(int retryTimeoutMinutes) {
        this.retryTimeoutMinutes = retryTimeoutMinutes;
    }

    public int getTimeoutMilliSeconds() {
        return timeoutMilliSeconds;
    }

    public int getRetryMaxExponentialWaitMinutes() {
        return retryMaxExponentialWaitMinutes;
    }

    public int getRetryTimeoutMinutes() {
        return retryTimeoutMinutes;
    }

    public JsoupSession(int timeoutMilliSeconds, int retryMaxExponentialWaitMinutes, int retryTimeoutMinutes) {
        this.timeoutMilliSeconds = timeoutMilliSeconds;
        this.retryMaxExponentialWaitMinutes = retryMaxExponentialWaitMinutes;
        this.retryTimeoutMinutes = retryTimeoutMinutes;
    }

    public JsoupSession() {
        BundleContext bundleContext = FrameworkUtil.getBundle(JsoupSession.class).getBundleContext();

        ServiceReference<JsoupService> serviceReference = bundleContext.getServiceReference(JsoupService.class);
        if (serviceReference != null) {
            JsoupService jsoupService = bundleContext.getService(serviceReference);
            this.timeoutMilliSeconds = jsoupService.getDefaultTimeoutMilliSeconds();
            this.retryMaxExponentialWaitMinutes = jsoupService.getDefaultRetryMaxExponentialWaitMinutes();
            this.retryTimeoutMinutes = jsoupService.getDefaultRetryTimeoutMinutes();
        }

    }

    private Predicate<Document> retryDocumentPredicate;
    private Predicate<Connection.Response> retryResponsePredicate;

    public Predicate<Document> getRetryDocumentPredicate() {
        return retryDocumentPredicate;
    }

    public void setRetryDocumentPredicate(Predicate<Document> retryDocumentPredicate) {
        this.retryDocumentPredicate = retryDocumentPredicate;
    }

    public Predicate<Connection.Response> getRetryResponsePredicate() {
        return retryResponsePredicate;
    }

    public void setRetryResponsePredicate(Predicate<Connection.Response> retryResponsePredicate) {
        this.retryResponsePredicate = retryResponsePredicate;
    }

    private Document lastDocument;

    public Document getLastDocument() {
        return lastDocument;
    }

    private Connection.Response lastResponse;

    public Connection.Response getLastResponse() {
        return lastResponse;
    }

    private void setLastDocument(Connection.Response response) {
        if (response != null) {
            try {
                lastDocument = response.parse();
            } catch (IOException e) {
                lastDocument = null;
            }
        }
        else {
            lastResponse = null;
        }

    }



    private void setLastResponse(Connection.Response response) {
        this.lastResponse = response;
        setLastDocument(response);
    }
    /**
     * Return a jSoup document from the URL, using defaultTimeoutMilliSeconds and defaultMaxAttempts before failing.
     *
     * Will log an error to the log if the attempt is uncessful before return null;
     *
     * @param url  URL to get
     * @return jSoup document if found, or null if not
     */
    public Document getDocument(String url) throws JsoupException {
        return getDocument(Connection.Method.GET, url, null, null, null);
    }

    public Document getDocument(String url, Map<String, String> cookies, Map<String, String> headers) throws JsoupException {
        return getDocument(Connection.Method.GET, url, null, cookies, headers);
    }

    public Document getDocument(String url, Map<String, String> cookies) throws JsoupException {
        return getDocument(Connection.Method.GET, url, null, cookies, null);
    }

    public Document getDocument(Connection.Method method, String url, Map<String, ? extends Object> data, Map<String, String> cookies, Map<String, String> headers) throws JsoupException {
        return getDocument(method, url, data, cookies, headers, null);
    }

    public Document getDocument(Connection.Method method, String url, Map<String, ? extends Object> data, Map<String, String> cookies, Map<String, String> headers, String postDataCharset) throws JsoupException {
        return getDocument(method, url, data, cookies, headers, postDataCharset, timeoutMilliSeconds, retryMaxExponentialWaitMinutes, retryTimeoutMinutes);
    }

    public Document getDocument(Connection.Method method, String url, Map<String, ? extends Object> data, Map<String, String> cookies, Map<String, String> headers, String postDataCharset, int timeoutMilliseconds, int retryMaxExponentialWaitMinutes, int retryTimeoutMinutes) throws JsoupException {
        Connection.Response response = getResponse(method, url, data, cookies, headers, postDataCharset, timeoutMilliseconds, retryMaxExponentialWaitMinutes, retryTimeoutMinutes);
        if (response != null) try {
            return response.parse();
        } catch (IOException e) {
            throw new JsoupException(e);
        }

        return null;
    }


    public Connection.Response getResponse(final Connection.Method method, final String url, final Map<String, ? extends Object> data, final Map<String, String> cookies, final Map<String, String> headers) throws JsoupException {
        return getResponse(method, url, data, cookies, headers, null);
    }


    public Connection.Response getResponse(final Connection.Method method, final String url, final Map<String, ? extends Object> data, final Map<String, String> cookies, final Map<String, String> headers, final String postDataCharset) throws JsoupException {
        return getResponse(method, url, data, cookies, headers, postDataCharset, timeoutMilliSeconds, retryMaxExponentialWaitMinutes, retryTimeoutMinutes);
    }

    public Connection.Response getResponse(final Connection.Method method, final String url, final Map<String, ? extends Object> data, final Map<String, String> cookies, final Map<String, String> headers, final String postDataCharset,
                                           final int timeoutMilliseconds, final int retryMaxExponentialWaitMinutes, final int retryTimeoutMinutes) throws JsoupException {

        return getResponse(method, url, data, cookies, headers, postDataCharset, timeoutMilliseconds, retryMaxExponentialWaitMinutes, retryTimeoutMinutes, false);
    }

    public Connection.Response getResponse(final Connection.Method method, final String url, final Map<String, ? extends Object> data, final Map<String, String> cookies, final Map<String, String> headers, final String postDataCharset,
                                           final int timeoutMilliseconds, final int retryMaxExponentialWaitMinutes, final int retryTimeoutMinutes, final boolean ignoreHttpErrors) throws JsoupException {
        Logger logger = LoggerFactory.getLogger(JsoupProvider.class);

        Callable<Connection.Response> attempt = new Callable<Connection.Response>() {
            @Override
            public Connection.Response call() throws Exception {
                Connection connection = Jsoup.connect(url).timeout(timeoutMilliseconds).maxBodySize(0);
                if (cookies != null) connection = connection.cookies(cookies);
                if (data != null) {

                    ArrayList<Connection.KeyVal> keyValPairs = new ArrayList<>();
                    for (Map.Entry<String, ? extends Object> entry : data.entrySet()) {
                        if (entry.getValue() instanceof Collection) {
                            for (Object value : (Collection)entry.getValue()) {
                                keyValPairs.add(HttpConnection.KeyVal.create(entry.getKey(), value.toString()));
                            }
                        }
                        if (entry.getValue() instanceof Object[]) {
                            for (Object value : (Object[])entry.getValue()) {
                                keyValPairs.add(HttpConnection.KeyVal.create(entry.getKey(), value.toString()));
                            }

                        }
                        keyValPairs.add(HttpConnection.KeyVal.create(entry.getKey(), entry.getValue().toString()));
                    }

                    connection.data(keyValPairs);
                }
                if (headers != null) {
                    for (String header : headers.keySet()) {
                        connection.header(header, headers.get(header));
                    }
                }
                connection.method(method);
                if (postDataCharset != null) connection.postDataCharset(postDataCharset);
                connection.validateTLSCertificates(false);
                connection.ignoreHttpErrors(ignoreHttpErrors);
                return connection.execute();
            }
        };


        //noinspection PackageAccessibility
        Retryer<Connection.Response> retryer = RetryerBuilder.<Connection.Response>newBuilder()
                .withWaitStrategy(WaitStrategies.exponentialWait(retryMaxExponentialWaitMinutes, TimeUnit.MINUTES))
                .withStopStrategy(StopStrategies.stopAfterDelay(retryTimeoutMinutes, TimeUnit.MINUTES))
                .retryIfException(throwable -> !(throwable instanceof InterruptedException))
                .retryIfResult(response -> {
                    if (response == null) return true;

                    setLastResponse(response);

                    if (retryResponsePredicate != null) {
                        if (retryResponsePredicate.test(response)) return true;
                    }
                    if (retryDocumentPredicate != null) {
                        try {
                            Document document = response.parse();
                            if (retryDocumentPredicate.test(document)) return true;
                        } catch (Exception ignored) {
                            return true;
                        }

                    }

                    return false;
                })
                .build();

        setLastResponse(null);

        Connection.Response result = null;
        try {
            result = retryer.call(attempt);
        }
        catch (RetryException es) {
            throw new JsoupRetryException("Retry exception getting response from url: " + url, es);
        }
        catch (Exception ex) {
            throw new JsoupException("Exception getting response from url: " + url, ex);
        }


        setLastResponse(result);

        return result;
    }

    public Document postDocument(String url, Map<String, ? extends Object> params) throws JsoupException {
        return getDocument(Connection.Method.POST, url, params, null, null);
    }

    public Document postDocument(String url, Map<String, ? extends Object> params, Map<String, String> cookies) throws JsoupException {
        return getDocument(Connection.Method.POST, url, params, cookies, null);
    }

    public Document postDocument(String url, Map<String, ? extends Object> params, Map<String, String> cookies, Map<String, String> headers) throws JsoupException {
        return getDocument(Connection.Method.POST, url, params, cookies, headers);
    }

    public Document postDocument(String url, Map<String, ? extends Object> params, Map<String, String> cookies, Map<String, String> headers, String postDataCharset) throws JsoupException {
        return getDocument(Connection.Method.POST, url, params, cookies, headers, postDataCharset);
    }

    public Map<String, String> getCookies(Connection.Method method, String url, Map<String, ? extends Object> data) throws JsoupException {
        Connection.Response response = getResponse(method, url, data, null, null);
        if (response != null)
            return response.cookies();

        return null;

    }

    public Map<String, String> getCookies(String url) throws JsoupException {
        return getCookies(Connection.Method.GET, url, null);
    }

    public Map<String, String> getCookies(String url, Map<String, ? extends Object> data) throws JsoupException {
        return getCookies(Connection.Method.POST, url, data);
    }

    public Node getNextNonEmptyNode(Node input) {
        if (input == null) return null;

        Node sibling = input.nextSibling();
        if (sibling == null) return null;
        if (sibling instanceof TextNode && ((TextNode) sibling).text().replaceAll("\u00A0", " ").trim().length() == 0) {
            return getNextNonEmptyNode(sibling);
        }
        return sibling;
    }


    public TextNode getNextNonEmptyTextNode(Node input) {
        if (input == null) return null;

        Node sibling = getNextNonEmptyNode(input);
        while (sibling != null && !(sibling instanceof TextNode)) {
            sibling = getNextNonEmptyNode(sibling);
        }
        return (TextNode)sibling;
    }

    public TextNode getFirstNonEmptyTextNodeChild(Element parent) {
        if (parent == null) return null;

        if (parent.childNodeSize() == 0) return null;

        if (parent.childNode(0) instanceof TextNode && ((TextNode) parent.childNode(0)).text().replaceAll("\u00A0", " ").trim().length() > 0) {
            return (TextNode)parent.childNode(0);
        }
        else {
            return getNextNonEmptyTextNode(parent.childNode(0));
        }

    }

    public Node getFirstNonEmptyNodeChild(Element parent) {
        if (parent == null) return null;

        if (parent.childNodeSize() == 0) return null;

        if (parent.childNode(0) instanceof Element) {
            return parent.childNode(0);
        }

        if (parent.childNode(0) instanceof TextNode && ((TextNode) parent.childNode(0)).text().replaceAll("\u00A0", " ").trim().length() > 0) {
            return parent.childNode(0);
        }
        else {
            return getNextNonEmptyNode(parent.childNode(0));
        }

    }
}
