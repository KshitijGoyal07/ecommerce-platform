package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.OrderDao;
import com.ecommerce.repository.jdbc.OrderLineRequest;
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
import java.util.ArrayList;
import java.util.List;

/** Performs checkout via OrderDao's stock-safe database transaction. */
@WebServlet("/buyer/checkout")
public final class BuyerCheckoutServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private final com.ecommerce.repository.jdbc.ProductDao productDao = new com.ecommerce.repository.jdbc.ProductDao();

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
            ShoppingCart cart = CartSupport.getCart(session);
            List<CartLineView> lines = CartSupport.loadLines(cart, productDao);
            if (lines.isEmpty()) {
                CartSupport.flash(session, null, "Your cart is empty. Add a product before checkout.");
                response.sendRedirect(request.getContextPath() + "/buyer/cart");
                return;
            }
            request.setAttribute("cartLines", lines);
            request.setAttribute("subtotal", CartSupport.subtotal(lines));
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
            request.setAttribute("pageTitle", "Checkout | MarketSquare");
            CartSupport.consumeFlash(session, request);
            request.getRequestDispatcher("/WEB-INF/views/dashboard/checkout.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to prepare checkout.", exception);
            request.setAttribute("errorMessage", "Checkout is temporarily unavailable.");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form token expired. Refresh checkout and try again.");
            return;
        }
        String address = request.getParameter("shippingAddress");
        if (address == null || address.isBlank() || address.trim().length() < 10 || address.trim().length() > 1000) {
            CartSupport.flash(session, null, "Enter a delivery address between 10 and 1,000 characters.");
            response.sendRedirect(request.getContextPath() + "/buyer/checkout");
            return;
        }

        ShoppingCart cart = CartSupport.getCart(session);
        try {
            List<OrderLineRequest> requested = new ArrayList<>();
            for (var entry : cart.getQuantities().entrySet()) {
                requested.add(new OrderLineRequest(entry.getKey(), entry.getValue()));
            }
            long orderId = orderDao.placeOrder(account.id(), requested, address.trim());
            cart.clear();
            CartSupport.flash(session, "Order #" + orderId + " was placed successfully.", null);
            response.sendRedirect(request.getContextPath() + "/buyer/orders?placed=" + orderId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            CartSupport.flash(session, null, exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/buyer/cart");
        } catch (SQLException exception) {
            getServletContext().log("Buyer checkout failed.", exception);
            CartSupport.flash(session, null,
                    "We could not place the order. Check the order-table permissions and Tomcat log, then retry.");
            response.sendRedirect(request.getContextPath() + "/buyer/cart");
        }
    }

    private static UserAccount authenticatedBuyer(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (value instanceof UserAccount account && account.role() == Role.BUYER && account.active()) {
            return account;
        }
        return null;
    }
}
