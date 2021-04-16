package com.xtra.core.mapper;

import com.xtra.core.model.AdvancedStreamOptions;
import com.xtra.core.projection.ClassifiedStreamOptions;
import com.xtra.core.utility.Util;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class AdvancedStreamOptionsMapper {

    public abstract ClassifiedStreamOptions convertToClassified(AdvancedStreamOptions advancedStreamOptions);

    @AfterMapping
    void classifyAdvancedStreamOptions(AdvancedStreamOptions advancedStreamOptions, @MappingTarget ClassifiedStreamOptions classifiedStreamOptions) {

        //input key values
        Map<String, String> inputKeyValues = new HashMap<>();
//        if (advancedStreamOptions.getHeaders() != null && !advancedStreamOptions.getHeaders().equals(""))
//            inputKeyValues.put("-headers", advancedStreamOptions.getHeaders());
//        if (advancedStreamOptions.getCookie() != null && !advancedStreamOptions.getCookie().equals(""))
//            inputKeyValues.put("-cookie", advancedStreamOptions.getCookie());

        if (!inputKeyValues.isEmpty())
            classifiedStreamOptions.setInputKeyValues(Util.additionalArguments(inputKeyValues));

        //input flags
        Set<String> inputFlags = new HashSet<>();
//        if (advancedStreamOptions.getGeneratePts() != null && advancedStreamOptions.getGeneratePts())
//            inputFlags.add("-genpts");
//        if (advancedStreamOptions.getStreamAllCodecs() != null && advancedStreamOptions.getStreamAllCodecs())
//            inputFlags.add("-streamAll");

        if (!inputFlags.isEmpty())
            classifiedStreamOptions.setInputFlags(Util.additionalArguments(inputFlags));


        //output key values
        Map<String, String> outputKeyValues = new HashMap<>();
//        if (advancedStreamOptions.getHeaders() != null && !advancedStreamOptions.getHeaders().equals(""))
//            outputKeyValues.put("-headersOutput", advancedStreamOptions.getHeaders());
//        if (advancedStreamOptions.getCookie() != null && !advancedStreamOptions.getCookie().equals(""))
//            outputKeyValues.put("-cookieOutput", advancedStreamOptions.getCookie());

        if (!outputKeyValues.isEmpty())
            classifiedStreamOptions.setOutputKeyValues(Util.additionalArguments(outputKeyValues));


        //output flags
        Set<String> outputFlags = new HashSet<>();
//        if (advancedStreamOptions.getAllowRecording() != null && advancedStreamOptions.getAllowRecording())
//            outputFlags.add("-allowRecordingOutput");
//        if (advancedStreamOptions.getDirectSource() != null && advancedStreamOptions.getDirectSource())
//            outputFlags.add("-directSourceOutput");

        if (!outputFlags.isEmpty())
            classifiedStreamOptions.setOutputFlags(Util.additionalArguments(outputFlags));

    }
}
