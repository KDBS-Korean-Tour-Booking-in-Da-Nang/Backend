package com.example.KDBS.model;

import com.example.KDBS.enums.TicketReasonType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_reasons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketReason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_reason", nullable = false)
    private TicketReasonType ticketReasonType;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @JsonBackReference
    private Ticket ticket;
}
