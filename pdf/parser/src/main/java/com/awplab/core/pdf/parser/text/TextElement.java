package com.awplab.core.pdf.parser.text;

import com.awplab.core.pdf.parser.Element;
import com.awplab.core.pdf.parser.GlyphElement;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by andyphillips404 on 1/15/14.
 */
public class TextElement extends Element {
    private String text;
    private List<GlyphElement> glyphElements;
    private int pageNumber;
    //private float averageWidthOfSpace;

    //public float getAverageWidthOfSpace() {
    //    return averageWidthOfSpace;
    //}

    public int getPageNumber() {
        return pageNumber;
    }

    public PDFont getFont() {
        return glyphElements.get(0).getFont();
    }

    public float getSpaceWidth() { return getFont().getSpaceWidth(); }

    public TextElement(int pageNumber, float lineTolerance, String text, List<GlyphElement> glyphElements) {

        this.text = text;
        this.glyphElements = new ArrayList<GlyphElement>(glyphElements);
        this.pageNumber = pageNumber;

        Rectangle2D bounds = null;

        Collections.sort(this.glyphElements, new GlyphElementComparator(lineTolerance));

        if (this.glyphElements.size() > 0) {

            for (GlyphElement glyphElement : glyphElements) {
                if (bounds == null) bounds = glyphElement.getBounds();
                else bounds = bounds.createUnion(glyphElement.getBounds());
            }

        }

        this.setBounds(bounds);

    }

    public String getText() {
        return text;
    }

    public List<GlyphElement> getGlyphElements() {
        return glyphElements;
    }

    public float getStartX() {
        return (float)getBounds().getX();
    }

    public float getStartY() {
        return (float)getBounds().getY();
    }

    public float getEndX() {
        return (float)getBounds().getMaxX();
    }

    public float getEndY() {
        return (float)getBounds().getMaxY();
    }

    public float getCenterX() {
        return (float)getBounds().getCenterX();
    }

    public float getCenterY() {
        return (float)getBounds().getCenterY();
    }


}
