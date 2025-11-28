package com.smartshop.repositories;


import com.smartshop.entity.Payment;
import com.smartshop.entity.PaymentStatus;
import com.smartshop.entity.PaymentType;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdOrderByNumeroPaiementAsc(Long orderId);

    List<Payment> findByStatut(PaymentStatus statut);
    List<Payment> findByTypePaiement(PaymentType typePaiement);
    Page<Payment> findByStatut(PaymentStatus statut, Pageable pageable);
    @Query("SELECT p FROM Payment p WHERE p.statut = 'EN_ATTENTE' ORDER BY p.datePaiement")
    List<Payment> findPendingPayments();
    List<Payment> findByOrderIdAndStatut(Long orderId, PaymentStatus statut);
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Payment p WHERE p.order.id = :orderId AND p.statut = 'ENCAISSÉ'")
    BigDecimal getTotalPaidForOrder(@Param("orderId") Long orderId);
    @Query("SELECT COALESCE(MAX(p.numeroPaiement), 0) + 1 FROM Payment p WHERE p.order.id = :orderId")
    Integer getNextPaymentNumber(@Param("orderId") Long orderId);
    long countByOrderId(Long orderId);
    @Query("SELECT p FROM Payment p WHERE p.datePaiement BETWEEN :start AND :end ORDER BY p.datePaiement DESC")
    List<Payment> findByDatePaiementBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT p FROM Payment p WHERE p.typePaiement = 'CHEQUE' AND p.statut = 'EN_ATTENTE' " +
            "AND p.dateEcheance IS NOT NULL AND p.dateEcheance <= :date")
    List<Payment> findOverdueCheques(@Param("date") LocalDateTime date);

    @Query("SELECT p.typePaiement, COALESCE(SUM(p.montant), 0) FROM Payment p " +
            "WHERE p.statut = 'ENCAISSÉ' AND p.dateEncaissement BETWEEN :start AND :end " +
            "GROUP BY p.typePaiement")
    List<Object[]> getTotalByTypeAndPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Optional<Payment> findByReference(String reference);
    boolean existsByReference(String reference);
}