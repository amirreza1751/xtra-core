package com.xtra.core.mapper;

import com.xtra.core.model.Stream;
import com.xtra.core.dto.ChannelStart;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class ChannelStartMapper {
    public abstract Stream convertToStream(ChannelStart channelStart);

    public abstract Stream updateStreamFields(ChannelStart channelStart, @MappingTarget Stream stream);
}
