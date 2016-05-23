/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.awplab.core.pdf.parser;

import com.awplab.core.common.TemporaryFile;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * Example showing custom rendering by subclassing PageDrawer.
 * 
 * <p>If you want to do custom graphics processing rather than Graphics2D rendering, then you should
 * subclass {@link PDFGraphicsStreamEngine} instead. Subclassing PageDrawer is only suitable for
 * cases where the goal is to render onto a Graphics2D surface.
 *
 * @author John Hewson
 */
public class ParsedDocument implements AutoCloseable
{
    private Logger logger = LoggerFactory.getLogger(ParsedDocument.class);

    final private PDDocument pdDocument;

    private BufferedImage bufferedImage = null;

    private List<List<Element>> elements = new ArrayList<>();

    private boolean debugMode = false;

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public PDDocument getPDDocument() {
        return pdDocument;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public ParsedDocument(PDDocument pdDocument) {
        this.pdDocument = pdDocument;

        processDocument();
    }


    private void processDocument() {

        try {
            ElementRenderer renderer = new ElementRenderer(pdDocument, debugMode);
            bufferedImage =  renderer.renderImage(0, 3.0f);
            for (ElementPageDrawer elementPageDrawer : renderer.getPageDrawerList()) {
                elements.add(new ArrayList<>(elementPageDrawer.getElements()));
            }
        }
        catch (IOException ex) {
            logger.error("IO Exception processing document",ex);
        }
        logger.info("Done Processing Document");
    }

    public List<List<Element>> getElements() {
        return elements;
    }

    /**
     * Example PDFRenderer subclass, uses ElementPageDrawer for custom rendering.
     */
    private static class ElementRenderer extends PDFRenderer
    {
        private boolean debug;

        private ElementRenderer(PDDocument document, boolean debug)
        {
            super(document);
        }

        private List<ElementPageDrawer> pageDrawerList = new ArrayList<>();

        List<ElementPageDrawer> getPageDrawerList() {
            return pageDrawerList;
        }

        @Override
        protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException
        {
            ElementPageDrawer elementPageDrawer = new ElementPageDrawer(parameters, debug);
            pageDrawerList.add(elementPageDrawer);
            return elementPageDrawer;
        }

    }




    @Override
    public void close() throws Exception {
        pdDocument.close();
    }
}
