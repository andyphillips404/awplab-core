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
}
