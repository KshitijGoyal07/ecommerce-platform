package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/dashboard")
public final class DashboardRedirectServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (!(value instanceof UserAccount account)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String destination = switch (account.role()) {
            case BUYER -> "/buyer/dashboard";
            case SELLER -> "/seller/dashboard";
            case ADMIN -> "/admin/dashboard";
        };
        response.sendRedirect(request.getContextPath() + destination);
    }
}
