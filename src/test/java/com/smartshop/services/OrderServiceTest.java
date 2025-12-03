package com.smartshop.services;

import com.smartshop.dtos.CreateOrderDTO;
import com.smartshop.dtos.OrderDTO;
import com.smartshop.entity.*;
import com.smartshop.exceptions.*;
import com.smartshop.mappers.OrderMapper;
import com.smartshop.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Client testClient;
    private Product testProduct;
    private Order testOrder;
    private OrderDTO testOrderDTO;
    private CreateOrderDTO createOrderDTO;
    private PromoCode testPromoCode;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "tauxTVA", new BigDecimal("20"));

        testClient = Client.builder()
                .id(1L)
                .nom("Dupont")
                .email("jean.dupont@email.com")
                .isActive(true)
                .totalOrders(5)
                .totalSpent(new BigDecimal("1000.00"))
                .customerTier(CustomerTier.SILVER)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .nom("Produit Test")
                .prix(new BigDecimal("100.00"))
                .stock(50)
                .deleted(false)
                .build();

        testPromoCode = PromoCode.builder()
                .id(1L)
                .code("PROMO10")
                .discountPercentage(10)
                .maxUses(100)
                .active(true)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .reference("CMD-2025-00001")
                .client(testClient)
                .orderDate(LocalDateTime.now())
                .statut(OrderStatus.PENDING)
                .sousTotal(new BigDecimal("200.00"))
                .montantHT(new BigDecimal("200.00"))
                .tauxTVA(new BigDecimal("20"))
                .montantTVA(new BigDecimal("40.00"))
                .totalTTC(new BigDecimal("240.00"))
                .montantPaye(BigDecimal.ZERO)
                .montantRestant(new BigDecimal("240.00"))
                .remiseTotale(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        testOrderDTO = OrderDTO.builder()
                .id(1L)
                .reference("CMD-2025-00001")
                .clientId(1L)
                .statut(OrderStatus.PENDING)
                .totalTTC(new BigDecimal("240.00"))
                .build();

        createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setClientId(1L);
        CreateOrderDTO.CreateOrderItemDTO itemDTO = new CreateOrderDTO.CreateOrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantite(2);
        createOrderDTO.setItems(List.of(itemDTO));
    }


    @Test
    void createOrder_Success() {
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(orderRepository.findMaxId()).willReturn(0L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        given(orderMapper.toDTO(any(Order.class))).willReturn(testOrderDTO);

        OrderDTO result = orderService.createOrder(createOrderDTO);

        assertThat(result).isNotNull();
        assertThat(result.getReference()).isEqualTo("CMD-2025-00001");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_ClientNotFound_ThrowsException() {
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("Client non trouvé avec l'ID: 1");
    }

    @Test
    void createOrder_ClientInactive_ThrowsException() {
        testClient.setIsActive(false);
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("Le client est inactif");
    }

    @Test
    void createOrder_EmptyItems_ThrowsException() {
        createOrderDTO.setItems(Collections.emptyList());
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("La commande doit contenir au moins un article");
    }

    @Test
    void createOrder_ProductNotFound_ThrowsException() {
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));
        given(productRepository.findById(1L)).willReturn(Optional.empty());
        given(orderRepository.findMaxId()).willReturn(0L);

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Produit non trouvé avec l'ID: 1");
    }

    @Test
    void createOrder_ProductDeleted_ThrowsException() {
        testProduct.setDeleted(true);
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(orderRepository.findMaxId()).willReturn(0L);

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("n'est plus disponible");
    }

    @Test
    void createOrder_WithValidPromoCode_Success() {
        createOrderDTO.setPromoCode("PROMO10");
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(promoCodeRepository.findByCode("PROMO10")).willReturn(Optional.of(testPromoCode));
        given(orderRepository.findMaxId()).willReturn(0L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderMapper.toDTO(any(Order.class))).willReturn(testOrderDTO);

        orderService.createOrder(createOrderDTO);

        verify(promoCodeRepository).save(any(PromoCode.class));
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPromoCode()).isEqualTo(testPromoCode);
    }

    @Test
    void createOrder_PromoCodeNotFound_ThrowsException() {
        createOrderDTO.setPromoCode("INVALID");
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(promoCodeRepository.findByCode("INVALID")).willReturn(Optional.empty());
        given(orderRepository.findMaxId()).willReturn(0L);

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(PromocodeNotFoundException.class)
                .hasMessageContaining("Code promo non trouvé: INVALID");
    }

    @Test
    void createOrder_PromoCodeInvalid_ThrowsException() {
        testPromoCode.setActive(false);
        createOrderDTO.setPromoCode("PROMO10");
        given(clientRepository.findById(1L)).willReturn(Optional.of(testClient));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(promoCodeRepository.findByCode("PROMO10")).willReturn(Optional.of(testPromoCode));
        given(orderRepository.findMaxId()).willReturn(0L);

        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(PromocodeInvalidException.class)
                .hasMessageContaining("n'est pas valide ou a expiré");
    }


    @Test
    void getOrderById_Success() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        OrderDTO result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Commande non trouvée avec l'ID: 99");
    }


    @Test
    void getOrderByReference_Success() {
        String reference = "CMD-2025-00001";
        given(orderRepository.findByReference(reference)).willReturn(Optional.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        OrderDTO result = orderService.getOrderByReference(reference);

        assertThat(result).isNotNull();
        assertThat(result.getReference()).isEqualTo(reference);
    }

    @Test
    void getOrderByReference_NotFound_ThrowsException() {
        given(orderRepository.findByReference("INVALID")).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByReference("INVALID"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Commande non trouvée avec la référence");
    }

    @Test
    void getAllOrders_Success() {
        given(orderRepository.findAll()).willReturn(List.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllOrders_EmptyList() {
        given(orderRepository.findAll()).willReturn(Collections.emptyList());

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllOrdersPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
        given(orderRepository.findAll(pageable)).willReturn(orderPage);
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        Page<OrderDTO> result = orderService.getAllOrdersPaginated(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }


    @Test
    void getOrdersByClient_Success() {
        given(clientRepository.existsById(1L)).willReturn(true);
        given(orderRepository.findByClientId(1L)).willReturn(List.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        List<OrderDTO> result = orderService.getOrdersByClient(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByClient_ClientNotFound_ThrowsException() {
        given(clientRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> orderService.getOrdersByClient(99L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("Client non trouvé avec l'ID: 99");
    }

    @Test
    void getOrdersByClientPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
        given(clientRepository.existsById(1L)).willReturn(true);
        given(orderRepository.findByClientId(1L, pageable)).willReturn(orderPage);
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        Page<OrderDTO> result = orderService.getOrdersByClientPaginated(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getOrdersByClientPaginated_ClientNotFound_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        given(clientRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> orderService.getOrdersByClientPaginated(99L, pageable))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void getOrdersByStatus_Success() {
        given(orderRepository.findByStatut(OrderStatus.PENDING)).willReturn(List.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        List<OrderDTO> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPendingOrders_Success() {
        given(orderRepository.findPendingOrders()).willReturn(List.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        List<OrderDTO> result = orderService.getPendingOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    void confirmOrder_Success() {
        testOrder.setMontantPaye(new BigDecimal("240.00"));
        testOrder.setMontantRestant(BigDecimal.ZERO);
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(testProduct)
                .quantite(2)
                .prixUnitaire(new BigDecimal("100.00"))
                .build();
        testOrder.getOrderItems().add(orderItem);

        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderMapper.toDTO(any(Order.class))).willReturn(testOrderDTO);

        OrderDTO result = orderService.confirmOrder(1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatut()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(orderCaptor.getValue().getConfirmedAt()).isNotNull();
    }

    @Test
    void confirmOrder_NotPending_ThrowsException() {
        testOrder.setStatut(OrderStatus.CONFIRMED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("Impossible de confirmer une commande qui n'est pas en attente");
    }

    @Test
    void confirmOrder_NotFullyPaid_ThrowsException() {
        testOrder.setMontantPaye(new BigDecimal("100.00"));
        testOrder.setMontantRestant(new BigDecimal("140.00"));
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("La commande ne peut pas être confirmée");
    }

    @Test
    void confirmOrder_NotFound_ThrowsException() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.confirmOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void cancelOrder_Success() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderMapper.toDTO(any(Order.class))).willReturn(testOrderDTO);

        OrderDTO result = orderService.cancelOrder(1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatut()).isEqualTo(OrderStatus.CANCELED);
        assertThat(orderCaptor.getValue().getCanceledAt()).isNotNull();
    }

    @Test
    void cancelOrder_CannotBeCanceled_ThrowsException() {
        testOrder.setStatut(OrderStatus.CONFIRMED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("Impossible d'annuler cette commande");
    }

    @Test
    void cancelOrder_NotFound_ThrowsException() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void rejectOrder_WithReason_Success() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderMapper.toDTO(any(Order.class))).willReturn(testOrderDTO);

        OrderDTO result = orderService.rejectOrder(1L, "Stock insuffisant");

        assertThat(result).isNotNull();
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatut()).isEqualTo(OrderStatus.REJECTED);
        assertThat(orderCaptor.getValue().getNotes()).contains("Rejet: Stock insuffisant");
    }

    @Test
    void rejectOrder_WithoutReason_Success() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderMapper.toDTO(any(Order.class))).willReturn(testOrderDTO);

        OrderDTO result = orderService.rejectOrder(1L, null);

        assertThat(result).isNotNull();
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatut()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void rejectOrder_NotPending_ThrowsException() {
        testOrder.setStatut(OrderStatus.CONFIRMED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.rejectOrder(1L, "Raison"))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("Impossible de rejeter une commande qui n'est pas en attente");
    }

    @Test
    void rejectOrder_NotFound_ThrowsException() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.rejectOrder(99L, "Raison"))
                .isInstanceOf(OrderNotFoundException.class);
    }


    @Test
    void countOrdersByStatus_Success() {
        given(orderRepository.countByStatut(OrderStatus.PENDING)).willReturn(5L);

        long count = orderService.countOrdersByStatus(OrderStatus.PENDING);

        assertThat(count).isEqualTo(5L);
    }


    @Test
    void getTotalSpentByClient_Success() {
        BigDecimal expectedTotal = new BigDecimal("1500.00");
        given(orderRepository.getTotalSpentByClient(1L)).willReturn(expectedTotal);

        BigDecimal result = orderService.getTotalSpentByClient(1L);

        assertThat(result).isEqualByComparingTo(expectedTotal);
    }


    @Test
    void countConfirmedOrdersByClient_Success() {
        given(orderRepository.countConfirmedOrdersByClient(1L)).willReturn(10L);

        long count = orderService.countConfirmedOrdersByClient(1L);

        assertThat(count).isEqualTo(10L);
    }


    @Test
    void updatePaymentAmounts_Success() {
        BigDecimal totalPaid = new BigDecimal("100.00");
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        given(paymentRepository.getTotalPaidForOrder(1L)).willReturn(totalPaid);

        orderService.updatePaymentAmounts(1L);

        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getMontantPaye()).isEqualByComparingTo(totalPaid);
        assertThat(orderCaptor.getValue().getMontantRestant()).isEqualByComparingTo(new BigDecimal("140.00"));
    }

    @Test
    void updatePaymentAmounts_NotFound_ThrowsException() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updatePaymentAmounts(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }


    @Test
    void existsById_ReturnsTrue() {
        given(orderRepository.existsById(1L)).willReturn(true);

        boolean exists = orderService.existsById(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ReturnsFalse() {
        given(orderRepository.existsById(99L)).willReturn(false);

        boolean exists = orderService.existsById(99L);

        assertThat(exists).isFalse();
    }


    @Test
    void getClientOrderHistory_Success() {
        given(clientRepository.existsById(1L)).willReturn(true);
        given(orderRepository.findByClientId(1L)).willReturn(List.of(testOrder));
        given(orderMapper.toDTO(testOrder)).willReturn(testOrderDTO);

        List<OrderDTO> result = orderService.getClientOrderHistory(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getClientOrderHistory_ClientNotFound_ThrowsException() {
        given(clientRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> orderService.getClientOrderHistory(99L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("Client non trouvé avec l'ID: 99");
    }
}