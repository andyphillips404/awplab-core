package com.awplab.core.pdf.parser;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by andyphillips404 on 4/30/16.
 */
public class GlyphElement extends Element {
    private PDFont font;
    private Shape shape;
    private String unicode;
    private int code;
    private Paint paint;

    public GlyphElement(Rectangle2D bounds, PDFont font, Shape shape, String unicode, int code, Paint paint) {
        super(bounds);
        this.font = font;
        this.shape = shape;
        this.unicode = unicode;
        this.code = code;
        this.paint = paint;
    }

    public PDFont getFont() {
        return font;
    }

    public Shape getShape() {
        return shape;
    }

    public String getUnicode() {
        return unicode;
    }

    public int getCode() {
        return code;
    }

    public Paint getPaint() {
        return paint;
    }

}
