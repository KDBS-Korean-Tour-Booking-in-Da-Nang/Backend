package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.IdCardApiResponse;
import com.example.KDBS.model.UserIdCard;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserIdCardMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "frontImagePath", ignore = true)
    @Mapping(target = "backImagePath", ignore = true)

    @Mapping(source = "addressEntities.province", target = "province")
    @Mapping(source = "addressEntities.district", target = "district")
    @Mapping(source = "addressEntities.ward", target = "ward")
    @Mapping(source = "addressEntities.street", target = "street")
    UserIdCard toEntity(IdCardApiResponse dto);
}
