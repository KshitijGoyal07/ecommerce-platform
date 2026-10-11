package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.OrderDao;
import com.ecommerce.security.CsrfTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Shows only orders belonging to the signed-in buyer. */
@WebServlet("/buyer/orders")
public final class BuyerOrdersServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        if (!(value instanceof UserAccount account) || account.role() != Role.BUYER || !account.active()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            request.setAttribute("orders", orderDao.findByBuyerId(account.id()));
            request.setAttribute("displayName", account.fullName());
            request.setAttribute("pageTitle", "Your orders | MarketSquare");
            CartSupport.consumeFlash(session, request);
            request.getRequestDispatcher("/WEB-INF/views/dashboard/orders.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load buyer order history.", exception);
            request.setAttribute("errorMessage", "Your order history is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }
}
