
package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.ActivityLogDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/activity")
public final class AdminActivityServlet extends HttpServlet {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Object value = session == null
                ? null
                : session.getAttribute("authenticatedUser");

        if (!(value instanceof UserAccount admin)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (admin.role() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            request.setAttribute(
                    "activities", activityLogDao.findRecent(100));
            request.setAttribute("displayName", admin.fullName());

            request.getRequestDispatcher(
                    "/WEB-INF/views/dashboard/admin-activity.jsp")
                    .forward(request, response);

        } catch (SQLException exception) {
            getServletContext().log(
                    "Unable to load administrator activity monitoring.",
                    exception);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load activity records.");
        }
    }
}
