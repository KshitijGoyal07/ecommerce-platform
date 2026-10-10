package com.ecommerce.controller;

import com.ecommerce.model.UserAccount;
import com.ecommerce.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public final class LoginServlet extends HttpServlet {
    private final AuthenticationService authenticationService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (currentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.setAttribute("pageTitle", "Sign in | MarketSquare");
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        try {
            Optional<UserAccount> authenticated = authenticationService.authenticate(email, password);
            if (authenticated.isEmpty()) {
                request.setAttribute("errorMessage", "Email or password is incorrect, or the account is inactive.");
                request.setAttribute("email", email == null ? "" : email.trim());
                request.setAttribute("pageTitle", "Sign in | MarketSquare");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute("authenticatedUser", authenticated.get());
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException exception) {
            getServletContext().log("Login failed because the account store is unavailable.", exception);
            request.setAttribute("errorMessage", "Sign-in is temporarily unavailable. Check that MySQL is running and try again.");
            request.setAttribute("pageTitle", "Sign in | MarketSquare");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private static UserAccount currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
