package com.awplab.core.pdf.parser.text;

import com.awplab.core.pdf.parser.Element;
import com.awplab.core.pdf.parser.GlyphElement;
import com.awplab.core.pdf.parser.ParsedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Created by andyphillips404 on 1/15/14.
 */
public class TextElements extends ArrayList<TextElement> {

    static final float LETTER_TOLERANCE_PERCENT = 0.3f;
    static final float WORD_CHARACTER_LINE_TOLERANCE = 0.5f;


    public TextElements(float lineTolerance) {
        this.lineTolerance = lineTolerance;
    }

    public TextElements(float lineTolerance, ParsedDocument parsedDocument) {

        this.lineTolerance = lineTolerance;



        for (int pageNumber = 1; pageNumber <= parsedDocument.getElements().size(); pageNumber++) {

            List<Element> pageElements = parsedDocument.getElements().get(pageNumber - 1);

            ArrayList<GlyphElement> currentPageElements = new ArrayList<>();
            for (Element element : pageElements) {
                if (element instanceof GlyphElement) currentPageElements.add((GlyphElement)element);
            }

            Collections.sort(currentPageElements, new GlyphElementComparator(WORD_CHARACTER_LINE_TOLERANCE));

            int startOfInterest = 0;

            for (int pos = 0; pos < currentPageElements.size(); pos++) {
                if (pos == 0) continue;

                GlyphElement startTextPosition = currentPageElements.get(startOfInterest);
                GlyphElement textPosition = currentPageElements.get(pos);


                GlyphElement lastTextPosition = currentPageElements.get(pos - 1);
                double lastX = lastTextPosition.getBounds().getX() + lastTextPosition.getBounds().getWidth();
                double expectedNegativeCharToleranceX = lastTextPosition.getBounds().getWidth() * LETTER_TOLERANCE_PERCENT;
                double expectedPositiveCharToleranceX = lastTextPosition.getFont().getSpaceWidth() * LETTER_TOLERANCE_PERCENT;

                if ((textPosition.getBounds().getY() != startTextPosition.getBounds().getY()) ||
                        (!textPosition.getFont().equals(startTextPosition.getFont())) ||
                        (pos == (currentPageElements.size() - 1)) ||
                        (textPosition.getBounds().getX() <= (lastX - expectedNegativeCharToleranceX) || textPosition.getBounds().getX() >= (lastX + expectedPositiveCharToleranceX))) {

                    int stop = pos - 1;
                    if (pos == currentPageElements.size() - 1) stop = pos;
                    ArrayList<GlyphElement> glyphElements = new ArrayList<GlyphElement>();
                    String text = "";
                    for (int pos2 = startOfInterest; pos2 <= stop; pos2++) {
                        text += currentPageElements.get(pos2).getUnicode();
                        glyphElements.add(currentPageElements.get(pos2));
                    }

                    this.add(new TextElement(pageNumber, lineTolerance, text, glyphElements));

                    startOfInterest = pos;

                }


            }

        }

        this.sort();





    }



    /**
     * This method will return all sequential elements that, starting with <code>startIndex</code> and
     * ignoring elements that meet the <code>ignore</code> condition, meet the
     * <code>include</code> condition stopping when the first element is encountered that meets the <code>stop</code>
     * condition after the ignore condition
     *
     * @param startIndex index to start finding elements
     * @param include condition to test the element against to include, must not be null
     * @param ignore condition to test the element against to ignore, can be null
     * @param stop condition to test to stop searching, can be null.  The stop element is not included in the list
     * @return all found elements
     */
    public TextElements findElements(int startIndex, TextElementCondition include, TextElementCondition ignore, TextElementCondition stop) {
        if (include == null) throw new NullPointerException("include");

        TextElements elements = new TextElements(lineTolerance);
        for (int x = startIndex; x < this.size(); x++) {
            TextElement textElement = this.get(x);
            if (ignore != null) if (ignore.conditionMet(textElement)) continue;

            if (stop != null) if (stop.conditionMet(textElement)) break;

            if (include.conditionMet(textElement)) elements.add(textElement);

        }

        // resort the elements to ensure proper alignment
        elements.sort();
        return elements;
    }

