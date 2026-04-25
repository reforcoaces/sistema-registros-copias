package br.com.sistemacopias.config;

import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith("/login") || path.startsWith("/css/") || path.startsWith("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        AppUser user = session == null ? null : (AppUser) session.getAttribute("loggedUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        if ((path.startsWith("/usuarios")
                || path.startsWith("/pedidos/editar")
                || path.startsWith("/importacao")) && user.getRole() != UserRole.ADMIN) {
            response.sendRedirect("/pedidos?acessoNegado");
            return false;
        }
        return true;
    }
}
