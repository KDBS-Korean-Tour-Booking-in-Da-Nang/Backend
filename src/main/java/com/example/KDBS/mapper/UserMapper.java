package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.StaffCreateRequest;
import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.request.UserUpdateRequest;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", source = "role")
    User toUser(UserRegisterRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "staffTask", source = "staffTask")
    User toStaff(StaffCreateRequest request);

    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "dob", source = "dob")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "address", source = "address")
    void updateUserFromDto(UserUpdateRequest dto, @MappingTarget User user);
}
