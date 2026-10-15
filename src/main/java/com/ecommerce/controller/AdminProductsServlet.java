
package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.security.CsrfTokens;
import com.ecommerce.repository.jdbc.UserDao;
import com.ecommerce.repository.jdbc.ActivityLogDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/products")
public final class AdminProductsServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDao();
        private final UserDao userDao = new UserDao();
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
            List<Product> products = productDao.findAll();

            request.setAttribute("products", products);
            request.setAttribute("displayName", admin.fullName());
            request.setAttribute(
                    "csrfToken",
                    CsrfTokens.getOrCreate(session));

            request.setAttribute(
                    "successMessage",
                    session.getAttribute("adminProductsSuccess"));
            request.setAttribute(
                    "errorMessage",
                    session.getAttribute("adminProductsError"));

            session.removeAttribute("adminProductsSuccess");
            session.removeAttribute("adminProductsError");

            request.getRequestDispatcher(
                    "/WEB-INF/views/dashboard/admin-products.jsp")
                    .forward(request, response);

        } catch (SQLException exception) {
            getServletContext().log(
                    "Unable to load administrator product management.",
                    exception);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load product listings.");
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
            String action = requiredParameter(request, "action");
            long productId = "create".equals(action)
        ? 0
        : parseProductId(request.getParameter("productId"));

            switch (action) {
                
                case "create" -> {
                long sellerId;

                try {
                        sellerId = Long.parseLong(
                                requiredParameter(request, "sellerId"));

                        if (sellerId <= 0) {
                        throw new NumberFormatException();
                        }
                } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                                "Enter a valid seller ID.");
                }

                UserAccount seller = userDao.findById(sellerId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Seller account was not found."));

                if (seller.role() != Role.SELLER || !seller.active()) {
                        throw new IllegalArgumentException(
                                "Select an active seller account.");
                }

                String name = requiredParameter(request, "name");
                String category = requiredParameter(request, "category");
                String description = request.getParameter("description");

                BigDecimal price;
                int stockQuantity;

                try {
                        price = new BigDecimal(
                                requiredParameter(request, "price"));
                        stockQuantity = Integer.parseInt(
                                requiredParameter(request, "stockQuantity"));
                } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                                "Enter a valid price and whole-number stock quantity.");
                }

                Product draft = Product.draft(
                        sellerId,
                        name,
                        description,
                        category,
                        price,
                        stockQuantity);

                productDao.create(draft);

                recordActivity(admin, "ADMIN_PRODUCT_CREATED", "PRODUCT", null);

                setFlash(session, "Product listing created successfully.", null);
                }

                case "update" -> {
                    Product product = productDao.findById(productId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Product listing not found."));

                    String name =
                            requiredParameter(request, "name");
                    String description =
                            request.getParameter("description");
                    String category =
                            requiredParameter(request, "category");
                    BigDecimal price = new BigDecimal(
                            requiredParameter(request, "price"));

                    product.updateDetails(
                            name, description, category, price);

                    if (!productDao.updateDetails(product)) {
                        throw new IllegalArgumentException(
                                "Product listing could not be updated.");
                    }

                    recordActivity(admin, "ADMIN_PRODUCT_UPDATED", "PRODUCT", productId);

                    setFlash(
                            session,
                            "Product details updated successfully.",
                            null);
                }

                case "deactivate" -> {
                    if (!productDao.deactivate(productId)) {
                        throw new IllegalArgumentException(
                                "Product not found or already inactive.");
                    }

                    recordActivity(admin, "ADMIN_PRODUCT_DEACTIVATED", "PRODUCT", productId);

                    setFlash(
                            session,
                            "Product listing deactivated.",
                            null);
                }

                case "activate" -> {
                    if (!productDao.activate(productId)) {
                        throw new IllegalArgumentException(
                                "Product not found or already active.");
                    }

                    recordActivity(admin, "ADMIN_PRODUCT_ACTIVATED", "PRODUCT", productId);

                    setFlash(
                            session,
                            "Product listing reactivated.",
                            null);
                }

                default -> throw new IllegalArgumentException(
                        "Choose a valid product-management action.");
            }

        } catch (IllegalArgumentException exception) {
            setFlash(session, null, exception.getMessage());

        } catch (SQLException exception) {
            getServletContext().log(
                    "Administrator product-management operation failed.",
                    exception);

            setFlash(
                    session,
                    null,
                    "The operation failed. Check the server log.");
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/products");
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

    private static long parseProductId(String value) {
        try {
            long id = Long.parseLong(
                    value == null ? "" : value.trim());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "The product ID is invalid.");
        }
    }

    private static void setFlash(
            HttpSession session, String success, String error) {

        session.removeAttribute("adminProductsSuccess");
        session.removeAttribute("adminProductsError");

        if (success != null) {
            session.setAttribute("adminProductsSuccess", success);
        }

        if (error != null) {
            session.setAttribute("adminProductsError", error);
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
