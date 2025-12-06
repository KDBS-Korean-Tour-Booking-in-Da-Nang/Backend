package com.example.KDBS.service;

import com.example.KDBS.dto.request.TicketRequest;
import com.example.KDBS.dto.response.TicketResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TicketMapper;
import com.example.KDBS.model.Ticket;
import com.example.KDBS.model.TicketReason;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.TicketRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(ticketMapper::toTicketResponse)
                .toList();
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        Ticket ticket = ticketMapper.toTicket(request);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<TicketReason> reasons = request.getReasons().stream()
                .map(reasonType -> TicketReason.builder()
                        .ticketReasonType(reasonType)
                        .ticket(ticket)
                        .build())
                .toList();

        ticket.setUser(user);
        ticket.setReasons(reasons);
        ticketRepository.save(ticket);

        return ticketMapper.toTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));
        return ticketMapper.toTicketResponse(ticket);
    }
}
