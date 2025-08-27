package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.PostRequest;
import com.example.KDBS.dto.response.HashtagResponse;
import com.example.KDBS.dto.response.PostImgResponse;
import com.example.KDBS.dto.response.PostResponse;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.PostHashtag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PostMapper {

    @Mapping(target = "forumPostId", ignore = true)// Title not in PostRequest; set to empty
    @Mapping(target = "react", constant = "0") // Initial react count
    @Mapping(target = "user", ignore = true) // Set user manually in service
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "hashtags", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // Handled by @PrePersist
    ForumPost toEntity(PostRequest dto);

    @Mapping(target = "hashtags", expression = "java(mapHashtags(entity))")
    @Mapping(target = "images", expression = "java(mapImages(entity))")
    PostResponse toResponse(ForumPost entity);

    // default helper to convert hashtags
    default List<HashtagResponse> mapHashtags(ForumPost entity) {
        if (entity.getHashtags() == null) return Collections.emptyList();
        return entity.getHashtags().stream()
                .map(PostHashtag::getHashtag)
                .map(h -> new HashtagResponse(h.getHashtagId(), h.getContent()))
                .collect(Collectors.toList());
    }

    // default helper to convert images
    default List<PostImgResponse> mapImages(ForumPost entity) {
        if (entity.getImages() == null) return Collections.emptyList();
        return entity.getImages().stream()
                .map(img -> new PostImgResponse(img.getImgPath()))
                .collect(Collectors.toList());
    }

}