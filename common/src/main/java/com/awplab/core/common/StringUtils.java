package com.awplab.core.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.jar.Pack200;

/**
 * Created by andyphillips404 on 6/9/16.
 */
public final class StringUtils {
    private StringUtils() {
    }

    public static Optional<String> matchAndReplaceAll(String string, String regex, String replacement) {
        if (string == null || regex == null || replacement == null) return Optional.empty();
        return string.matches(regex) ? Optional.of(string.replaceAll(regex, replacement)) : Optional.empty();
    }

    public static Optional<Date> parseDate(String format, String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return Optional.of(simpleDateFormat.parse(dateString));
        }
        catch (ParseException e) {
            return Optional.empty();
        }
    }

    public static Optional<Float> parseFloat(String number) {
        try {
            return Optional.of(Float.parseFloat(number));
        }
        catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInteger(String number) {
        try {
            return Optional.of(Integer.parseInt(number));
        }
        catch (Exception ex) {
            return Optional.empty();
        }
    }

}
