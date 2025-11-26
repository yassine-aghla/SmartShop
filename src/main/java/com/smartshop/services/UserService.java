package com.smartshop.services;

import com.smartshop.dtos.UserDTO;
import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.exceptions.UserNotFoundException;
import com.smartshop.exceptions.UserAlreadyExistsException;
import com.smartshop.exceptions.UserBusinessException;
import com.smartshop.mappers.UserMapper;
import com.smartshop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService  {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Création d'un nouvel utilisateur avec le username: {}", userDTO.getUsername());


        validateUserData(userDTO);

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            log.warn("Tentative de création avec un username qui existe déjà: {}", userDTO.getUsername());
            throw new UserAlreadyExistsException(
                    "Un utilisateur avec le username '" + userDTO.getUsername() + "' existe déjà"
            );
        }


        User user = userMapper.toEntity(userDTO);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: ID={}, username={}", savedUser.getId(), savedUser.getUsername());

        return userMapper.toDTO(savedUser);
    }


    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Récupération de l'utilisateur avec l'ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé avec l'ID: {}", id);
                    return new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
                });

        return userMapper.toDTO(user);
    }


    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.info("Récupération de l'utilisateur avec le username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé avec le username: {}", username);
                    return new UserNotFoundException("Utilisateur non trouvé avec le username: " + username);
                });

        return userMapper.toDTO(user);
    }


    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole role) {
        log.info("Récupération de tous les utilisateurs avec le rôle: {}", role);

        return userRepository.findByRoleOrderByCreatedAtDesc(role)
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Mise à jour de l'utilisateur avec l'ID: {}", id);


        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé pour la mise à jour: ID={}", id);
                    return new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
                });

        if (!user.getUsername().equals(userDTO.getUsername()) &&
                userRepository.existsByUsername(userDTO.getUsername())) {
            log.warn("Tentative de mise à jour avec un username qui existe déjà: {}", userDTO.getUsername());
            throw new UserAlreadyExistsException(
                    "Un utilisateur avec le username '" + userDTO.getUsername() + "' existe déjà"
            );
        }

        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès: ID={}", updatedUser.getId());

        return userMapper.toDTO(updatedUser);
    }


    @Transactional
    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé pour la suppression: ID={}", id);
                    return new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
                });

        userRepository.delete(user);
        log.info("Utilisateur supprimé avec succès: ID={}", id);
    }


    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }


    public void validateUserData(UserDTO userDTO) {
        log.debug("Validation des données utilisateur: {}", userDTO.getUsername());

        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new UserBusinessException("Le username ne peut pas être vide");
        }

        if (userDTO.getUsername().length() < 3 || userDTO.getUsername().length() > 100) {
            throw new UserBusinessException("Le username doit contenir entre 3 et 100 caractères");
        }

        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new UserBusinessException("Le password ne peut pas être vide");
        }

        if (userDTO.getPassword().length() < 6) {
            throw new UserBusinessException("Le password doit contenir au minimum 6 caractères");
        }

        if (userDTO.getRole() == null) {
            throw new UserBusinessException("Le rôle ne peut pas être null");
        }

        log.debug("Validation des données utilisateur réussie");
    }
}