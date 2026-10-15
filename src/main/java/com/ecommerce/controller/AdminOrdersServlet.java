
package com.ecommerce.controller;

import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.OrderDao;
import com.ecommerce.repository.jdbc.PersistedOrder;
import com.ecommerce.security.CsrfTokens;
import com.ecommerce.repository.jdbc.ActivityLogDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/admin/orders")
public final class AdminOrdersServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserAccount admin = authenticatedUser(session);

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (admin.role() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            List<Map<String, Object>> orders =
                    orderDao.findAllForAdmin()
                            .stream()
                            .map(AdminOrdersServlet::toViewRow)
                            .toList();

            request.setAttribute("orders", orders);
            request.setAttribute("displayName", admin.fullName());
            request.setAttribute(
                    "csrfToken", CsrfTokens.getOrCreate(session));

            request.setAttribute(
                    "successMessage",
                    session.getAttribute("adminOrdersSuccess"));
            request.setAttribute(
                    "errorMessage",
                    session.getAttribute("adminOrdersError"));

            session.removeAttribute("adminOrdersSuccess");
            session.removeAttribute("adminOrdersError");

            request.getRequestDispatcher(
                    "/WEB-INF/views/dashboard/admin-orders.jsp")
                    .forward(request, response);

        } catch (SQLException exception) {
            getServletContext().log(
                    "Unable to load administrator order management.",
                    exception);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load marketplace orders.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        UserAccount admin = authenticatedUser(session);

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (admin.role() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!CsrfTokens.isValid(
                session, request.getParameter("csrfToken"))) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid or expired form token.");
            return;
        }

        try {
            long orderId = parseOrderId(
                    request.getParameter("orderId"));

            String submittedStatus =
                    requiredParameter(request, "nextStatus")
                            .toUpperCase(Locale.ROOT);

            OrderStatus nextStatus;
            try {
                nextStatus = OrderStatus.valueOf(submittedStatus);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Choose a valid order status.");
            }

            if (!orderDao.updateStatus(orderId, nextStatus)) {
                throw new IllegalArgumentException(
                        "Order not found.");
            }

            recordActivity(
                admin,
                "ADMIN_ORDER_STATUS_UPDATED_TO_" + nextStatus.name(),
                "ORDER",
                orderId);

            setFlash(
                    session,
                    "Order #" + orderId + " status updated to "
                            + nextStatus + ".",
                    null);

        } catch (IllegalArgumentException exception) {
            setFlash(session, null, exception.getMessage());

        } catch (SQLException exception) {
            getServletContext().log(
                    "Administrator order update failed.",
                    exception);

            setFlash(
                    session,
                    null,
                    "The order update failed. Check the server log.");
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/orders");
    }

    private static Map<String, Object> toViewRow(PersistedOrder order) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("id", order.id());
        row.put("buyerId", order.buyerId());
        row.put("status", order.status().name());
        row.put("totalAmount", order.totalAmount());
        row.put("paymentMethod", order.paymentMethod());
        row.put("paymentStatus", order.paymentStatus());
        row.put("createdAtDisplay", order.getCreatedAtDisplay());
        row.put("shippingAddress", order.shippingAddressSnapshot());
        row.put("items", order.items());

        row.put("nextStatuses", switch (order.status()) {
            case PLACED ->
                    List.of("CONFIRMED", "CANCELLED");
            case CONFIRMED ->
                    List.of("PROCESSING", "CANCELLED");
            case PROCESSING ->
                    List.of("SHIPPED", "CANCELLED");
            case SHIPPED ->
                    List.of("DELIVERED");
            case DELIVERED, CANCELLED ->
                    List.of();
        });

        return row;
    }

    private static UserAccount authenticatedUser(HttpSession session) {
        Object value = session == null
                ? null
                : session.getAttribute("authenticatedUser");

        return value instanceof UserAccount account ? account : null;
    }

    private static String requiredParameter(
            HttpServletRequest request, String name) {

        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "The " + name + " field is required.");
        }

        return value.trim();
    }

    private static long parseOrderId(String value) {
        try {
            long id = Long.parseLong(
                    value == null ? "" : value.trim());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("The order ID is invalid.");
        }
    }

    private static void setFlash(
            HttpSession session, String success, String error) {

        session.removeAttribute("adminOrdersSuccess");
        session.removeAttribute("adminOrdersError");

        if (success != null) {
            session.setAttribute("adminOrdersSuccess", success);
        }

        if (error != null) {
            session.setAttribute("adminOrdersError", error);
        }
    }

    private void recordActivity(
        UserAccount actor,
        String action,
        String entityType,
        Long entityId) {

    try {
        activityLogDao.record(
                actor.id(), action, entityType, entityId);
    } catch (SQLException exception) {
        getServletContext().log(
                "Could not record administrator activity: " + action,
                exception);
    }
}
}
