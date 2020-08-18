package com.xtra.core.utility;

public class Util {
    boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String removeQuotations(String input) {
        return input.replace("\"", "");
    }
}
