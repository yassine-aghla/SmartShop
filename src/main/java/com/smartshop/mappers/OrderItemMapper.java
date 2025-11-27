package com.smartshop.mappers;

import com.smartshop.dtos.OrderItemDTO;
import com.smartshop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "product.id", target = "productId")
    OrderItemDTO toDTO(OrderItem orderItem);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(OrderItemDTO orderItemDTO);

    List<OrderItemDTO> toDTOList(List<OrderItem> orderItems);

    List<OrderItem> toEntityList(List<OrderItemDTO> orderItemDTOs);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(OrderItemDTO dto, @MappingTarget OrderItem entity);
}