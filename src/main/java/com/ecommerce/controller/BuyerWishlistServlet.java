package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
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

/** Displays and updates the authenticated buyer's wishlist. */
@WebServlet("/buyer/wishlist")
public final class BuyerWishlistServlet extends HttpServlet {
    private static final String MESSAGE_KEY = "wishlistMessage";
    private static final String ERROR_KEY = "wishlistError";
    private final WishlistDao wishlistDao = new WishlistDao();
    private final ProductDao productDao = new ProductDao();

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
            request.setAttribute("wishlistProducts", wishlistDao.findByBuyerId(account.id()));
            request.setAttribute(
                "wishlistSavedPrices",
                wishlistDao.findSavedPricesByBuyerId(account.id()));
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("cartQuantity", CartSupport.getCart(session).getTotalQuantity());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
            request.setAttribute("pageTitle", "Your wishlist | MarketSquare");
            request.setAttribute("wishlistMessage", session.getAttribute(MESSAGE_KEY));
            request.setAttribute("wishlistError", session.getAttribute(ERROR_KEY));
            session.removeAttribute(MESSAGE_KEY);
            session.removeAttribute(ERROR_KEY);
            request.getRequestDispatcher("/WEB-INF/views/dashboard/wishlist.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load buyer wishlist.", exception);
            request.setAttribute("errorMessage", "Your wishlist is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        UserAccount account = authenticatedBuyer(session);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!CsrfTokens.isValid(session, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form token expired. Refresh and try again.");
            return;
        }
        String target = returnPath(request.getParameter("returnTo"));
        try {
            long productId = positiveLong(request.getParameter("productId"));
            String action = request.getParameter("action");
            if ("add".equals(action)) {
                Product product = productDao.findById(productId).filter(Product::isActive)
                        .orElseThrow(() -> new IllegalArgumentException("This product is no longer available."));
                boolean added = wishlistDao.add(account.id(), product.getId());
                session.setAttribute(MESSAGE_KEY, added ? "Product saved to your wishlist." : "This product is already in your wishlist.");
                session.removeAttribute(ERROR_KEY);
            } else if ("remove".equals(action)) {
                boolean removed = wishlistDao.remove(account.id(), productId);
                session.setAttribute(MESSAGE_KEY, removed ? "Product removed from your wishlist." : "That product was not in your wishlist.");
                session.removeAttribute(ERROR_KEY);
            } else {
                throw new IllegalArgumentException("Choose a valid wishlist action.");
            }
        } catch (IllegalArgumentException exception) {
            session.setAttribute(ERROR_KEY, exception.getMessage());
            session.removeAttribute(MESSAGE_KEY);
        } catch (SQLException exception) {
            getServletContext().log("Unable to update buyer wishlist.", exception);
            session.setAttribute(ERROR_KEY, "We could not update your wishlist. Please try again.");
            session.removeAttribute(MESSAGE_KEY);
        }
        response.sendRedirect(request.getContextPath() + target);
    }

    private static UserAccount authenticatedBuyer(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (!(value instanceof UserAccount account) || account.role() != Role.BUYER) return null;
        return account;
    }

    private static long positiveLong(String raw) {
        try {
            long value = Long.parseLong(raw == null ? "" : raw.trim());
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Product ID is invalid.");
        }
    }

    private static String returnPath(String value) {
        if ("dashboard".equals(value)) return "/buyer/dashboard";
        if ("wishlist".equals(value)) return "/buyer/wishlist";
        if (value != null && value.matches("product-[1-9][0-9]{0,18}")) {
            return "/buyer/product?id=" + value.substring("product-".length());
        }
        return "/buyer/wishlist";
    }
}