    public TextElements subset(int startIndex, int length) {
        TextElements elements = new TextElements(lineTolerance);
        for (int x = startIndex; x < startIndex + length; x++) {
            elements.add(this.get(x));
        }

        elements.sort();
        return elements;
    }



    public TextElements findElements(TextElementCondition include, TextElementCondition ignore, TextElementCondition stop) {
        return findElements(0, include, ignore, stop);
    }

    public TextElements findElements(int startIndex, TextElementCondition include) {
        return findElements(startIndex, include, null, null);
    }

    public TextElements findElements(TextElementCondition include) {
        return findElements(0, include);
    }


    public TextElements findSequentialElements(int startIndex, TextElementCondition mustInclude, TextElementCondition ignore) {
        if (mustInclude == null) throw new NullPointerException("mustInclude");
        return findElements(startIndex, mustInclude, ignore, TextElementCondition.not(mustInclude));
    }

    public TextElement findElement(int startIndex, TextElementCondition matchCondition) {
        return findElement(startIndex, matchCondition, null, null);
    }

    public TextElement findElement(TextElementCondition matchCondition) {
        return findElement(0, matchCondition);
    }

    public TextElement findElement(String regex) {
        return findElement(TextElementCondition.textMatches(regex));
    }

    public TextElement findElement(int startIndex, TextElementCondition matchCondition, TextElementCondition ignore, TextElementCondition stop) {
         TextElements elements = findElements(startIndex, TextElementCondition.falseIfLimitMet(1, matchCondition), ignore, stop);
         if (elements.size() > 0) return elements.get(0);
         return null;
    }

    public enum FormFieldType {
        VALUE_RIGHT,
        VALUE_BELOW,
        VALUE_ABOVE
    }

    public String getFormFieldValue(String regexFormField, FormFieldType type, float width, float height) {
        return getFormFieldValue(this.findElement(regexFormField), type, width, height);
    }

    public String getFormFieldValue(String regexFormField, FormFieldType type, float width, float height, float startNegOffset) {
        return getFormFieldValue(this.findElement(regexFormField), type, width, height, startNegOffset);
    }

    public String getFormFieldValue(String regexFormField, FormFieldType type, float width, float height, float startNegOffset, float overlapOffset) {
        return getFormFieldValue(this.findElement(regexFormField), type, width, height, startNegOffset, overlapOffset);
    }

    public String getFormFieldValue(TextElement formFieldNameElement, FormFieldType type, float width, float height) {
        return getFormFieldValue(formFieldNameElement, type, width, height, 0);
    }

    public String getFormFieldValue(TextElement formFieldNameElement, FormFieldType type, float width, float height, float startNegOffset) {
        return getFormFieldValue(formFieldNameElement, type, width, height, startNegOffset, 0);
    }

    /**
     *
     * @param formFieldNameElement
     * @param type
     * @param width
     * @param height
     * @param startNegOffset For ABOVE, BELOW values, this will be the negative offset in the X direction from the start of the fieldNameElement
     *                       for Right, this is the negative offset in the Y direction from the startY of the field form name
     * @param overlapOffset for BELOW values, this will be the negative offset in the Y direction to account for overlap over the name element
     *                      for RIGHT, this is the negative offset in the X direction from the startX of the field form name
     *                      for ABOVE values, this will be the positive offset in teh Y direction to account for overlap
     * @return
     */
    public String getFormFieldValue(TextElement formFieldNameElement, FormFieldType type, float width, float height, float startNegOffset, float overlapOffset) {
        if (formFieldNameElement == null) return null;

        TextElementCondition condition = null;
        switch (type) {
            case VALUE_RIGHT: condition = TextElementCondition.withinRectangleInPage(formFieldNameElement.getPageNumber(), new Rectangle2D.Float(formFieldNameElement.getEndX() - overlapOffset, formFieldNameElement.getStartY() - startNegOffset, width, height)); break;
            case VALUE_ABOVE: condition = TextElementCondition.withinRectangleInPage(formFieldNameElement.getPageNumber(), new Rectangle2D.Float(formFieldNameElement.getStartX() - startNegOffset, formFieldNameElement.getStartY() - height + overlapOffset, width, height)); break;
            case VALUE_BELOW: condition = TextElementCondition.withinRectangleInPage(formFieldNameElement.getPageNumber(), new Rectangle2D.Float(formFieldNameElement.getStartX() - startNegOffset, formFieldNameElement.getEndY() - overlapOffset, width, height)); break;
        }

        TextElements valueElements =  findElements(0, condition, null, null);
        String value = valueElements.asText();
        if (value == null) return null;
        else return value.trim();
    }

