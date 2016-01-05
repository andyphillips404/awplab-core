package com.awplab.core.ipojo.command;

import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.List;

/**
 * Created by andyphillips404 on 12/21/15.
 */
public class AnsiPrintToolkit {

    /**
     * Default indentation.
     */
    private static final String DEFAULT_INDENTER = "  ";
    public static final List<String> GOOD_STATES = Arrays.asList("valid", "resolved", "registered");

    /**
     * Ansi buffer.
     */
    private Ansi buffer;

    /**
     * Verbosity mode.
     */
    private boolean verbose = false;

    /**
     * Indentation value.
     */
    private String indenter = DEFAULT_INDENTER;

    public AnsiPrintToolkit() {
        this(Ansi.ansi());
    }

    public AnsiPrintToolkit(Ansi ansi) {
        this.buffer = ansi;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Ansi getBuffer() {
        return buffer;
    }

    public String getIndenter() {
        return indenter;
    }

    public void setIndenter(String indenter) {
        this.indenter = indenter;
    }

    public void printElement(int level, Element element) {
        indent(level);
        // element ns:name in bold
        buffer.a(Ansi.Attribute.INTENSITY_BOLD);
        String ns = element.getNameSpace();
        if (verbose && !isEmpty(ns)) {
            buffer.a(ns);
            buffer.a(":");
        }
        buffer.a(element.getName());
        buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);

        // Then print attributes
        if (element.getAttributes() != null) {
            for (Attribute attribute : element.getAttributes()) {
                printAttribute(attribute);
            }
        }

        // Now print childs Element (incrementing the indentation counter)
        if (element.getElements() != null) {
            for (Element child : element.getElements()) {
                // EOL
                eol();
                printElement((level + 1), child);
            }
        }

    }

    public void printAttribute(Attribute attribute) {

        // First, a separator
        buffer.a(" ");

        // Then the namespace (if verbose)
        String ns = attribute.getNameSpace();
        if (verbose && !isEmpty(ns)) {
            buffer.a(ns);
            buffer.a(":");
        }

        // The print the key/value pair
        buffer.a(attribute.getName());
        buffer.a("=\"");
        String value = attribute.getValue();
        if ("state".equals(attribute.getName())) {
            if (GOOD_STATES.contains(value)) {
                buffer.fg(Ansi.Color.GREEN);
            } else {
                buffer.fg(Ansi.Color.RED);
            }
        }
        buffer.a(Ansi.Attribute.ITALIC);
        buffer.a(value);
        buffer.a(Ansi.Attribute.ITALIC_OFF);
        buffer.reset();
        buffer.a("\"");

    }

    public static boolean isEmpty(String value) {
        return ((value == null) || ("".equals(value)));
    }

    public void indent() {
        indent(1);
    }

    public void indent(int level) {
        for (int i = 0; i < level; i++) {
            buffer.a(indenter);
        }
    }

    public void eol() {
        eol(1);
    }

    public void eol(int level) {
        for (int i = 0; i < level; i++) {
            buffer.a('\n');
        }
    }

    public void red(String message) {
        color(message, Ansi.Color.RED);
    }

    public void green(String message) {
        color(message, Ansi.Color.GREEN);
    }

    public void blue(String message) {
        color(message, Ansi.Color.BLUE);
    }

    public void white(String message) {
        color(message, Ansi.Color.WHITE);
    }

    public void black(String message) {
        color(message, Ansi.Color.BLACK);
    }

    public void cyan(String message) {
        color(message, Ansi.Color.CYAN);
    }

    public void yellow(String message) {
        color(message, Ansi.Color.YELLOW);
    }

    public void magenta(String message) {
        color(message, Ansi.Color.MAGENTA);
    }

    public void color(String message, Ansi.Color color) {
        buffer.fg(color);
        buffer.a(message);
        buffer.reset();
    }

    public void italic(String message) {
        buffer.a(Ansi.Attribute.ITALIC);
        buffer.a(message);
        buffer.a(Ansi.Attribute.ITALIC_OFF);
    }

    public void bold(String message) {
        buffer.a(Ansi.Attribute.INTENSITY_BOLD);
        buffer.a(message);
        buffer.a(Ansi.Attribute.INTENSITY_BOLD_OFF);
    }

    public void underline(String message) {
        buffer.a(Ansi.Attribute.UNDERLINE);
        buffer.a(message);
        buffer.a(Ansi.Attribute.UNDERLINE_OFF);
    }

    public void print(String message) {
        buffer.a(message);
    }


}
