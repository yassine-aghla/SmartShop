package com.smartshop.interceptor;
import com.smartshop.entity.UserRole;
import com.smartshop.exceptions.ForbiddenException;
import com.smartshop.exceptions.UnauthorizedException;
import com.smartshop.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (isPublicRoute(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(AuthService.SESSION_USER_KEY) == null) {
            throw new UnauthorizedException("Vous devez être connecté pour accéder à cette ressource");
        }

        UserRole role = (UserRole) session.getAttribute(AuthService.SESSION_USER_ROLE_KEY);
        if (!hasPermission(path, method, role)) {
            throw new ForbiddenException("Vous n'avez pas les permissions nécessaires pour cette action");
        }

        return true;
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/check") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources");
    }

    private boolean hasPermission(String path, String method, UserRole role) {
        if (role == UserRole.ADMIN) {
            return true;
        }
        if (role == UserRole.CLIENT) {
            if (path.startsWith("/api/products") && method.equals("GET")) {
                return true;
            }
            if (path.startsWith("/api/clients/me") || path.startsWith("/api/clients/profile")) {
                return true;
            }
            if (path.startsWith("/api/orders/my-orders") && method.equals("GET")) {
                return true;
            }
            if (path.startsWith("/api/auth/")) {
                return true;
            }
            return false;
        }

        return false;
    }
}