    public float lineTolerance = 0;

    public float getLineTolerance() {
        return lineTolerance;
    }

    public void setLineTolerance(float lineTolerance) {
        this.lineTolerance = lineTolerance;
        this.sort();
    }

    public enum AsTextOptions {
        TRIM_OUTPUT,
        ATTEMPT_TO_REASSEMBLE_PARAGRAPHS,
        SINGLE_LINE_CLEAN_SPACES,
        DO_NOT_STRIP_CARRIAGE_RETURNS
    }


    public String asText() {
        return asText(new ArrayList<AsTextOptions>());
    }

    public String asText(AsTextOptions option) {
        return asText(Arrays.asList(option));
    }

    public String asText(AsTextOptions... option) {
        return asText(Arrays.asList(option));
    }

    public String asText(List<AsTextOptions> options) {


        if (this.size() == 0) return null;

        String ret = this.get(0).getText();

        for (int x = 1; x < this.size(); x++) {

            TextElement thisElement = this.get(x);
            TextElement lastElement = this.get(x-1);

            float deltaY = lastElement.getStartY() - thisElement.getStartY();
            if (Math.abs(deltaY) <= lineTolerance) {
                // we are on the same line
                // lets see delta x
                float deltaX = thisElement.getStartX() - lastElement.getEndX();
                if (deltaX > lastElement.getSpaceWidth() && !lastElement.getText().endsWith(" ")) {
                    ret += " ";
                }
            }
            else {
                if (options.contains(AsTextOptions.ATTEMPT_TO_REASSEMBLE_PARAGRAPHS)) ret += (ret.endsWith(" ") || ret.endsWith("-") ? "" : " ");
                else if (!ret.endsWith("\n") && !thisElement.getText().startsWith("\n")) ret +=  "\n";
            }
            ret += thisElement.getText();
        }

        if (options.contains(AsTextOptions.TRIM_OUTPUT)) ret = ret.trim();
        if (!options.contains(AsTextOptions.DO_NOT_STRIP_CARRIAGE_RETURNS)) ret = ret.replaceAll("\r", "");
        if (options.contains(AsTextOptions.SINGLE_LINE_CLEAN_SPACES)) ret = ret.replaceAll("\n", " ").replaceAll(" +", " ").trim();

        return ret;
    }


    public TextElementsSections getElementsAsSections(TextElementCondition header, TextElementCondition ignore, TextElementCondition stop, TextElementCondition includeInSection) {
        TextElementsSections ret = new TextElementsSections();

        TextElements sections = this.findElements(header, ignore, stop);
        for (TextElement sectionElement : sections) {
            TextElementCondition stopCondition = null;
            if (stop == null) {
                stopCondition = header;
            }
            else {
                stopCondition = TextElementCondition.or(header, stop);
            }

            TextElements sectionElements = this.findElements(this.indexOf(sectionElement) + 1,includeInSection, ignore, stopCondition);
            ret.put(sectionElement, sectionElements);
        }

        return ret;
    }


