package com.ecommerce.controller;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.UserDao;
import com.ecommerce.security.CsrfTokens;
import com.ecommerce.repository.jdbc.ActivityLogDao;
import com.ecommerce.security.PasswordHasher;

import java.util.Arrays;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/admin/users")
public final class AdminUsersServlet extends HttpServlet {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserAccount admin = authenticatedUser(session);

        if (admin == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        if (admin.role() != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            List<Map<String, Object>> users =
                    userDao.findAllAccounts()
                            .stream()
                            .map(AdminUsersServlet::toViewRow)
                            .toList();

            request.setAttribute("users", users);
            request.setAttribute("displayName", admin.fullName());
            request.setAttribute(
                    "csrfToken", CsrfTokens.getOrCreate(session));

            request.setAttribute(
                    "successMessage",
                    session.getAttribute("adminUsersSuccess"));
            request.setAttribute(
                    "errorMessage",
                    session.getAttribute("adminUsersError"));

            session.removeAttribute("adminUsersSuccess");
            session.removeAttribute("adminUsersError");
            request.getRequestDispatcher(
                    "/WEB-INF/views/dashboard/admin-users.jsp")
                    .forward(request, response);

        } catch (SQLException exception) {
            getServletContext().log(
                    "Unable to load administrator user management.",
                    exception);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load user accounts.");
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
            response.sendRedirect(
                    request.getContextPath() + "/login");
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
            long userId = "create".equals(action)
                ? 0
                : parseUserId(request.getParameter("userId"));

            switch (action) {
                
                case "delete" -> {
                if (userId == admin.id()) {
                        throw new IllegalArgumentException(
                                "You cannot delete your own administrator account.");
                }

                if (!userDao.deleteAccount(userId)) {
                        throw new IllegalArgumentException(
                                "User account not found.");
                }

                recordActivity(admin, "ADMIN_USER_DELETED", "USER", userId);

                setFlash(session, "User account deleted successfully.", null);
                }

                case "create" -> {
                String fullName = requiredParameter(request, "fullName");
                String email = requiredParameter(request, "email");
                String password = request.getParameter("password");

                if (password == null || password.isBlank()) {
                        throw new IllegalArgumentException(
                                "Password is required.");
                }

                Role role = Role.valueOf(
                        requiredParameter(request, "role")
                                .toUpperCase(Locale.ROOT));

                char[] passwordChars = password.toCharArray();
                String passwordHash;

                try {
                        passwordHash = PasswordHasher.hash(passwordChars);
                } finally {
                        Arrays.fill(passwordChars, '\0');
                }

                UserAccount created = userDao.create(
                        fullName, email, passwordHash, role);

                recordActivity(
                        admin, "ADMIN_USER_CREATED", "USER", created.id());

                setFlash(session, "User account created successfully.", null);
                }

                case "update" -> {
                    String fullName =
                            requiredParameter(request, "fullName");
                    String email =
                            requiredParameter(request, "email");

                    Role role = Role.valueOf(
                            requiredParameter(request, "role")
                                    .toUpperCase(Locale.ROOT));

                    if (userId == admin.id() && role != Role.ADMIN) {
                        throw new IllegalArgumentException(
                                "You cannot remove your own administrator role.");
                    }

                    boolean updated = userDao.updateAccount(
                            userId, fullName, email, role);

                    if (!updated) {
                        throw new IllegalArgumentException(
                                "User account not found.");
                    }

                    recordActivity(admin, "ADMIN_USER_UPDATED", "USER", userId);

                    setFlash(session, "User account updated.", null);
                }

                case "deactivate" -> {
                    if (userId == admin.id()) {
                        throw new IllegalArgumentException(
                                "You cannot deactivate your own account.");
                    }

                    if (!userDao.deactivate(userId)) {
                        throw new IllegalArgumentException(
                                "User account not found or already inactive.");
                    }

                    recordActivity(admin, "ADMIN_USER_DEACTIVATED", "USER", userId);

                    setFlash(
                            session,
                            "User account deactivated.",
                            null);
                }

                case "activate" -> {
                    if (!userDao.activate(userId)) {
                        throw new IllegalArgumentException(
                                "User account not found or already active.");
                    }

                    recordActivity(admin, "ADMIN_USER_ACTIVATED", "USER", userId);

                    setFlash(
                            session,
                            "User account reactivated.",
                            null);
                }

                default -> throw new IllegalArgumentException(
                        "Choose a valid user-management action.");
            }

        } catch (IllegalArgumentException exception) {
            setFlash(session, null, exception.getMessage());

        } catch (SQLException exception) {
            getServletContext().log(
                    "Administrator user-management operation failed.",
                    exception);

            setFlash(
                    session,
                    null,
                    "The operation failed. Check the server log.");
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/users");
    }

    private static Map<String, Object> toViewRow(UserAccount user) {
        Map<String, Object> row = new LinkedHashMap<>();

        row.put("id", user.id());
        row.put("fullName", user.fullName());
        row.put("email", user.email());
        row.put("role", user.role().name());
        row.put("active", user.active());
        row.put("createdAt", user.createdAt().toString());

        return row;
    }

    private static UserAccount authenticatedUser(HttpSession session) {
        Object value = session == null
                ? null
                : session.getAttribute("authenticatedUser");

        return value instanceof UserAccount account
                ? account
                : null;
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

    private static long parseUserId(String value) {
        try {
            long id = Long.parseLong(
                    value == null ? "" : value.trim());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "The user ID is invalid.");
        }
    }

    private static void setFlash(
            HttpSession session, String success, String error) {

        session.removeAttribute("adminUsersSuccess");
        session.removeAttribute("adminUsersError");

        if (success != null) {
            session.setAttribute("adminUsersSuccess", success);
        }

        if (error != null) {
            session.setAttribute("adminUsersError", error);
        }
    }

    
        private void recordActivity(
                UserAccount actor,
                String action,
                String entityType,
                long entityId) {
 
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