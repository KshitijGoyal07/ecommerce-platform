package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.security.CsrfTokens;
import com.ecommerce.service.ProductListingInput;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Handles seller-owned product creation, editing, stock changes, and soft-deactivation. */
@WebServlet("/seller/products")
public final class SellerProductServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        UserAccount account = authenticatedUser(session);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (account.role() != Role.SELLER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only seller accounts can manage product listings.");
            return;
        }
        if (!CsrfTokens.isValid(session, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form token expired. Refresh the seller workspace and try again.");
            return;
        }

        try {
            String action = requiredParameter(request, "action");
            switch (action) {
                case "create" -> createProduct(request, account);
                case "update" -> updateProduct(request, account);
                case "stock" -> updateStock(request, account);
                case "activate" -> activateProduct(request, account);
                case "deactivate" -> deactivateProduct(request, account);
                default -> throw new IllegalArgumentException("Choose a valid product action.");
            }
        } catch (IllegalArgumentException exception) {
            setFlash(session, null, exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Seller product operation failed.", exception);
            setFlash(session, null,
                    "We could not save this listing. Check product/category database permissions and the Tomcat log.");
        }
        response.sendRedirect(request.getContextPath() + "/seller/dashboard");
    }

    private void createProduct(HttpServletRequest request, UserAccount account) throws SQLException {
        ProductListingInput input = formInput(request);
        int stock = ProductListingInput.parseStockQuantity(request.getParameter("stockQuantity"));
        Product created = productDao.create(input.toDraft(account.id(), stock));
        setFlash(request.getSession(false), "Product " + created.getName() + " was created.", null);
    }

    private void updateProduct(HttpServletRequest request, UserAccount account) throws SQLException {
        long productId = parseProductId(request.getParameter("productId"));
        ProductListingInput input = formInput(request);
        Product existing = productDao.findById(productId)
                .filter(product -> product.getSellerId() == account.id())
                .orElseThrow(() -> new IllegalArgumentException("Product not found in your seller account."));
        input.applyTo(existing);
        if (!productDao.updateDetailsForSeller(existing, account.id())) {
            throw new IllegalArgumentException("Product not found in your seller account.");
        }
        setFlash(request.getSession(false), "Product details were updated.", null);
    }

    private void updateStock(HttpServletRequest request, UserAccount account) throws SQLException {
        long productId = parseProductId(request.getParameter("productId"));
        int quantity = ProductListingInput.parseStockQuantity(request.getParameter("stockQuantity"));
        if (!productDao.setStockQuantityForSeller(account.id(), productId, quantity)) {
            throw new IllegalArgumentException("Product not found in your seller account.");
        }
        setFlash(request.getSession(false), "Inventory quantity was updated.", null);
    }

    private void deactivateProduct(HttpServletRequest request, UserAccount account) throws SQLException {
        long productId = parseProductId(request.getParameter("productId"));
        if (!productDao.deactivateForSeller(account.id(), productId)) {
            throw new IllegalArgumentException("Product not found in your seller account.");
        }
        setFlash(request.getSession(false), "Product listing was deactivated and hidden from buyers.", null);
    }

    private void activateProduct(HttpServletRequest request, UserAccount account)
        throws SQLException {
        long productId = parseProductId(request.getParameter("productId"));

        if (!productDao.activateForSeller(account.id(), productId)) {
            throw new IllegalArgumentException(
                    "Product not found in your seller account.");
        }

        setFlash(
                request.getSession(false),
                "Product listing was reactivated and is now visible to buyers.",
                null);
    }

    private static ProductListingInput formInput(HttpServletRequest request) {
        return ProductListingInput.fromForm(
                request.getParameter("name"), request.getParameter("description"),
                request.getParameter("category"), request.getParameter("price"));
    }

    private static String requiredParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A product action is required.");
        }
        return value.trim();
    }

    private static long parseProductId(String value) {
        try {
            long id = Long.parseLong(value == null ? "" : value.trim());
            if (id <= 0) {
                throw new NumberFormatException("non-positive");
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("The product ID is invalid.");
        }
    }

    private static UserAccount authenticatedUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }

    private static void setFlash(HttpSession session, String success, String error) {
        if (session == null) {
            return;
        }
        session.removeAttribute("sellerSuccess");
        session.removeAttribute("sellerError");
        if (success != null) {
            session.setAttribute("sellerSuccess", success);
        }
        if (error != null && !error.isBlank()) {
            session.setAttribute("sellerError", error);
        }
    }
}
