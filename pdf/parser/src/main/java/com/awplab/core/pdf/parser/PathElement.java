package com.awplab.core.pdf.parser;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * Created by andyphillips404 on 5/10/16.
 */
public class PathElement extends Element {


    private Stroke stroke;
    private Paint paint;
    private GeneralPath path;

    private Paint fillPaint;
    private int windingRule;

    public PathElement(Rectangle2D bounds, Stroke stroke, Paint paint, GeneralPath path) {
        super(bounds);
        this.stroke = stroke;
        this.paint = paint;
        this.path = path;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public Paint getPaint() {
        return paint;
    }

    public GeneralPath getPath() {
        return path;
    }

    public PathElement(Rectangle2D bounds, Stroke stroke, Paint paint, GeneralPath path, Paint fillPaint, int windingRule) {
        super(bounds);
        this.stroke = stroke;
        this.paint = paint;
        this.path = path;
        this.fillPaint = fillPaint;
        this.windingRule = windingRule;
    }

    public PathElement(Rectangle2D bounds, GeneralPath path, Paint fillPaint, int windingRule) {
        super(bounds);
        this.path = path;
        this.fillPaint = fillPaint;
        this.windingRule = windingRule;
    }

    public Paint getFillPaint() {
        return fillPaint;
    }

    public int getWindingRule() {
        return windingRule;
    }
}
