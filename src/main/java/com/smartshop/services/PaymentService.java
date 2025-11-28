package com.smartshop.services;

import com.smartshop.dtos.PaymentDTO;
import com.smartshop.entity.*;
import com.smartshop.exceptions.*;
import com.smartshop.mappers.PaymentMapper;
import com.smartshop.repositories.OrderRepository;
import com.smartshop.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;

    public static final BigDecimal LIMITE_ESPECES = new BigDecimal("20000.00");

    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        log.info("Création d'un paiement pour la commande ID: {}, montant: {}, type: {}",
                paymentDTO.getOrderId(), paymentDTO.getMontant(), paymentDTO.getTypePaiement());

        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Commande non trouvée avec l'ID: " + paymentDTO.getOrderId()));
        if (order.getStatut() != OrderStatus.PENDING) {
            throw new PaymentBusinessException(
                    "Impossible d'ajouter un paiement à une commande qui n'est pas en attente. Statut: " + order.getStatut());
        }

        if (paymentDTO.getMontant() == null || paymentDTO.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentBusinessException("Le montant du paiement doit être supérieur à 0");
        }


        if (paymentDTO.getMontant().compareTo(order.getMontantRestant()) > 0) {
            throw new PaymentBusinessException(
                    String.format("Le montant du paiement (%.2f DH) dépasse le montant restant à payer (%.2f DH)",
                            paymentDTO.getMontant(), order.getMontantRestant()));
        }

        validatePaymentByType(paymentDTO);

        Payment payment = Payment.builder()
                .order(order)
                .numeroPaiement(paymentRepository.getNextPaymentNumber(order.getId()))
                .montant(paymentDTO.getMontant().setScale(2, RoundingMode.HALF_UP))
                .typePaiement(paymentDTO.getTypePaiement())
                .datePaiement(paymentDTO.getDatePaiement() != null ? paymentDTO.getDatePaiement() : LocalDateTime.now())
                .banque(paymentDTO.getBanque())
                .dateEcheance(paymentDTO.getDateEcheance())
                .notes(paymentDTO.getNotes())
                .build();

        if (paymentDTO.getTypePaiement() == PaymentType.ESPECES) {
            payment.setStatut(PaymentStatus.ENCAISSÉ);
            payment.setDateEncaissement(LocalDateTime.now());
        } else {
            payment.setStatut(PaymentStatus.EN_ATTENTE);
        }

        payment.generateReference(order.getId());

        if (paymentDTO.getReference() != null && !paymentDTO.getReference().isEmpty()) {
            payment.setReference(paymentDTO.getReference());
        }

        Payment savedPayment = paymentRepository.save(payment);
        order.addPayment(savedPayment);


        updateOrderPaymentAmounts(order);

        log.info("Paiement créé avec succès: ref={}, montant={}, statut={}",
                savedPayment.getReference(), savedPayment.getMontant(), savedPayment.getStatut());

        PaymentDTO result = paymentMapper.toDTO(savedPayment);
        result.setMontantRestantApres(order.getMontantRestant());

        return result;
    }

    private void validatePaymentByType(PaymentDTO paymentDTO) {
        switch (paymentDTO.getTypePaiement()) {
            case ESPECES:
                if (paymentDTO.getMontant().compareTo(LIMITE_ESPECES) > 0) {
                    throw new PaymentBusinessException(
                            String.format("Le paiement en espèces ne peut pas dépasser %.2f DH (Art. 193 CGI)",
                                    LIMITE_ESPECES));
                }
                break;

            case CHEQUE:
                if (paymentDTO.getBanque() == null || paymentDTO.getBanque().isEmpty()) {
                    log.warn("Paiement par chèque sans banque spécifiée");
                }
                break;

            case VIREMENT:
                if (paymentDTO.getBanque() == null || paymentDTO.getBanque().isEmpty()) {
                    log.warn("Paiement par virement sans banque spécifiée");
                }
                break;

            default:
                throw new PaymentBusinessException("Type de paiement non supporté: " + paymentDTO.getTypePaiement());
        }
    }

    private void updateOrderPaymentAmounts(Order order) {
        BigDecimal totalPaid = paymentRepository.getTotalPaidForOrder(order.getId());
        order.setMontantPaye(totalPaid);
        order.setMontantRestant(order.getTotalTTC().subtract(totalPaid).setScale(2, RoundingMode.HALF_UP));

        if (order.getMontantRestant().compareTo(BigDecimal.ZERO) < 0) {
            order.setMontantRestant(BigDecimal.ZERO);
        }

        orderRepository.save(order);
        log.debug("Montants commande mis à jour: payé={}, restant={}", order.getMontantPaye(), order.getMontantRestant());
    }


    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        log.info("Récupération du paiement ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Paiement non trouvé avec l'ID: " + id));

        return paymentMapper.toDTO(payment);
    }


    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByOrder(Long orderId) {
        log.info("Récupération des paiements de la commande ID: {}", orderId);

        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("Commande non trouvée avec l'ID: " + orderId);
        }

        return paymentRepository.findByOrderIdOrderByNumeroPaiementAsc(orderId).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus statut) {
        log.info("Récupération des paiements avec statut: {}", statut);
        return paymentRepository.findByStatut(statut).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByStatusPaginated(PaymentStatus statut, Pageable pageable) {
        log.info("Récupération des paiements avec statut: {} (paginé)", statut);
        return paymentRepository.findByStatut(statut, pageable).map(paymentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPendingPayments() {
        log.info("Récupération des paiements en attente d'encaissement");
        return paymentRepository.findPendingPayments().stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public PaymentDTO encaisserPayment(Long paymentId) {
        log.info("Encaissement du paiement ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Paiement non trouvé avec l'ID: " + paymentId));

        if (payment.getStatut() != PaymentStatus.EN_ATTENTE) {
            throw new PaymentBusinessException(
                    "Impossible d'encaisser un paiement qui n'est pas en attente. Statut actuel: " + payment.getStatut());
        }

        payment.encaisser();
        Payment savedPayment = paymentRepository.save(payment);

        updateOrderPaymentAmounts(payment.getOrder());

        log.info("Paiement {} encaissé avec succès", savedPayment.getReference());
        return paymentMapper.toDTO(savedPayment);
    }

    @Transactional
    public PaymentDTO rejeterPayment(Long paymentId, String motif) {
        log.info("Rejet du paiement ID: {} - Motif: {}", paymentId, motif);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Paiement non trouvé avec l'ID: " + paymentId));

        if (payment.getStatut() != PaymentStatus.EN_ATTENTE) {
            throw new PaymentBusinessException(
                    "Impossible de rejeter un paiement qui n'est pas en attente. Statut actuel: " + payment.getStatut());
        }

        payment.rejeter();
        if (motif != null && !motif.isEmpty()) {
            payment.setNotes(payment.getNotes() != null ? payment.getNotes() + " | Rejet: " + motif : "Rejet: " + motif);
        }

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Paiement {} rejeté", savedPayment.getReference());
        return paymentMapper.toDTO(savedPayment);
    }


    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidForOrder(Long orderId) {
        return paymentRepository.getTotalPaidForOrder(orderId);
    }


    @Transactional(readOnly = true)
    public long countPaymentsByOrder(Long orderId) {
        return paymentRepository.countByOrderId(orderId);
    }


    @Transactional(readOnly = true)
    public List<PaymentDTO> getOverdueCheques() {
        log.info("Récupération des chèques en retard d'encaissement");
        return paymentRepository.findOverdueCheques(LocalDateTime.now()).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return paymentRepository.existsById(id);
    }
}
