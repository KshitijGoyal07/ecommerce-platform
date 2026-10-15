
package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.BuyerAccountDao;
import com.ecommerce.repository.jdbc.UserDao;
import com.ecommerce.security.CsrfTokens;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/buyer/account")
public final class BuyerAccountServlet extends HttpServlet {

    private final BuyerAccountDao accountDao = new BuyerAccountDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserAccount buyer = authenticatedBuyer(session);

        if (buyer == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            request.setAttribute("displayName", buyer.fullName());
            request.setAttribute("email", buyer.email());
            request.setAttribute(
                    "addresses", accountDao.findAddresses(buyer.id()));
            request.setAttribute(
                    "paymentMethods",
                    accountDao.findPaymentMethods(buyer.id()));
            request.setAttribute(
                    "csrfToken", CsrfTokens.getOrCreate(session));

            request.setAttribute(
                    "successMessage",
                    session.getAttribute("buyerAccountSuccess"));
            request.setAttribute(
                    "errorMessage",
                    session.getAttribute("buyerAccountError"));

            session.removeAttribute("buyerAccountSuccess");
            session.removeAttribute("buyerAccountError");

            request.getRequestDispatcher(
                    "/WEB-INF/views/dashboard/account.jsp")
                    .forward(request, response);

        } catch (SQLException exception) {
            getServletContext().log(
                    "Unable to load buyer account information.", exception);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load your account information.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserAccount buyer = authenticatedBuyer(session);

        if (buyer == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!CsrfTokens.isValid(
                session, request.getParameter("csrfToken"))) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid or expired form token.");
            return;
        }

        String successMessage;

        try {
            String action = required(request, "action");

            switch (action) {
                case "updateProfile" -> {
                    String name = required(request, "fullName");
                    String email = required(request, "email");

                    if (!userDao.updateAccount(
                            buyer.id(), name, email, Role.BUYER)) {
                        throw new IllegalArgumentException(
                                "Your account could not be updated.");
                    }

                    UserAccount updated = userDao.findById(buyer.id())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Buyer account not found."));

                    session.setAttribute("authenticatedUser", updated);
                    successMessage = "Account details updated successfully.";
                }

                case "addAddress" -> {
                    accountDao.addAddress(
                            buyer.id(),
                            required(request, "label"),
                            required(request, "recipientName"),
                            required(request, "phone"),
                            required(request, "line1"),
                            request.getParameter("line2"),
                            required(request, "city"),
                            required(request, "state"),
                            required(request, "postalCode"),
                            request.getParameter("country"));

                    successMessage = "Address added successfully.";
                }

                case "deleteAddress" -> {
                    long addressId = positiveLong(
                            request, "addressId");

                    if (!accountDao.deleteAddress(buyer.id(), addressId)) {
                        throw new IllegalArgumentException(
                                "Address not found in your address book.");
                    }

                    successMessage = "Address removed successfully.";
                }

                case "defaultAddress" -> {
                    long addressId = positiveLong(
                            request, "addressId");

                    if (!accountDao.setDefaultAddress(
                            buyer.id(), addressId)) {
                        throw new IllegalArgumentException(
                                "Address not found in your address book.");
                    }

                    successMessage = "Default address updated.";
                }

                case "addPayment" -> {
                    accountDao.addPaymentMethod(
                            buyer.id(),
                            required(request, "type"),
                            required(request, "label"),
                            request.getParameter("lastFour"));

                    successMessage = "Payment method saved successfully.";
                }

                case "deletePayment" -> {
                    long paymentId = positiveLong(
                            request, "paymentMethodId");

                    if (!accountDao.deletePaymentMethod(
                            buyer.id(), paymentId)) {
                        throw new IllegalArgumentException(
                                "Payment method not found.");
                    }

                    successMessage = "Payment method removed successfully.";
                }

                case "defaultPayment" -> {
                    long paymentId = positiveLong(
                            request, "paymentMethodId");

                    if (!accountDao.setDefaultPaymentMethod(
                            buyer.id(), paymentId)) {
                        throw new IllegalArgumentException(
                                "Payment method not found.");
                    }

                    successMessage = "Default payment method updated.";
                }

                default -> throw new IllegalArgumentException(
                        "Choose a valid account action.");
            }

            setFlash(session, successMessage, null);

        } catch (IllegalArgumentException exception) {
            setFlash(session, null, exception.getMessage());

        } catch (SQLException exception) {
            getServletContext().log(
                    "Unable to update buyer account information.", exception);
            setFlash(
                    session, null,
                    "The account operation failed. Check your details and try again.");
        }

        response.sendRedirect(
                request.getContextPath() + "/buyer/account");
    }

    private UserAccount authenticatedBuyer(HttpSession session)
            throws IOException {
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("authenticatedUser");

        if (!(value instanceof UserAccount account)) {
            return null;
        }

        if (account.role() != Role.BUYER || !account.active()) {
            return null;
        }

        return account;
    }

    private static String required(
            HttpServletRequest request, String name) {
        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Please complete the " + name + " field.");
        }

        return value.trim();
    }

    private static long positiveLong(
            HttpServletRequest request, String name) {
        try {
            long value = Long.parseLong(required(request, name));

            if (value <= 0) {
                throw new NumberFormatException();
            }

            return value;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + name + ".");
        }
    }

    private static void setFlash(
            HttpSession session, String success, String error) {
        session.removeAttribute("buyerAccountSuccess");
        session.removeAttribute("buyerAccountError");

        if (success != null) {
            session.setAttribute("buyerAccountSuccess", success);
        }

        if (error != null) {
            session.setAttribute("buyerAccountError", error);
        }
    }
}
