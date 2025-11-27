package com.smartshop.mappers;

import com.smartshop.dtos.OrderDTO;
import com.smartshop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, PaymentMapper.class})
public interface OrderMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.nom", target = "clientNom")
    @Mapping(source = "client.email", target = "clientEmail")
    @Mapping(source = "client.customerTier", target = "clientTier")
    @Mapping(source = "promoCode.id", target = "promoCodeId")
    @Mapping(source = "promoCode.code", target = "promoCodeValue")
    @Mapping(source = "remisePromoPourcentage_montant", target = "remisePromoMontant")
    @Mapping(target = "nombrePaiements", expression = "java(order.getPayments() != null ? order.getPayments().size() : 0)")
    @Mapping(target = "isFullyPaid", expression = "java(order.isFullyPaid())")
    @Mapping(target = "canBeConfirmed", expression = "java(order.canBeConfirmed())")
    OrderDTO toDTO(Order order);

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "remisePromoMontant", target = "remisePromoPourcentage_montant")
    Order toEntity(OrderDTO orderDTO);

    List<OrderDTO> toDTOList(List<Order> orders);

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(OrderDTO dto, @MappingTarget Order entity);
}