package com.smartshop.controllers;


import com.smartshop.dtos.CreateOrderDTO;
import com.smartshop.dtos.OrderDTO;
import com.smartshop.entity.OrderStatus;
import com.smartshop.services.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        try {
            log.info("POST /api/orders - Création d'une commande pour le client ID: {}", createOrderDTO.getClientId());
            OrderDTO createdOrder = orderService.createOrder(createOrderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la commande: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("GET /api/orders - Récupération de toutes les commandes");
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<OrderDTO>> getAllOrdersPaginated(Pageable pageable) {
        log.info("GET /api/orders/paginated - Récupération des commandes paginées");
        Page<OrderDTO> orders = orderService.getAllOrdersPaginated(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            log.info("GET /api/orders/{} - Récupération de la commande", id);
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<?> getOrderByReference(@PathVariable String reference) {
        try {
            log.info("GET /api/orders/reference/{} - Récupération de la commande", reference);
            OrderDTO order = orderService.getOrderByReference(reference);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getOrdersByClient(@PathVariable Long clientId) {
        try {
            log.info("GET /api/orders/client/{} - Récupération des commandes du client", clientId);
            List<OrderDTO> orders = orderService.getOrdersByClient(clientId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/client/{clientId}/paginated")
    public ResponseEntity<?> getOrdersByClientPaginated(@PathVariable Long clientId, Pageable pageable) {
        try {
            log.info("GET /api/orders/client/{}/paginated - Récupération des commandes du client paginées", clientId);
            Page<OrderDTO> orders = orderService.getOrdersByClientPaginated(clientId, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /api/orders/status/{} - Récupération des commandes par statut", status);
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/pending")
    public ResponseEntity<List<OrderDTO>> getPendingOrders() {
        log.info("GET /api/orders/pending - Récupération des commandes en attente");
        List<OrderDTO> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/client/{clientId}/history")
    public ResponseEntity<?> getClientOrderHistory(@PathVariable Long clientId) {
        try {
            log.info("GET /api/orders/client/{}/history - Historique des commandes", clientId);
            List<OrderDTO> orders = orderService.getClientOrderHistory(clientId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            log.info("PUT /api/orders/{}/confirm - Confirmation de la commande", id);
            OrderDTO confirmedOrder = orderService.confirmOrder(id);
            return ResponseEntity.ok(confirmedOrder);
        } catch (Exception e) {
            log.error("Erreur lors de la confirmation de la commande: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            log.info("PUT /api/orders/{}/cancel - Annulation de la commande", id);
            OrderDTO canceledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(canceledOrder);
        } catch (Exception e) {
            log.error("Erreur lors de l'annulation de la commande: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable Long id, @RequestParam(required = false) String reason) {
        try {
            log.info("PUT /api/orders/{}/reject - Rejet de la commande", id);
            OrderDTO rejectedOrder = orderService.rejectOrder(id, reason);
            return ResponseEntity.ok(rejectedOrder);
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la commande: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
    }


    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /api/orders/count/status/{} - Comptage des commandes", status);
        long count = orderService.countOrdersByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/client/{clientId}/total-spent")
    public ResponseEntity<?> getTotalSpentByClient(@PathVariable Long clientId) {
        try {
            log.info("GET /api/orders/client/{}/total-spent - Total dépensé", clientId);
            BigDecimal totalSpent = orderService.getTotalSpentByClient(clientId);
            Map<String, Object> result = new HashMap<>();
            result.put("clientId", clientId);
            result.put("totalSpent", totalSpent);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/client/{clientId}/count-confirmed")
    public ResponseEntity<?> countConfirmedOrdersByClient(@PathVariable Long clientId) {
        try {
            log.info("GET /api/orders/client/{}/count-confirmed - Comptage commandes confirmées", clientId);
            long count = orderService.countConfirmedOrdersByClient(clientId);
            Map<String, Object> result = new HashMap<>();
            result.put("clientId", clientId);
            result.put("confirmedOrders", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
