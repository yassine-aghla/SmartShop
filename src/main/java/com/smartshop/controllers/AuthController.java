package com.smartshop.controllers;


import com.smartshop.dtos.auth.LoginRequestDTO;
import com.smartshop.dtos.auth.LoginResponseDTO;
import com.smartshop.services.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpSession session) {

        LoginResponseDTO response = authService.login(loginRequest, session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponseDTO> getCurrentUser(HttpSession session) {
        var user = authService.getCurrentUser(session);
        return ResponseEntity.ok(LoginResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .message("Utilisateur connecté")
                .build());
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAuth(HttpSession session) {
        boolean isAuthenticated = authService.isAuthenticated(session);
        return ResponseEntity.ok(Map.of("authenticated", isAuthenticated));
    }
}