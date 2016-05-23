package com.awplab.core.pdf.command;


import com.awplab.core.pdf.parser.ParsedDocument;
import com.awplab.core.pdf.service.PDFService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.net.URL;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "pdf", name="download")
@Service
public class Download implements Action {

    @Reference
    PDFService pdfService;

    @Argument(name = "URL", description = "URL to download pdf for", required = true)
    String url;

    @Override
    public Object execute() throws Exception {

        try (ParsedDocument parsedDocument = pdfService.getParsedDocument(new URL(url))) {
            System.out.println("Done");
        }


        return null;
    }

}
