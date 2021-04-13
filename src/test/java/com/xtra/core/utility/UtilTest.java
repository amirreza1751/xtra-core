package com.xtra.core.utility;

import com.xtra.core.model.Options;
import org.junit.jupiter.api.Test;

import java.util.*;

class UtilTest {

    @Test
    void addArgument() {
        HashMap<String, String> keyValues = new HashMap<>();
        keyValues.put("vcodec", "h264");
        keyValues.put("test2", "value2");
        keyValues.put("test3", "value3");
        ArrayList<String> sss = new ArrayList<>();
        for (Map.Entry<String, String> mapElement : keyValues.entrySet()) {
            String key = mapElement.getKey();
            String value = mapElement.getValue();
            sss.add(key);
            sss.add(value);
        }
        Set<String> flags = new HashSet<>();
        flags.add("-s");
        flags.add("-abcd");
        flags.add("-rtew");

        Options options = new Options();
        options.setInputKeyValues(keyValues);
        options.setInputFlags(flags);
        String[] teeemp = flags.toArray(new String[0]);
        String temp2 =  keyValues.toString();
        String [] result = sss.toArray(new String[0]);
    }
}