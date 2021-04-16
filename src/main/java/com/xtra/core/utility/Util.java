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

    public static String [] additionalArguments(Map<String, String> keyValues){
        if (keyValues != null){
            ArrayList<String> temp = new ArrayList<>();
            for (Map.Entry<String, String> mapElement : keyValues.entrySet()) {
                String key = mapElement.getKey();
                String value = mapElement.getValue();
                temp.add(key);
                temp.add(value);
            }
            return temp.toArray(new String[0]);
        } else
            return new String[0];
    }
    public static String [] additionalArguments(Set<String> flags){
        if (flags != null)
            return flags.toArray(new String[0]);
        else
            return new String[0];
    }
}
