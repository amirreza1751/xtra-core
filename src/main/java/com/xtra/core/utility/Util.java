package com.xtra.core.utility;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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
