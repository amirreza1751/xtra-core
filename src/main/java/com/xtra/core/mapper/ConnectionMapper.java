package com.xtra.core.mapper;

import com.xtra.core.model.Connection;
import com.xtra.core.projection.ConnectionDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class ConnectionMapper {
    public abstract ConnectionDetails convertToDetails(Connection connection);
}