    @Deprecated
    /**
     * Deprecated, please use getElementsAsSections
     */
    public HashMap<TextElement, TextElements> getElementsSeparatedBySections(TextElementCondition section, TextElementCondition ignore, TextElementCondition stop, TextElementCondition includeInSection) {
        HashMap<TextElement, TextElements> ret = new HashMap<TextElement, TextElements>();

        TextElements sections = this.findElements(section, ignore, stop);
        for (TextElement sectionElement : sections) {
            TextElementCondition stopCondition = null;
            if (stop == null) {
                stopCondition = section;
            }
            else {
                stopCondition = TextElementCondition.or(section, stop);
            }

            TextElements sectionElements = this.findElements(this.indexOf(sectionElement) + 1,includeInSection, ignore, stopCondition);
            ret.put(sectionElement, sectionElements);
        }

        return ret;
    }

    public TextElementsTable asTable(TableEvaluateElement evaluateElement, float... columnSeparators ) {
        return this.asTable(evaluateElement, false, columnSeparators);
    }

    public TextElementsTable asTable(TableEvaluateElement evaluateElement, boolean considerStartOnly, float... columnSeparators ) {

        if (this.size() == 0) return null;


        TextElementsTable rows = new TextElementsTable();

        TextElements[] workingRow = null;

        // two pass system, first find the row markers
        for (TextElement element : this) {
            int column = -1;
            for (column = 0; column <= columnSeparators.length; column++) {
                if (column == columnSeparators.length) {
                    if (element.getStartX() > columnSeparators[column-1]) break;
                    column = -1;
                    break;
                }

                float pos = columnSeparators[column];

                if (column == 0) {
                    if (considerStartOnly && element.getStartX() <= pos) break;
                    if (element.getEndX() <= pos) break;
                    continue;
                }

                float priorPos = columnSeparators[column - 1];
                if (considerStartOnly && element.getStartX() >= priorPos && element.getStartX() <= pos) break;
                if (element.getStartX() >= priorPos && element.getEndX() <= pos) break;
            }


            if (column >= 0) {
                TableEvaluateElementType triggerType = evaluateElement.evaluateElement(workingRow, column, element);


                if (triggerType == TableEvaluateElementType.TRIGGER_NEW_ROW_BEFORE_ELEMENT) {
                    if (workingRow != null) rows.add(workingRow);
                    workingRow = new TextElements[columnSeparators.length + 1];
                }

                if (triggerType != TableEvaluateElementType.IGNORE_ELEMENT) {
                    if (workingRow == null) workingRow = new TextElements[columnSeparators.length + 1];
                    if (workingRow[column] == null) workingRow[column] = new TextElements(lineTolerance);
                    workingRow[column].add(element);
                }

                if (triggerType == TableEvaluateElementType.TRIGGER_NEW_ROW_AFTER_ELEMENT) {
                    rows.add(workingRow);
                    workingRow = new TextElements[columnSeparators.length + 1];
                }
            }

        }

        if (workingRow != null) {
            rows.add(workingRow);
        }
        return rows;
    }

    public enum TableEvaluateElementType {
        INCLUDE_ELEMENT,
        IGNORE_ELEMENT,
        TRIGGER_NEW_ROW_AFTER_ELEMENT,
        TRIGGER_NEW_ROW_BEFORE_ELEMENT
    }

    public interface TableEvaluateElement {
        public TableEvaluateElementType evaluateElement(TextElements[] workingRow, int column, TextElement element);
    }

    public void sort() {
        Collections.sort(this, new Comparator<TextElement>() {
            @Override
            public int compare(TextElement o1, TextElement o2) {
                if (o1.getPageNumber() == o2.getPageNumber()) {
                    if (Math.abs(o1.getCenterY() - o2.getCenterY()) <= lineTolerance) {
                        return Float.compare(o1.getStartX(), o2.getStartX());
                    }
                    else {
                        return Float.compare(o1.getCenterY(), o2.getCenterY());
                    }
                }
                else {
                    return Integer.compare(o1.getPageNumber(), o2.getPageNumber());
                }
            }
        });
    }


}

