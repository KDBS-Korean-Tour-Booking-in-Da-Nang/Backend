package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TicketRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.TicketResponse;
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
    public ApiResponse<List<TicketResponse>> getAllTickets() {
        return ApiResponse.<List<TicketResponse>>builder()
                .result(ticketService.getAllTickets())
                .build();
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<TicketResponse> getTicketById(@PathVariable("ticketId") Long ticketId) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.getTicketById(ticketId))
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<TicketResponse> createTicket(@RequestBody TicketRequest request) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.createTicket(request))
                .build();
    }
}
