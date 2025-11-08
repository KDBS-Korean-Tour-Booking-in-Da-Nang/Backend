package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TossConfirmRequest;
import com.example.KDBS.dto.request.TossCreateOrderRequest;
import com.example.KDBS.dto.response.TossCreateOrderResponse;
import com.example.KDBS.service.TossPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/toss")
public class TossController {

    private final TossPaymentService tossPaymentService;

    @PostMapping("/orders")
    public ResponseEntity<TossCreateOrderResponse> createOrder(
            @Valid @RequestBody TossCreateOrderRequest req) {
        return ResponseEntity.ok(tossPaymentService.createOrder(req));
    }

    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirm(@Valid @RequestBody TossConfirmRequest req) {
        JSONObject res = tossPaymentService.confirmPayment(req);
        int status = res.containsKey("error") ? 400 : 200;
        return ResponseEntity.status(status).body(res);
    }
}
