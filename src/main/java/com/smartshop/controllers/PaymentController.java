package com.smartshop.controllers;

import com.smartshop.dtos.PaymentDTO;
import com.smartshop.entity.PaymentStatus;
import com.smartshop.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        try {
            log.info("POST /api/payments - Création d'un paiement pour la commande ID: {}", paymentDTO.getOrderId());
            PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
        } catch (Exception e) {
            log.error("Erreur lors de la création du paiement: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            log.info("GET /api/payments/{} - Récupération du paiement", id);
            PaymentDTO payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentsByOrder(@PathVariable Long orderId) {
        try {
            log.info("GET /api/payments/order/{} - Récupération des paiements de la commande", orderId);
            List<PaymentDTO> payments = paymentService.getPaymentsByOrder(orderId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        log.info("GET /api/payments/status/{} - Récupération des paiements par statut", status);
        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }


    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByStatusPaginated(
            @PathVariable PaymentStatus status, Pageable pageable) {
        log.info("GET /api/payments/status/{}/paginated - Récupération des paiements paginés", status);
        Page<PaymentDTO> payments = paymentService.getPaymentsByStatusPaginated(status, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PaymentDTO>> getPendingPayments() {
        log.info("GET /api/payments/pending - Récupération des paiements en attente");
        List<PaymentDTO> payments = paymentService.getPendingPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/overdue-cheques")
    public ResponseEntity<List<PaymentDTO>> getOverdueCheques() {
        log.info("GET /api/payments/overdue-cheques - Récupération des chèques en retard");
        List<PaymentDTO> payments = paymentService.getOverdueCheques();
        return ResponseEntity.ok(payments);
    }


    @PutMapping("/{id}/encaisser")
    public ResponseEntity<?> encaisserPayment(@PathVariable Long id) {
        try {
            log.info("PUT /api/payments/{}/encaisser - Encaissement du paiement", id);
            PaymentDTO encaissedPayment = paymentService.encaisserPayment(id);
            return ResponseEntity.ok(encaissedPayment);
        } catch (Exception e) {
            log.error("Erreur lors de l'encaissement: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
    }


    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterPayment(@PathVariable Long id, @RequestParam(required = false) String motif) {
        try {
            log.info("PUT /api/payments/{}/rejeter - Rejet du paiement", id);
            PaymentDTO rejectedPayment = paymentService.rejeterPayment(id, motif);
            return ResponseEntity.ok(rejectedPayment);
        } catch (Exception e) {
            log.error("Erreur lors du rejet: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
    }


    @GetMapping("/order/{orderId}/total")
    public ResponseEntity<?> getTotalPaidForOrder(@PathVariable Long orderId) {
        try {
            log.info("GET /api/payments/order/{}/total - Total payé", orderId);
            BigDecimal totalPaid = paymentService.getTotalPaidForOrder(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("totalPaid", totalPaid);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @GetMapping("/order/{orderId}/count")
    public ResponseEntity<?> countPaymentsByOrder(@PathVariable Long orderId) {
        try {
            log.info("GET /api/payments/order/{}/count - Comptage des paiements", orderId);
            long count = paymentService.countPaymentsByOrder(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("paymentsCount", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}