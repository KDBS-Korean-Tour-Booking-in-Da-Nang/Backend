package com.example.KDBS.service;

import com.example.KDBS.dto.request.TicketRequest;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TicketMapper;
import com.example.KDBS.model.Ticket;
import com.example.KDBS.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Transactional
    public Ticket createTicket(TicketRequest request) {
        Ticket ticket = ticketMapper.toTicket(request);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));
    }
}
