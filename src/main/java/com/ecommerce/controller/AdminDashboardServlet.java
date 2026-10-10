package com.ecommerce.controller;

import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.repository.jdbc.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/dashboard")
public final class AdminDashboardServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserAccount account = authenticatedUser(request);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("activeUserCount", userDao.findAllActive().size());
            request.setAttribute("productCount", productDao.findAll().size());
            request.setAttribute("pageTitle", "Admin workspace | MarketSquare");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/admin.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load administrator overview.", exception);
            request.setAttribute("errorMessage", "The marketplace overview is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    private static UserAccount authenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
