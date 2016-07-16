package com.awplab.core.pdf.service;

import com.awplab.core.common.TemporaryFile;
import com.awplab.core.pdf.parser.ParsedDocument;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 4/25/16.
 */
public interface PDFService {


    ParsedDocument getParsedDocument(URL url);

    ParsedDocument getParsedDocument(File file);

    ParsedDocument getParsedDocument(TemporaryFile file);

    ParsedDocument getParsedDocument(final URL url,
                                     final long networkTimeout, final TimeUnit networkTimeoutUnit,
                                     final long retryMaxExponentialWaitTime, final TimeUnit retryMaxExponentialWaitTimeUnit,
                                     final long retryMaxTimeoutTime, final TimeUnit retryMaxTimeoutTimeUnit);


    String CONFIG_MANAGED_SERVICE_NAME = "com.awplab.core.pdf.service";

    String PROPERTY_DEFAULT_NETWORK_TIMEOUT = "com.awplab.core.pdf.service.defaultNetworkTimeout";

    String PROPERTY_DEFAULT_RETRY_MAX_EXPONENTIAL_WAIT_TIME = "com.awplab.core.pdf.service.defaultRetryMaxExponentialWaitTime";

    String PROPERTY_DEFAULT_RETRY_MAX_RETRY_TIMEOUT = "com.awplab.core.pdf.service.defaultRetryTimeout";

    String PROPERTY_DEFAULT_NETWORK_TIMEOUT_UNIT = "com.awplab.core.pdf.service.defaultNetworkTimeoutUnit";

    String PROPERTY_DEFAULT_RETRY_MAX_EXPONENTIAL_WAIT_TIME_UNIT = "com.awplab.core.pdf.service.defaultRetryMaxExponentialWaitTimeUnit";

    String PROPERTY_DEFAULT_RETRY_MAX_RETRY_TIMEOUT_UNIT = "com.awplab.core.pdf.service.defaultRetryTimeoutUnit";

    String PROPERTY_DEFAULT_RENDER_SCALE = "com.awplab.core.pdf.service.defaultRenderScale";

}
