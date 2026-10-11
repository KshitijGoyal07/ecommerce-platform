package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.security.CsrfTokens;
import com.ecommerce.service.ProductListingInput;
import com.ecommerce.service.ProductImageStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Handles seller-owned product creation, editing, stock changes, and soft-deactivation. */
@WebServlet("/seller/products")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024)
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

        String targetView = "create";
        try {
            String action = requiredParameter(request, "action");
            targetView = "create".equals(action) ? "create" : "listings";
            switch (action) {
                case "create" -> createProduct(request, account);
                case "update" -> updateProduct(request, account);
                case "removeImage" -> removeProductImage(request, account);
                case "stock" -> updateStock(request, account);
                case "activate" -> activateProduct(request, account);
                case "deactivate" -> deactivateProduct(request, account);
                default -> throw new IllegalArgumentException("Choose a valid product action.");
            }
        } catch (IllegalArgumentException exception) {
            setFlash(session, null, exception.getMessage());
        } catch (IllegalStateException exception) {
            setFlash(session, null, "The product image must be 5 MB or smaller. Please choose a smaller JPEG or PNG image.");
        } catch (IOException exception) {
            getServletContext().log("Unable to save seller product image.", exception);
            setFlash(session, null, "The product image could not be processed. Please try another JPEG or PNG image.");
        } catch (SQLException exception) {
            getServletContext().log("Seller product operation failed.", exception);
            setFlash(session, null,
                    "We could not save this listing. Check product/category database permissions and the Tomcat log.");
        }
        response.sendRedirect(request.getContextPath()
                + "/seller/dashboard?view=" + targetView);
    }

    private void createProduct(HttpServletRequest request, UserAccount account)
            throws SQLException, IOException, ServletException {
        ProductListingInput input = formInput(request);
        int stock = ProductListingInput.parseStockQuantity(request.getParameter("stockQuantity"));
        Product draft = input.toDraft(account.id(), stock);
        String imagePath = ProductImageStorage.save(request.getPart("image"));
        if (imagePath != null) draft.setImagePath(imagePath);
        try {
            Product created = productDao.create(draft);
            setFlash(request.getSession(false), "Product " + created.getName() + " was created.", null);
        } catch (SQLException | RuntimeException exception) {
            ProductImageStorage.delete(imagePath);
            throw exception;
        }
    }

    private void updateProduct(HttpServletRequest request, UserAccount account)
            throws SQLException, IOException, ServletException {
        long productId = parseProductId(request.getParameter("productId"));
        ProductListingInput input = formInput(request);
        Product existing = productDao.findById(productId)
                .filter(product -> product.getSellerId() == account.id())
                .orElseThrow(() -> new IllegalArgumentException("Product not found in your seller account."));
        input.applyTo(existing);
        String previousImagePath = existing.getImagePath();
        String uploadedImagePath = ProductImageStorage.save(request.getPart("image"));
        if (uploadedImagePath != null) existing.setImagePath(uploadedImagePath);
        try {
            if (!productDao.updateDetailsForSeller(existing, account.id())) {
                throw new IllegalArgumentException("Product not found in your seller account.");
            }
            if (uploadedImagePath != null && previousImagePath != null) {
                ProductImageStorage.delete(previousImagePath);
            }
            setFlash(request.getSession(false), "Product details were updated.", null);
        } catch (SQLException | RuntimeException exception) {
            ProductImageStorage.delete(uploadedImagePath);
            throw exception;
        }
    }

    private void removeProductImage(HttpServletRequest request, UserAccount account)
            throws SQLException {
        long productId = parseProductId(request.getParameter("productId"));
        Product product = productDao.findById(productId)
                .filter(candidate -> candidate.getSellerId() == account.id())
                .orElseThrow(() -> new IllegalArgumentException("Product not found in your seller account."));
        String oldImagePath = product.getImagePath();
        if (oldImagePath == null) {
            setFlash(request.getSession(false), null, "This product does not have a photo to remove.");
            return;
        }
        product.setImagePath(null);
        if (!productDao.updateDetailsForSeller(product, account.id())) {
            throw new IllegalArgumentException("Product not found in your seller account.");
        }
        ProductImageStorage.delete(oldImagePath);
        setFlash(request.getSession(false), "Product photo removed.", null);
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
