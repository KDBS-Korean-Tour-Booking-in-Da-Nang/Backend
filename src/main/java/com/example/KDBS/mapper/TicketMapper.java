package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TicketRequest;
import com.example.KDBS.model.Ticket;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "message", source = "message")
    @Mapping(target = "reasons", source = "reasons")
    Ticket toTicket(TicketRequest request);
}
