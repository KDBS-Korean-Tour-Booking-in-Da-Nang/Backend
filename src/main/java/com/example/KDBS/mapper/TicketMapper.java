package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TicketRequest;
import com.example.KDBS.dto.response.TicketResponse;
import com.example.KDBS.enums.TicketReasonType;
import com.example.KDBS.model.Ticket;
import com.example.KDBS.model.TicketReason;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "message", source = "message")
    Ticket toTicket(TicketRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "reasons", source = "reasons")
    TicketResponse toTicketResponse(Ticket ticket);

    //Helper method for mapping list of TicketReason to list of TicketReasonType
    default TicketReasonType map(TicketReason reason) {
        return reason.getTicketReasonType();
    }
}
