package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TicketRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.model.Ticket;
import com.example.KDBS.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping
    public ApiResponse<List<Ticket>> getAllTickets() {
        return ApiResponse.<List<Ticket>>builder()
                .result(ticketService.getAllTickets())
                .build();
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<Ticket> getTicketById(@PathVariable("ticketId") Long ticketId) {
        return ApiResponse.<Ticket>builder()
                .result(ticketService.getTicketById(ticketId))
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<Ticket> createTicket(@RequestBody TicketRequest request) {
        return ApiResponse.<Ticket>builder()
                .result(ticketService.createTicket(request))
                .build();
    }
}
