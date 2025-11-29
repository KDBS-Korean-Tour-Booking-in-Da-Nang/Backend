package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.UserActionLogResponse;
import com.example.KDBS.model.UserActionLog;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserActionLogMapper {

    UserActionLogResponse toResponse(UserActionLog log);
}


