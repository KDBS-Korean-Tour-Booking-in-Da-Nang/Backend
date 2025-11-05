package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.ReactionRequest;
import com.example.KDBS.dto.response.ReactionResponse;
import com.example.KDBS.model.Reaction;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReactionMapper {
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.avatar", target = "userAvatar")
    ReactionResponse toReactionResponse(Reaction reaction);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "targetId", target = "targetId")
    @Mapping(source = "targetType", target = "targetType")
    @Mapping(source = "reactionType", target = "reactionType")
    Reaction toReaction(ReactionRequest request);


}
