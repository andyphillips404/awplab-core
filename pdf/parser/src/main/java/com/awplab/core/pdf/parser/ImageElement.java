package com.awplab.core.pdf.parser;

import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by andyphillips404 on 5/1/16.
 */
public class ImageElement extends Element {
    PDImage pdImage;

    public ImageElement(Rectangle2D bounds, PDImage pdImage) {
        super(bounds);
        this.pdImage = pdImage;
    }

    public PDImage getPdImage() {
        return pdImage;
    }
}
