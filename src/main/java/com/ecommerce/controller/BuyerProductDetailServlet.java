package com.ecommerce.controller;

import com.ecommerce.model.Product;
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
import java.util.Set;

/** Shows an active product detail page and records the view for the current buyer. */
@WebServlet("/buyer/product")
public final class BuyerProductDetailServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final ProductViewDao productViewDao = new ProductViewDao();
    private final WishlistDao wishlistDao = new WishlistDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UserAccount account = authenticatedBuyer(session);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long productId;
        try {
            productId = Long.parseLong(request.getParameter("id"));
            if (productId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found.");
            return;
        }
        try {
            Product product = productDao.findById(productId).filter(Product::isActive).orElse(null);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "This product is no longer available.");
                return;
            }
            productViewDao.recordView(account.id(), product.getId());
            Set<Long> wishlistIds = wishlistDao.findProductIdsByBuyerId(account.id());
            request.setAttribute("product", product);
            request.setAttribute("isWishlisted", wishlistIds.contains(product.getId()));
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("cartQuantity", CartSupport.getCart(session).getTotalQuantity());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
            request.setAttribute("pageTitle", product.getName() + " | MarketSquare");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/product-detail.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load buyer product details or record product view.", exception);
            request.setAttribute("errorMessage", "Product details are temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    private static UserAccount authenticatedBuyer(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (!(value instanceof UserAccount account) || account.role() != Role.BUYER) return null;
        return account;
    }
}
