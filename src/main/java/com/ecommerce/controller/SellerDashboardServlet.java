package com.ecommerce.controller;

import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/seller/dashboard")
public final class SellerDashboardServlet extends HttpServlet {
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
            var products = productDao.findBySellerId(account.id());
            request.setAttribute("products", products);
            request.setAttribute("productCount", products.size());
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("pageTitle", "Seller workspace | MarketSquare");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/seller.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load seller inventory.", exception);
            request.setAttribute("errorMessage", "Your inventory is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    private static UserAccount authenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
