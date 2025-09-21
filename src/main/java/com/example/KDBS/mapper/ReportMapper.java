package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.ReportRequest;
import com.example.KDBS.dto.response.ReportResponse;
import com.example.KDBS.dto.response.ReportSummaryResponse;
import com.example.KDBS.model.Report;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    // Request -> Entity
    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "reporter", ignore = true)   // set ở service theo userEmail
    @Mapping(target = "status", ignore = true)     // set mặc định ở @PrePersist
    @Mapping(target = "reportedAt", ignore = true) // set ở @PrePersist
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "resolvedBy", ignore = true)
    @Mapping(target = "adminNote", ignore = true)
    Report toEntity(ReportRequest request);

    // Entity -> Response
    @Mapping(target = "reportId", source = "reportId")
    @Mapping(target = "reporterUsername", source = "reporter.username")
    @Mapping(target = "reporterEmail", source = "reporter.email")
    @Mapping(target = "resolvedByUsername", source = "resolvedBy.username")
    // targetTitle, targetAuthor sẽ set thủ công trong service vì cần query post/comment
    @Mapping(target = "targetTitle", ignore = true)
    @Mapping(target = "targetAuthor", ignore = true)
    ReportResponse toResponse(Report entity);

    // Entity -> SummaryResponse
    @Mapping(target = "reporterUsername", source = "reporter.username")
    @Mapping(target = "reportCount", constant = "0L")
    @Mapping(target = "targetTitle", ignore = true)
    @Mapping(target = "targetAuthor", ignore = true)
    @Mapping(target = "reasons", source = "reasons") // map Set -> String
    ReportSummaryResponse toSummaryResponse(Report entity);

    // Custom method: Set<String> -> String
    default String mapReasons(Set<String> reasons) {
        return reasons != null ? String.join(", ", reasons) : null;
    }
}
