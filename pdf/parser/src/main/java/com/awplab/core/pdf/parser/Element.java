package com.awplab.core.pdf.parser;


import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by andyphillips404 on 4/25/16.
 */
public class Element {
    private Rectangle2D bounds;

    protected Element() {
    }

    protected void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
    }

    public Element(Rectangle2D bounds) {
        this.bounds = bounds;
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

}
