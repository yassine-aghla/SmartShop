package com.smartshop.mappers;

import com.smartshop.dtos.PromoCodeDto;
import com.smartshop.entity.PromoCode;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromoCodeMapper {
    PromoCodeDto toDTO(PromoCode promoCode);
    PromoCode toEntity(PromoCodeDto promoCodeDTO);
    void updateEntityFromDTO(PromoCodeDto promoCodeDTO, @MappingTarget PromoCode promoCode);
}