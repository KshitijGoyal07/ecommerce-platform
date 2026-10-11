package com.ecommerce.controller;

import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.SellerOrderReportDao;
import com.ecommerce.repository.jdbc.ProductDao;
import com.ecommerce.security.CsrfTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/seller/dashboard")
public final class SellerDashboardServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final SellerOrderReportDao sellerOrderReportDao =
        new SellerOrderReportDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserAccount account = authenticatedUser(request);
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            var products = productDao.findBySellerId(account.id());
            request.setAttribute("products", products);
            request.setAttribute("productCount", products.size());
            try {
                request.setAttribute(
                        "sellerOrders",
                        sellerOrderReportDao.findOrdersForSeller(account.id())
                );

                request.setAttribute(
                        "salesSummary",
                        sellerOrderReportDao.getSalesSummary(account.id())
                );

            } catch (SQLException reportException) {
                getServletContext().log(
                        "Unable to load seller order reports.",
                        reportException
                );

                request.setAttribute(
                        "sellerReportError",
                        "Seller order reports are temporarily unavailable."
                );

                request.setAttribute(
                        "sellerOrders",
                        java.util.Collections.emptyList()
                );
            }
            request.setAttribute("displayName", account.fullName());
            String sellerView = request.getParameter("view");
            if (sellerView == null || !java.util.Set.of("create", "listings", "sales", "orders").contains(sellerView)) {
                sellerView = "create";
            }
            request.setAttribute("sellerView", sellerView);
            request.setAttribute("pageTitle", "Seller workspace | MarketSquare");
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession(true)));
            HttpSession session = request.getSession(false);
            if (session != null) {
                request.setAttribute("sellerSuccess", session.getAttribute("sellerSuccess"));
                request.setAttribute("sellerError", session.getAttribute("sellerError"));
                session.removeAttribute("sellerSuccess");
                session.removeAttribute("sellerError");
            }
            request.getRequestDispatcher("/WEB-INF/views/dashboard/seller.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load seller inventory.", exception);
            request.setAttribute("errorMessage", "Your inventory is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/dashboard/error.jsp").forward(request, response);
        }
    }

    private static UserAccount authenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("authenticatedUser");
        return value instanceof UserAccount account ? account : null;
    }
}
