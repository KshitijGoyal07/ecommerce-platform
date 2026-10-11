package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.service.CartLineView;
import com.ecommerce.service.ShoppingCart;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Shared helpers for the buyer cart and checkout flow. */
final class CartSupport {
    static final String SESSION_KEY = "shoppingCart";
    static final String SUCCESS_KEY = "buyerCartMessage";
    static final String ERROR_KEY = "buyerCartError";

    private CartSupport() { }

    static ShoppingCart getCart(HttpSession session) {
        Object existing = session.getAttribute(SESSION_KEY);
        if (existing instanceof ShoppingCart cart) {
            return cart;
        }
        ShoppingCart cart = new ShoppingCart();
        session.setAttribute(SESSION_KEY, cart);
        return cart;
    }

    static List<CartLineView> loadLines(ShoppingCart cart, ProductDao productDao) throws SQLException {
        List<CartLineView> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.getQuantities().entrySet()) {
            Product product = productDao.findById(entry.getKey()).orElse(null);
            if (product == null || !product.isActive() || product.getStockQuantity() <= 0) {
                cart.remove(entry.getKey());
                continue;
            }
            int quantity = entry.getValue();
            if (quantity > product.getStockQuantity()) {
                quantity = product.getStockQuantity();
                cart.setQuantity(product.getId(), quantity);
            }
            lines.add(new CartLineView(product, quantity));
        }
        return List.copyOf(lines);
    }

    static BigDecimal subtotal(List<CartLineView> lines) {
        return lines.stream().map(CartLineView::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static void flash(HttpSession session, String success, String error) {
        session.removeAttribute(SUCCESS_KEY);
        session.removeAttribute(ERROR_KEY);
        if (success != null && !success.isBlank()) session.setAttribute(SUCCESS_KEY, success);
        if (error != null && !error.isBlank()) session.setAttribute(ERROR_KEY, error);
    }

    static void consumeFlash(HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        request.setAttribute("buyerCartMessage", session.getAttribute(SUCCESS_KEY));
        request.setAttribute("buyerCartError", session.getAttribute(ERROR_KEY));
        session.removeAttribute(SUCCESS_KEY);
        session.removeAttribute(ERROR_KEY);
    }
}
