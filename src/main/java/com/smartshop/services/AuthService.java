package com.smartshop.services;


import com.smartshop.dtos.auth.LoginRequestDTO;
import com.smartshop.dtos.auth.LoginResponseDTO;
import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.exceptions.UnauthorizedException;
import com.smartshop.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public static final String SESSION_USER_KEY = "LOGGED_USER";
    public static final String SESSION_USER_ID_KEY = "LOGGED_USER_ID";
    public static final String SESSION_USER_ROLE_KEY = "LOGGED_USER_ROLE";

    public LoginResponseDTO login(LoginRequestDTO loginRequest, HttpSession session) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Nom d'utilisateur ou mot de passe incorrect"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new UnauthorizedException("Nom d'utilisateur ou mot de passe incorrect");
        }

        session.setAttribute(SESSION_USER_KEY, user);
        session.setAttribute(SESSION_USER_ID_KEY, user.getId());
        session.setAttribute(SESSION_USER_ROLE_KEY, user.getRole());

        session.setMaxInactiveInterval(30 * 60);

        return LoginResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .message("Connexion réussie")
                .build();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            throw new UnauthorizedException("Utilisateur non connecté");
        }
        return user;
    }


    public boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(SESSION_USER_KEY) != null;
    }


    public boolean isAdmin(HttpSession session) {
        UserRole role = (UserRole) session.getAttribute(SESSION_USER_ROLE_KEY);
        return role == UserRole.ADMIN;
    }


    public boolean isClient(HttpSession session) {
        UserRole role = (UserRole) session.getAttribute(SESSION_USER_ROLE_KEY);
        return role == UserRole.CLIENT;
    }
}