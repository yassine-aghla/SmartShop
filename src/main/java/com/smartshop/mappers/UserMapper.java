package com.smartshop.mappers;



import com.smartshop.dtos.UserDTO;
import com.smartshop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper pour convertir entre User entity et UserDTO
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);

}
