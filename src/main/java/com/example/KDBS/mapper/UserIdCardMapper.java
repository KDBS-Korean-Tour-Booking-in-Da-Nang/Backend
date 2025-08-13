package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.IdCardApiResponse;
import com.example.KDBS.model.UserIdCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = ImageMapper.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserIdCardMapper {
    UserIdCardMapper INSTANCE = Mappers.getMapper(UserIdCardMapper.class);

    // Ignore user & image paths because they are set separately
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "frontImagePath", ignore = true)
    @Mapping(target = "backImagePath", ignore = true)
    UserIdCard toEntity(IdCardApiResponse dto);
}
