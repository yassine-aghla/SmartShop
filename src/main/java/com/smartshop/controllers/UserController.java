package com.smartshop.controllers;

import com.smartshop.dtos.UserDTO;
import com.smartshop.entity.UserRole;
import com.smartshop.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("GET /api/users - Récupération de tous les utilisateurs");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Récupération de l'utilisateur", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.info("GET /api/users/username/{} - Récupération de l'utilisateur", username);
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable UserRole role) {
        log.info("GET /api/users/role/{} - Récupération des utilisateurs par rôle", role);
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("POST /api/users - Création d'un nouvel utilisateur: {}", userDTO.getUsername());
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("PUT /api/users/{} - Mise à jour de l'utilisateur", id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Suppression de l'utilisateur", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        log.info("GET /api/users/exists/{} - Vérification de l'existence du username", username);
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }
}
