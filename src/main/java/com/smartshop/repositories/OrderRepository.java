package com.smartshop.repositories;

import com.smartshop.entity.Order;
import com.smartshop.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Recherche par référence unique
     */
    Optional<Order> findByReference(String reference);

    /**
     * Vérifie si une référence existe
     */
    boolean existsByReference(String reference);

    /**
     * Toutes les commandes d'un client
     */
    List<Order> findByClientId(Long clientId);

    /**
     * Commandes d'un client avec pagination
     */
    Page<Order> findByClientId(Long clientId, Pageable pageable);

    /**
     * Commandes par statut
     */
    List<Order> findByStatut(OrderStatus statut);

    /**
     * Commandes par statut avec pagination
     */
    Page<Order> findByStatut(OrderStatus statut, Pageable pageable);

    /**
     * Commandes d'un client par statut
     */
    List<Order> findByClientIdAndStatut(Long clientId, OrderStatus statut);

    /**
     * Compter les commandes d'un client par statut
     */
    long countByClientIdAndStatut(Long clientId, OrderStatus statut);

    /**
     * Compter les commandes par statut
     */
    long countByStatut(OrderStatus statut);

    /**
     * Commandes en attente (PENDING)
     */
    @Query("SELECT o FROM Order o WHERE o.statut = 'PENDING' ORDER BY o.orderDate DESC")
    List<Order> findPendingOrders();

    /**
     * Commandes confirmées d'un client
     */
    @Query("SELECT o FROM Order o WHERE o.client.id = :clientId AND o.statut = 'CONFIRMED' ORDER BY o.orderDate DESC")
    List<Order> findConfirmedOrdersByClient(@Param("clientId") Long clientId);

    /**
     * Montant total dépensé par un client (commandes confirmées)
     */
    @Query("SELECT COALESCE(SUM(o.totalTTC), 0) FROM Order o WHERE o.client.id = :clientId AND o.statut = 'CONFIRMED'")
    BigDecimal getTotalSpentByClient(@Param("clientId") Long clientId);

    /**
     * Nombre de commandes confirmées d'un client
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId AND o.statut = 'CONFIRMED'")
    long countConfirmedOrdersByClient(@Param("clientId") Long clientId);

    /**
     * Commandes dans une période
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :start AND :end ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Commandes utilisant un code promo
     */
    List<Order> findByPromoCodeId(Long promoCodeId);

    /**
     * Compter les utilisations d'un code promo
     */
    long countByPromoCodeIdAndStatutNot(Long promoCodeId, OrderStatus statut);

    /**
     * Dernière commande d'un client
     */
    @Query("SELECT o FROM Order o WHERE o.client.id = :clientId ORDER BY o.orderDate DESC LIMIT 1")
    Optional<Order> findLastOrderByClient(@Param("clientId") Long clientId);

    /**
     * Commandes non entièrement payées
     */
    @Query("SELECT o FROM Order o WHERE o.montantRestant > 0 AND o.statut = 'PENDING' ORDER BY o.orderDate")
    List<Order> findUnpaidOrders();

    /**
     * Générer le prochain numéro de référence
     */
    @Query("SELECT MAX(o.id) FROM Order o")
    Long findMaxId();

    /**
     * Recherche avec filtres multiples
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(:clientId IS NULL OR o.client.id = :clientId) AND " +
            "(:statut IS NULL OR o.statut = :statut) AND " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findWithFilters(
            @Param("clientId") Long clientId,
            @Param("statut") OrderStatus statut,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}