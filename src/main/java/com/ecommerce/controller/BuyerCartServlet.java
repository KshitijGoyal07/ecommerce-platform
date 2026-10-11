package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.security.CsrfTokens;
import com.ecommerce.service.CartLineView;
import com.ecommerce.service.ShoppingCart;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Shows and updates a buyer's session-scoped shopping cart. */
@WebServlet("/buyer/cart")
public final class BuyerCartServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UserAccount account = authenticatedUser(session);
        if (account == null || account.role() != Role.BUYER) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            ShoppingCart cart = CartSupport.getCart(session);
            List<CartLineView> lines = CartSupport.loadLines(cart, productDao);
            request.setAttribute("cartLines", lines);
            request.setAttribute("subtotal", CartSupport.subtotal(lines));
            request.setAttribute("cartQuantity", cart.getTotalQuantity());
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
            request.setAttribute("pageTitle", "Your cart | MarketSquare");
            CartSupport.consumeFlash(session, request);
            request.getRequestDispatcher("/WEB-INF/views/dashboard/cart.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load buyer cart.", exception);
            request.setAttribute("errorMessage", "Your cart is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        UserAccount account = authenticatedUser(session);
        if (account == null || account.role() != Role.BUYER) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!CsrfTokens.isValid(session, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form token expired. Refresh and try again.");
            return;
        }
        ShoppingCart cart = CartSupport.getCart(session);
        try {
            String action = text(request.getParameter("action"), "Choose a cart action.");
            switch (action) {
                case "add" -> addItem(cart, request);
                case "update" -> updateItem(cart, request);
                case "remove" -> removeItem(cart, request);
                case "clear" -> cart.clear();
                default -> throw new IllegalArgumentException("Choose a valid cart action.");
            }
            CartSupport.flash(session, "Your cart has been updated.", null);
        } catch (IllegalArgumentException exception) {
            CartSupport.flash(session, null, exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Unable to update buyer cart.", exception);
            CartSupport.flash(session, null, "We could not update your cart. Please try again.");
        }
        response.sendRedirect(request.getContextPath() + "/buyer/cart");
    }

    private void addItem(ShoppingCart cart, HttpServletRequest request) throws SQLException {
        long productId = positiveLong(request.getParameter("productId"), "Product");
        int quantity = positiveInt(request.getParameter("quantity"), 1);
        Product product = productDao.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException("This product is no longer available."));
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }
        int resulting = cart.getQuantities().getOrDefault(productId, 0) + quantity;
        if (resulting > product.getStockQuantity()) {
            throw new IllegalArgumentException("Your cart quantity exceeds the available stock for this product.");
        }
        cart.add(productId, quantity);
    }

    private void updateItem(ShoppingCart cart, HttpServletRequest request) throws SQLException {
        long productId = positiveLong(request.getParameter("productId"), "Product");
        int quantity = positiveInt(request.getParameter("quantity"), 0);
        Product product = productDao.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException("This product is no longer available."));
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }
        cart.setQuantity(productId, quantity);
    }

    private static void removeItem(ShoppingCart cart, HttpServletRequest request) {
        long productId = positiveLong(request.getParameter("productId"), "Product");
        if (!cart.remove(productId)) {
            throw new IllegalArgumentException("That product was not in your cart.");
        }
    }

    private static long positiveLong(String raw, String label) {
        try {
            long value = Long.parseLong(raw == null ? "" : raw.trim());
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " ID is invalid.");
        }
    }

    private static int positiveInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 1 || value > ShoppingCart.MAX_QUANTITY_PER_PRODUCT) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        }
    }

    private static String text(String value, String error) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(error);
        return value.trim();
    }

    private static UserAccount authenticatedUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
