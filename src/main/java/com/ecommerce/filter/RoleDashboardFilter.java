package com.ecommerce.filter;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Enforces authentication and role checks for workspace URLs. */
@WebFilter(urlPatterns = {"/buyer/*", "/seller/*", "/admin/*"})
public final class RoleDashboardFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (!(value instanceof UserAccount account)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        Role requiredRole = path.startsWith("/buyer/") ? Role.BUYER
                : path.startsWith("/seller/") ? Role.SELLER : Role.ADMIN;
        if (account.role() != requiredRole) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Your account does not have permission to open this workspace.");
            return;
        }
        chain.doFilter(request, response);
    }
}
