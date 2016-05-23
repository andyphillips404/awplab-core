package com.awplab.core.pdf.service.provider;

import com.awplab.core.common.TemporaryFile;
import com.awplab.core.pdf.parser.ParsedDocument;
import com.awplab.core.pdf.service.PDFService;
import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.felix.ipojo.annotations.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 4/25/16.
 */
@Component(immediate = true, publicFactory=false, managedservice = PDFProvider.CONFIG_MANAGED_SERVICE_NAME)
@Instantiate
@Provides(specifications = PDFService.class)
public class PDFProvider implements PDFService {
    private Logger logger = LoggerFactory.getLogger(PDFProvider.class);

    public static final String CONFIG_MANAGED_SERVICE_NAME = "com.awplab.core.pdf.service.provider";

    public static final String PROPERTY_DEFAULT_NETWORK_TIMEOUT = "com.awplab.core.pdf.service.provider.defaultNetworkTimeout";

    public static final String PROPERTY_DEFAULT_RETRY_MAX_EXPONENTIAL_WAIT_TIME = "com.awplab.core.pdf.service.provider.defaultRetryMaxExponentialWaitTime";

    public static final String PROPERTY_DEFAULT_RETRY_MAX_RETRY_TIMEOUT = "com.awplab.core.pdf.service.provider.defaultRetryTimeout";

    public static final String PROPERTY_DEFAULT_NETWORK_TIMEOUT_UNIT = "com.awplab.core.pdf.service.provider.defaultNetworkTimeoutUnit";

    public static final String PROPERTY_DEFAULT_RETRY_MAX_EXPONENTIAL_WAIT_TIME_UNIT = "com.awplab.core.pdf.service.provider.defaultRetryMaxExponentialWaitTimeUnit";

    public static final String PROPERTY_DEFAULT_RETRY_MAX_RETRY_TIMEOUT_UNIT = "com.awplab.core.pdf.service.provider.defaultRetryTimeoutUnit";

    @ServiceProperty(name = PROPERTY_DEFAULT_NETWORK_TIMEOUT, value = "5")
    private long defaultNetworkTimeout;

    @ServiceProperty(name = PROPERTY_DEFAULT_RETRY_MAX_EXPONENTIAL_WAIT_TIME, value = "2")
    private long defaultRetryMaxExponentialWaitTime;

    @ServiceProperty(name = PROPERTY_DEFAULT_RETRY_MAX_RETRY_TIMEOUT, value = "10")
    private long defaultRetryTimeout;

    @ServiceProperty(name = PROPERTY_DEFAULT_NETWORK_TIMEOUT_UNIT, value = "MINUTES")
    private String defaultNetworkTimeoutUnit;

    @ServiceProperty(name = PROPERTY_DEFAULT_RETRY_MAX_EXPONENTIAL_WAIT_TIME_UNIT, value = "MINUTES")
    private String defaultRetryMaxExponentialWaitTimeUnit;

    @ServiceProperty(name = PROPERTY_DEFAULT_RETRY_MAX_RETRY_TIMEOUT_UNIT, value = "MINUTES")
    private String defaultRetryTimeoutUnit;


    private File downloadDirectory = null;

    private Map<PDDocument, List<File>> tempFiles = new ConcurrentHashMap<>();

    @Validate
    public void start() {
        downloadDirectory = new File(SystemUtils.getJavaIoTmpDir(), UUID.randomUUID().toString());
        downloadDirectory.mkdirs();
    }

    @Invalidate
    public void stop() {
        if (downloadDirectory != null) {
            FileUtils.deleteQuietly(downloadDirectory);
        }
    }

    @Override
    public ParsedDocument getParsedDocument(URL url) {
        return getParsedDocument(url,
                defaultNetworkTimeout, TimeUnit.valueOf(defaultNetworkTimeoutUnit),
                defaultRetryMaxExponentialWaitTime, TimeUnit.valueOf(defaultRetryMaxExponentialWaitTimeUnit),
                defaultRetryTimeout, TimeUnit.valueOf(defaultRetryTimeoutUnit));
    }

    @Override
    public ParsedDocument getParsedDocument(final URL url,
                                            final long networkTimeout, final TimeUnit networkTimeoutUnit,
                                            final long retryMaxExponentialWaitTime,  final TimeUnit retryMaxExponentialWaitTimeUnit,
                                            final long retryTimeoutTime, final TimeUnit retryTimeoutTimeUnit) {

        TemporaryFile downloadedFile = TemporaryFile.randomFile();

        Callable<Boolean> attempt = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout((int)networkTimeoutUnit.toMillis(networkTimeout));
                    connection.setReadTimeout((int)networkTimeoutUnit.toMillis(networkTimeout));
                    connection.setInstanceFollowRedirects(true);
                    connection.connect();
                    int code = connection.getResponseCode();
                    String contentType = connection.getContentType();
                    if (code == HttpURLConnection.HTTP_OK && (contentType != null && contentType.toUpperCase().startsWith("APPLICATION/PDF"))) {
                        InputStream input = connection.getInputStream();
                        FileUtils.deleteQuietly(downloadedFile);
                        FileUtils.copyInputStreamToFile(input, downloadedFile);
                        return downloadedFile.exists();
                    }
                    else {
                        return false;
                    }

                }
                catch (IOException ex) {
                    downloadedFile.close();
                    throw ex;
                }
            }
        };

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .withWaitStrategy(WaitStrategies.exponentialWait(100, retryMaxExponentialWaitTime, retryMaxExponentialWaitTimeUnit))
                .withStopStrategy(StopStrategies.stopAfterDelay(retryTimeoutTime, retryTimeoutTimeUnit))
                .retryIfRuntimeException()
                .retryIfException()
                .retryIfResult(Predicates.<Boolean>equalTo(false))
                .build();


        boolean gotFile = false;
        try {
            gotFile = retryer.call(attempt);
        }
        catch (RetryException es) {
            logger.error("Maximum retries achieved attempting to download PDF from url: " + url, es);
            downloadedFile.close();
            return null;
        }
        catch (Exception ex) {
            logger.error("Unable to download PDF from url: " + url, ex);
            downloadedFile.close();
            return null;
        }

        try {
            return new ParsedDocument(downloadedFile);
        }
        catch (IOException ex) {
            logger.error("IOException attempting to return new parsed document!", ex);
            downloadedFile.close();
        }

        return null;
    }

    @Override
    public ParsedDocument getParsedDocument(File file) {

        try {
            return new ParsedDocument(PDDocument.load(file));
        }
        catch (IOException ex) {
            logger.error("IOException trying to read PDF document: " + file.toString());
        }

        return null;
    }

    @Override
    public ParsedDocument getParsedDocument(TemporaryFile file) {

        try {
            return new ParsedDocument(file);
        }
        catch (IOException ex) {
            logger.error("IOException trying to read PDF document: " + file.toString());
        }

        return null;
    }
}
