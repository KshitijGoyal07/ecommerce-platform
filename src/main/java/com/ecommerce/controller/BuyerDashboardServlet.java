package com.ecommerce.controller;

import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.security.CsrfTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/buyer/dashboard")
public final class BuyerDashboardServlet extends HttpServlet {
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
            var products = productDao.findActiveProducts(50, 0);
            request.setAttribute("products", products);
            request.setAttribute("productCount", products.size());
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession(true)));
            request.setAttribute("cartQuantity", CartSupport.getCart(request.getSession(true)).getTotalQuantity());
            request.setAttribute("pageTitle", "Buyer workspace | MarketSquare");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/buyer.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load buyer product catalogue.", exception);
            request.setAttribute("errorMessage", "The product catalogue is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    private static UserAccount authenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
