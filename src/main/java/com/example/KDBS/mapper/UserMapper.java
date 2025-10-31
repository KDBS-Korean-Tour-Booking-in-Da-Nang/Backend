package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.request.UserUpdateRequest;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = ImageMapper.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toUser(UserRegisterRequest request);

    UserResponse toUserResponse(User user);

    void updateUserFromDto(UserUpdateRequest dto, @MappingTarget User user);
}
