package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.TranslatedArticleResponse;
import com.example.KDBS.model.Article;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ArticleMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "articleTitleEN", source = "articleTitleEN")
    @Mapping(target = "articleDescriptionEN", source = "articleDescriptionEN")
    @Mapping(target = "articleContentEN", source = "articleContentEN")
    @Mapping(target = "articleTitleKR", source = "articleTitleKR")
    @Mapping(target = "articleDescriptionKR", source = "articleDescriptionKR")
    @Mapping(target = "articleContentKR", source = "articleContentKR")
    void mapTranslatedContentToArticle(TranslatedArticleResponse translatedArticleResponse, @MappingTarget Article article);
}
