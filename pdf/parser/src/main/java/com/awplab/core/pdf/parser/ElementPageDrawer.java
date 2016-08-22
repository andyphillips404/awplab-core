package com.awplab.core.pdf.parser;

/**
 * Created by andyphillips404 on 4/30/16.
 */

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Example PageDrawer subclass with custom rendering.
 */
class ElementPageDrawer extends PageDrawer
{
    private ElementPageDrawerGraphics elementPageDrawerGraphics;
    private boolean debug;

    private List<Element> elements = Collections.synchronizedList(new ArrayList<>());

    public ElementPageDrawerGraphics getElementPageDrawerGraphics() {
        return elementPageDrawerGraphics;
    }

    ElementPageDrawer(PageDrawerParameters parameters, boolean debug) throws IOException
    {
        super(parameters);
        this.debug = debug;
    }

    public List<Element> getElements() {
        return elements;
    }

    @Override
    public void drawPage(Graphics g, PDRectangle pageSize) throws IOException {
        elements.clear();

        elementPageDrawerGraphics = new ElementPageDrawerGraphics((Graphics2D)g);
        super.drawPage(elementPageDrawerGraphics, pageSize);
    }



    /**
     * Glyph bounding boxes.
     */
    @Override
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                             Vector displacement) throws IOException
    {

        elementPageDrawerGraphics.reset();

        // draw glyph
        super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);

        // bbox in EM -> user units
        Shape boundry = new Rectangle2D.Float(0, 0, font.getWidth(code) / 1000, 1);
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        boundry = at.createTransformedShape(boundry);

        GlyphElement glyphElement = new GlyphElement(boundry.getBounds2D(), font, elementPageDrawerGraphics.getLastDrawShape(), unicode, code,
                elementPageDrawerGraphics.getPaint());

        elements.add(glyphElement);

        if (debug) {
            drawDebugBoundry(boundry);
        }
    }

    private void drawDebugBoundry(Shape boundry) {
        // save
        Graphics2D graphics = getGraphics();
        Color color = graphics.getColor();
        Stroke stroke = graphics.getStroke();
        Shape clip = graphics.getClip();

        // draw
        graphics.setClip(graphics.getDeviceConfiguration().getBounds());
        graphics.setColor(Color.RED);
        graphics.setStroke(new BasicStroke(.5f));
        graphics.draw(boundry);

        // restore
        graphics.setStroke(stroke);
        graphics.setColor(color);
        graphics.setClip(clip);
    }

    /**
     * Stroke the path.
     *
     * @throws IOException If there is an IO error while stroking the path.
     */
    @Override
    public void strokePath() throws IOException {

        GeneralPath generalPath = getLinePath();

        super.strokePath();

        elements.add(new PathElement(generalPath.getBounds2D(), getGraphics().getStroke(), getGraphics().getPaint(), generalPath));

        if (debug) {
            drawDebugBoundry(generalPath.getBounds2D());
        }
    }

    /**
     * Fill the path.
     *
     * @param windingRule The winding rule this path will use.
     */
    @Override
    public void fillPath(int windingRule) throws IOException
    {
        // bbox in user units
        GeneralPath generalPath = getLinePath();

        // draw path (note that getLinePath() is now reset)
        super.fillPath(windingRule);

        elements.add(new PathElement(generalPath.getBounds2D(), generalPath, getGraphics().getPaint(), windingRule));

        if (debug) {
            drawDebugBoundry(generalPath.getBounds2D());
        }

    }

    /**
     * Draw the image.
     *
     * @param pdImage The image to draw.
     */
    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        super.drawImage(pdImage);

        if (debug) {

        }


    }


    /**
     * Append a rectangle to the current path.
     */
    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        super.appendRectangle(p0, p1, p2, p3);
    }




    /**
     * Modify the current clipping path by intersecting it with the current path.
     * The clipping path will not be updated until the succeeding painting operator is called.
     *
     * @param windingRule The winding rule which will be used for clipping.
     */
    @Override
    public void clip(int windingRule) {
        super.clip(windingRule);
    }


    /**
     * Starts a new path at (x,y).
     */
    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
    }


    /**
     * Draws a line from the current point to (x,y).
     */
    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
    }


    /**
     * Draws a curve from the current point to (x3,y3) using (x1,y1) and (x2,y2) as control points.
     */
    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        super.curveTo(x1, y1, x2, y2, x3, y3);
    }



    /**
     * Fill the path.
     *
     * @param windingRule The winding rule this path will use.
     */
    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {
        super.fillAndStrokePath(windingRule);
    }



    /**
     * Fill with Shading.
     *
     * @param shadingName The name of the Shading Dictionary to use for this fill instruction.
     */
    @Override
    public void shadingFill(COSName shadingName) throws IOException {
        super.shadingFill(shadingName);
    }





}

