package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.repository.jdbc.ProductViewDao;
import com.ecommerce.repository.jdbc.WishlistDao;
import com.ecommerce.security.CsrfTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Loads the buyer catalogue, wishlist state, and recently viewed products. */
@WebServlet("/buyer/dashboard")
public final class BuyerDashboardServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final WishlistDao wishlistDao = new WishlistDao();
    private final ProductViewDao productViewDao = new ProductViewDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UserAccount account = authenticatedBuyer(session);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            var products = productDao.findActiveProducts(50, 0);
            request.setAttribute("products", products);
            request.setAttribute("productCount", products.size());
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
            request.setAttribute("cartQuantity", CartSupport.getCart(session).getTotalQuantity());
            request.setAttribute("wishlistProductIds", wishlistDao.findProductIdsByBuyerId(account.id()));
            request.setAttribute("recentlyViewedProducts", productViewDao.findRecentlyViewedByBuyerId(account.id(), 6));
            request.setAttribute("pageTitle", "Buyer workspace | MarketSquare");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/buyer.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load buyer catalogue, wishlist state, or recent views.", exception);
            request.setAttribute("errorMessage", "The buyer workspace is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    private static UserAccount authenticatedBuyer(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (!(value instanceof UserAccount account) || account.role() != Role.BUYER) return null;
        return account;
    }
}
