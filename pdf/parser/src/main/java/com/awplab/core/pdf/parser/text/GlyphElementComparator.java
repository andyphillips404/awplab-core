package com.awplab.core.pdf.parser.text;

import com.awplab.core.pdf.parser.GlyphElement;

import java.util.Comparator;

/**
 * Created by andyphillips404 on 5/22/16.
 */
public class GlyphElementComparator implements Comparator<GlyphElement> {


    private float lineTolerance;

    public GlyphElementComparator(float lineTolerance) {

        this.lineTolerance = lineTolerance;
    }

    @Override
    public int compare(GlyphElement o1, GlyphElement o2) {

        if (Math.abs(o1.getBounds().getY() - o2.getBounds().getY()) < lineTolerance) {
            return Double.compare(o1.getBounds().getX(), o2.getBounds().getX());
        }
        else {
            return Double.compare(o1.getBounds().getY(), o2.getBounds().getY());
        }
    }

}
