package com.awplab.core.pdf.parser.text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andyphillips404 on 9/16/15.
 */
public class TextElementsSections extends LinkedHashMap<TextElement, TextElements> {

    protected TextElementsSections() {

    }

    public List<TextElementsSection> getSections() {

        ArrayList<TextElementsSection> textElementsSections = new ArrayList<>();

        for (Map.Entry<TextElement, TextElements> entry : this.entrySet()) {
            textElementsSections.add(new TextElementsSection(entry));
        }

        return textElementsSections;
    }

    public List<String> asText(TextElements.AsTextOptions... option) {
        ArrayList<String> textElementsSections = new ArrayList<>();

        for (TextElementsSection textElementsSection : getSections()) {
            textElementsSections.add(textElementsSection.asElements().asText(option));
        }

        return textElementsSections;

    }


}
