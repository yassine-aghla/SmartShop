package com.smartshop.mappers;

import com.smartshop.dtos.PaymentDTO;
import com.smartshop.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.reference", target = "orderReference")
    @Mapping(target = "montantRestantApres", ignore = true)
    @Mapping(target = "isEncaisse", expression = "java(payment.isEncaisse())")
    PaymentDTO toDTO(Payment payment);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(PaymentDTO paymentDTO);

    List<PaymentDTO> toDTOList(List<Payment> payments);

    List<Payment> toEntityList(List<PaymentDTO> paymentDTOs);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(PaymentDTO dto, @MappingTarget Payment entity);
}