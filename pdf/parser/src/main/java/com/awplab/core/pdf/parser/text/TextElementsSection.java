package com.awplab.core.pdf.parser.text;

import java.util.Map;

/**
 * Created by andyphillips404 on 9/16/15.
 */
public class TextElementsSection
{
    private TextElement header;
    private TextElements content;

    protected TextElementsSection(TextElement header, TextElements content) {
        this.header = header;
        this.content = content;
    }

    protected TextElementsSection(Map.Entry<TextElement, TextElements> entry) {
        this.header = entry.getKey();
        this.content = entry.getValue();
    }

    public TextElements asElements() {
        TextElements allElements = new TextElements(content.getLineTolerance());
        allElements.add(header);
        allElements.addAll(content);
        return allElements;
    }

    public TextElement getHeader() {
        return header;
    }

    public TextElements getContent() {
        return content;
    }
}
