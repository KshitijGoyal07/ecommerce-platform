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

@WebServlet("/register")
public final class RegisterServlet extends HttpServlet {
    private final AuthenticationService authenticationService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (currentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.setAttribute("pageTitle", "Create account | MarketSquare");
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");

        if (password == null || !password.equals(confirmPassword)) {
            renderError(request, response, "Passwords do not match.", fullName, email, role);
            return;
        }
        try {
            UserAccount account = authenticationService.register(fullName, email, password, role);
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute("authenticatedUser", account);
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (IllegalArgumentException exception) {
            renderError(request, response, exception.getMessage(), fullName, email, role);
        } catch (SQLException exception) {
            getServletContext().log("Registration failed because the account store is unavailable or the email is already used.", exception);
            String message = exception.getSQLState() != null && exception.getSQLState().startsWith("23")
                    ? "An account with that email already exists. Sign in or use another email."
                    : "Registration is temporarily unavailable. Check that MySQL is running and try again.";
            renderError(request, response, message, fullName, email, role);
        }
    }

    private void renderError(HttpServletRequest request, HttpServletResponse response, String message,
                             String fullName, String email, String role) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("fullName", fullName == null ? "" : fullName.trim());
        request.setAttribute("email", email == null ? "" : email.trim());
        request.setAttribute("role", role == null ? "BUYER" : role);
        request.setAttribute("pageTitle", "Create account | MarketSquare");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private static UserAccount currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
