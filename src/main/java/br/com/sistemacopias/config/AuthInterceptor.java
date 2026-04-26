package br.com.sistemacopias.config;

import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.model.UserRole;
import br.com.sistemacopias.support.ReforcoAccess;
import br.com.sistemacopias.support.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static boolean isPublic(String path) {
        return path.startsWith("/login")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/error");
    }

    private static boolean isCopiasAppPath(String path) {
        if (path.equals("/")) {
            return true;
        }
        return path.startsWith("/pedidos")
                || path.startsWith("/pedido")
                || path.startsWith("/dashboard")
                || path.startsWith("/relatorios")
                || path.startsWith("/importacao")
                || path.startsWith("/usuarios");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (isPublic(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        AppUser user = session == null ? null : (AppUser) session.getAttribute("loggedUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        if (path.startsWith("/logout")) {
            return true;
        }

        if (path.startsWith("/escolher-sistema")) {
            return true;
        }

        if (path.startsWith("/reforco")) {
            if (!ReforcoAccess.podeAcessarReforco(user)) {
                response.sendRedirect("/escolher-sistema?reforcoNegado=1");
                return false;
            }
            String sis = (String) session.getAttribute(SessionKeys.SISTEMA_ATIVO);
            if (!SessionKeys.REFORCO.equals(sis)) {
                if (SessionKeys.CONTROLE_FLUXO.equals(sis)) {
                    response.sendRedirect("/controle-fluxo/dashboard");
                } else {
                    response.sendRedirect("/");
                }
                return false;
            }
            return true;
        }

        if (path.startsWith("/controle-fluxo")) {
            if (!ReforcoAccess.podeAcessarControleEntradasSaidas(user)) {
                response.sendRedirect("/escolher-sistema?fluxoNegado=1");
                return false;
            }
            String sis = (String) session.getAttribute(SessionKeys.SISTEMA_ATIVO);
            if (!SessionKeys.CONTROLE_FLUXO.equals(sis)) {
                if (SessionKeys.REFORCO.equals(sis)) {
                    response.sendRedirect("/reforco/dashboard");
                } else {
                    response.sendRedirect("/");
                }
                return false;
            }
            return true;
        }

        String sistema = (String) session.getAttribute(SessionKeys.SISTEMA_ATIVO);
        if (sistema == null || sistema.isBlank()) {
            response.sendRedirect("/escolher-sistema");
            return false;
        }

        if (SessionKeys.REFORCO.equals(sistema) && isCopiasAppPath(path)) {
            response.sendRedirect("/reforco/dashboard");
            return false;
        }

        if (SessionKeys.CONTROLE_FLUXO.equals(sistema) && isCopiasAppPath(path)) {
            response.sendRedirect("/controle-fluxo/dashboard");
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
