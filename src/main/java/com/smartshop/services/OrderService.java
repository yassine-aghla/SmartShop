package com.smartshop.services;

import com.smartshop.dtos.CreateOrderDTO;
import com.smartshop.dtos.OrderDTO;
import com.smartshop.entity.*;
import com.smartshop.exceptions.*;
import com.smartshop.mappers.OrderMapper;
import com.smartshop.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;

    @Value("${smartshop.tva.rate:20}")
    private BigDecimal tauxTVA = new BigDecimal("20");

    private static final BigDecimal PROMO_CODE_DISCOUNT = new BigDecimal("5");


    @Transactional
    public OrderDTO createOrder(CreateOrderDTO createOrderDTO) {
        log.info("Création d'une nouvelle commande pour le client ID: {}", createOrderDTO.getClientId());

        Client client = clientRepository.findById(createOrderDTO.getClientId())
                .orElseThrow(() -> new ClientNotFoundException(
                        "Client non trouvé avec l'ID: " + createOrderDTO.getClientId()));

        if (!client.getIsActive()) {
            throw new OrderBusinessException("Le client est inactif et ne peut pas passer de commande");
        }


        if (createOrderDTO.getItems() == null || createOrderDTO.getItems().isEmpty()) {
            throw new OrderBusinessException("La commande doit contenir au moins un article");
        }

        Order order = Order.builder()
                .client(client)
                .reference(generateOrderReference())
                .orderDate(LocalDateTime.now())
                .statut(OrderStatus.PENDING)
                .tauxTVA(tauxTVA)
                .notes(createOrderDTO.getNotes())
                .orderItems(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        BigDecimal sousTotal = BigDecimal.ZERO;
        List<Product> productsToUpdate = new ArrayList<>();

        for (CreateOrderDTO.CreateOrderItemDTO itemDTO : createOrderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Produit non trouvé avec l'ID: " + itemDTO.getProductId()));

            if (product.getDeleted()) {
                throw new OrderBusinessException(
                        "Le produit '" + product.getNom() + "' n'est plus disponible");
            }

            if (!product.hasEnoughStock(itemDTO.getQuantite())) {
                log.warn("Stock insuffisant pour le produit {}: demandé={}, disponible={}",
                        product.getNom(), itemDTO.getQuantite(), product.getStock());

                order.setStatut(OrderStatus.REJECTED);
                order = orderRepository.save(order);

            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantite(itemDTO.getQuantite())
                    .prixUnitaire(product.getPrix())
                    .productNom(product.getNom())
                    .build();
            orderItem.calculateTotalLigne();

            order.addOrderItem(orderItem);
            sousTotal = sousTotal.add(orderItem.getTotalLigne());
            productsToUpdate.add(product);
        }

        order.setSousTotal(sousTotal.setScale(2, RoundingMode.HALF_UP));

        BigDecimal remiseFidelitePourcentage = client.getDiscountPercentage(sousTotal);
        BigDecimal remiseFideliteMontant = BigDecimal.ZERO;

        if (remiseFidelitePourcentage.compareTo(BigDecimal.ZERO) > 0) {
            remiseFideliteMontant = sousTotal
                    .multiply(remiseFidelitePourcentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            log.info("Remise fidélité appliquée: {}% = {} DH", remiseFidelitePourcentage, remiseFideliteMontant);
        }

        order.setRemiseFidelitePourcentage(remiseFidelitePourcentage);
        order.setRemiseFideliteMontant(remiseFideliteMontant);

        BigDecimal remisePromoPourcentage = BigDecimal.ZERO;
        BigDecimal remisePromoMontant = BigDecimal.ZERO;

        if (createOrderDTO.getPromoCode() != null && !createOrderDTO.getPromoCode().isEmpty()) {
            PromoCode promoCode = promoCodeRepository.findByCode(createOrderDTO.getPromoCode())
                    .orElseThrow(() -> new PromocodeNotFoundException(
                            "Code promo non trouvé: " + createOrderDTO.getPromoCode()));

            if (!promoCode.isValid()) {
                throw new PromocodeInvalidException(
                        "Le code promo '" + promoCode.getCode() + "' n'est pas valide ou a expiré");
            }

            order.setPromoCode(promoCode);
            remisePromoPourcentage = BigDecimal.valueOf(promoCode.getDiscountPercentage());

            remisePromoMontant = sousTotal
                    .multiply(remisePromoPourcentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            log.info("Code promo appliqué: {} - {}% = {} DH",
                    promoCode.getCode(), remisePromoPourcentage, remisePromoMontant);

            promoCode.incrementUseCount();
            promoCodeRepository.save(promoCode);
        }

        order.setRemisePromoPourcentage(remisePromoPourcentage);
        order.setRemisePromoPourcentage_montant(remisePromoMontant);

        BigDecimal remiseTotale = remiseFideliteMontant.add(remisePromoMontant)
                .setScale(2, RoundingMode.HALF_UP);
        order.setRemiseTotale(remiseTotale);

        BigDecimal montantHT = sousTotal.subtract(remiseTotale).setScale(2, RoundingMode.HALF_UP);
        if (montantHT.compareTo(BigDecimal.ZERO) < 0) {
            montantHT = BigDecimal.ZERO;
        }
        order.setMontantHT(montantHT);

        BigDecimal montantTVA = montantHT
                .multiply(tauxTVA)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        order.setMontantTVA(montantTVA);

        BigDecimal totalTTC = montantHT.add(montantTVA).setScale(2, RoundingMode.HALF_UP);
        order.setTotalTTC(totalTTC);

        order.setMontantPaye(BigDecimal.ZERO);
        order.setMontantRestant(totalTTC);

        Order savedOrder = orderRepository.save(order);

        log.info("Commande créée avec succès: ref={}, sous-total={}, remise={}, TVA={}, total={}",
                savedOrder.getReference(), sousTotal, remiseTotale, montantTVA, totalTTC);

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.info("Récupération de la commande ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée avec l'ID: " + id));

        return orderMapper.toDTO(order);
    }


    @Transactional(readOnly = true)
    public OrderDTO getOrderByReference(String reference) {
        log.info("Récupération de la commande ref: {}", reference);

        Order order = orderRepository.findByReference(reference)
                .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée avec la référence: " + reference));

        return orderMapper.toDTO(order);
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.info("Récupération de toutes les commandes");
        return orderRepository.findAll().stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrdersPaginated(Pageable pageable) {
        log.info("Récupération des commandes avec pagination");
        return orderRepository.findAll(pageable).map(orderMapper::toDTO);
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByClient(Long clientId) {
        log.info("Récupération des commandes du client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client non trouvé avec l'ID: " + clientId);
        }

        return orderRepository.findByClientId(clientId).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByClientPaginated(Long clientId, Pageable pageable) {
        log.info("Récupération des commandes du client ID: {} avec pagination", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client non trouvé avec l'ID: " + clientId);
        }

        return orderRepository.findByClientId(clientId, pageable).map(orderMapper::toDTO);
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus statut) {
        log.info("Récupération des commandes avec statut: {}", statut);
        return orderRepository.findByStatut(statut).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getPendingOrders() {
        log.info("Récupération des commandes en attente (PENDING)");
        return orderRepository.findPendingOrders().stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public OrderDTO confirmOrder(Long orderId) {
        log.info("Confirmation de la commande ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée avec l'ID: " + orderId));


        if (order.getStatut() != OrderStatus.PENDING) {
            throw new OrderBusinessException(
                    "Impossible de confirmer une commande qui n'est pas en attente. Statut actuel: " + order.getStatut());
        }


        if (!order.isFullyPaid()) {
            throw new OrderBusinessException(
                    String.format("La commande ne peut pas être confirmée. Montant restant à payer: %.2f DH",
                            order.getMontantRestant()));
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();

            if (!product.hasEnoughStock(item.getQuantite())) {

            }

            product.decrementStock(item.getQuantite());
            productRepository.save(product);
            log.debug("Stock décrémenté pour {}: -{} (nouveau stock: {})",
                    product.getNom(), item.getQuantite(), product.getStock());
        }

        Client client = order.getClient();
        client.updateStatisticsAfterOrder(order.getTotalTTC());
        clientRepository.save(client);

        log.info("Client {} mis à jour: totalOrders={}, totalSpent={}, tier={}",
                client.getNom(), client.getTotalOrders(), client.getTotalSpent(), client.getCustomerTier());

        order.setStatut(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Commande {} confirmée avec succès", savedOrder.getReference());

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Annulation de la commande ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée avec l'ID: " + orderId));

        if (!order.canBeCanceled()) {
            throw new OrderBusinessException(
                    "Impossible d'annuler cette commande. Statut actuel: " + order.getStatut());
        }

        order.setStatut(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Commande {} annulée avec succès", savedOrder.getReference());

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional
    public OrderDTO rejectOrder(Long orderId, String reason) {
        log.info("Rejet de la commande ID: {} - Raison: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée avec l'ID: " + orderId));

        if (order.getStatut() != OrderStatus.PENDING) {
            throw new OrderBusinessException(
                    "Impossible de rejeter une commande qui n'est pas en attente. Statut actuel: " + order.getStatut());
        }

        order.setStatut(OrderStatus.REJECTED);
        if (reason != null && !reason.isEmpty()) {
            order.setNotes(order.getNotes() != null ? order.getNotes() + " | Rejet: " + reason : "Rejet: " + reason);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Commande {} rejetée avec succès", savedOrder.getReference());

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public long countOrdersByStatus(OrderStatus statut) {
        return orderRepository.countByStatut(statut);
    }


    @Transactional(readOnly = true)
    public BigDecimal getTotalSpentByClient(Long clientId) {
        return orderRepository.getTotalSpentByClient(clientId);
    }

    @Transactional(readOnly = true)
    public long countConfirmedOrdersByClient(Long clientId) {
        return orderRepository.countConfirmedOrdersByClient(clientId);
    }

    private String generateOrderReference() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        Long maxId = orderRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;
        return String.format("CMD-%s-%05d", year, nextId);
    }

    @Transactional
    public void updatePaymentAmounts(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée avec l'ID: " + orderId));

        BigDecimal totalPaid = paymentRepository.getTotalPaidForOrder(orderId);
        order.setMontantPaye(totalPaid);
        order.setMontantRestant(order.getTotalTTC().subtract(totalPaid).setScale(2, RoundingMode.HALF_UP));

        orderRepository.save(order);
        log.debug("Montants de paiement mis à jour pour la commande {}: payé={}, restant={}",
                order.getReference(), order.getMontantPaye(), order.getMontantRestant());
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getClientOrderHistory(Long clientId) {
        log.info("Récupération de l'historique des commandes du client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client non trouvé avec l'ID: " + clientId);
        }

        return orderRepository.findByClientId(clientId).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }
}