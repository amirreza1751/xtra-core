package com.xtra.core.mapper;

import com.xtra.core.dto.StreamDetailsView;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.StreamInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class StreamMapper {
    public abstract StreamDetailsView copyStreamInfo(StreamInfo streamInfo, @MappingTarget StreamDetailsView streamDetailsView);

    public abstract StreamDetailsView copyProgressInfo(ProgressInfo progressInfo, @MappingTarget StreamDetailsView streamDetailsView);
}
