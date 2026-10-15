<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Admin User Management | MarketSquare</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/application.css">

    <style>
        .admin-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .admin-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        .admin-table th,
        .admin-table td {
            padding: 14px 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid #343b52;
        }

        .admin-table th {
            font-size: 0.85rem;
            white-space: nowrap;
        }

        .admin-table input,
        .admin-table select {
            box-sizing: border-box;
            width: 100%;
            min-width: 120px;
            padding: 9px;
            border: 1px solid #414a65;
            border-radius: 7px;
        }

        .admin-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            min-width: 150px;
        }

        .admin-actions form {
            margin: 0;
        }

        .admin-message {
            padding: 12px 16px;
            margin: 14px 0;
            border-radius: 8px;
        }

        .admin-success {
            border: 1px solid #398b68;
        }

        .admin-error {
            border: 1px solid #c65b65;
        }

        .account-status {
            font-weight: 600;
        }
    </style>
</head>

<body class="app-body">

<header class="site-header">
    <a class="brand"
       href="${pageContext.request.contextPath}/home">
        <span class="brand-mark">M</span>
        <span>MarketSquare</span>
    </a>

    <div class="header-user">
        <span>Admin workspace</span>

        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/admin/dashboard">
            Dashboard
        </a>

        <form method="post"
              action="${pageContext.request.contextPath}/logout">
            <button class="button button-secondary" type="submit">
                Sign out
            </button>
        </form>
    </div>
</header>

<main class="dashboard-layout">

    <section class="dashboard-intro">
        <p class="eyebrow">ADMIN WORKSPACE</p>

        <h1 class="dashboard-title">
            User management<span>.</span>
        </h1>

        <p class="muted-copy">
            Welcome,
            <c:out value="${displayName}" />.
            Review accounts, update user details, and manage account status.
        </p>
    </section>

    <section class="panel">

        <div class="panel-heading">
            <div>
                <p class="eyebrow">ACCOUNT ADMINISTRATION</p>
                <h2>Registered users</h2>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="admin-message admin-success" role="status">
                <c:out value="${successMessage}" />
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="admin-message admin-error" role="alert">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>
        
    <div class="panel-heading">
        <div>
            <p class="eyebrow">NEW ACCOUNT</p>
            <h2>Create user</h2>
        </div>
    </div>

    <form method="post"
        action="${pageContext.request.contextPath}/admin/users"
        style="display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:14px; margin-bottom:24px;">

        <input type="hidden"
            name="csrfToken"
            value="<c:out value='${csrfToken}' />">

        <input type="hidden" name="action" value="create">

        <label>
            Full name
            <input type="text" name="fullName"
                maxlength="120" required>
        </label>

        <label>
            Email
            <input type="email" name="email"
                maxlength="254" required>
        </label>

        <label>
            Temporary password
            <input type="password" name="password"
                autocomplete="new-password" required>
        </label>

        <label>
            Role
            <select name="role" required>
                <option value="BUYER">Buyer</option>
                <option value="SELLER">Seller</option>
                <option value="ADMIN">Admin</option>
            </select>
        </label>

        <div style="align-self:end;">
            <button class="button button-secondary" type="submit">
                Create user
            </button>
        </div>
    </form>

        <div class="admin-table-wrap">

            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Full name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>

                    <c:forEach var="user" items="${users}">
                        <tr>
                            <td>
                                <c:out value="${user.id}" />
                            </td>

                            <td>
                                <c:out value="${user.fullName}" />
                            </td>

                            <td>
                                <c:out value="${user.email}" />
                            </td>

                            <td>
                                <c:out value="${user.role}" />
                            </td>

                            <td class="account-status">
                                <c:choose>
                                    <c:when test="${user.active}">
                                        Active
                                    </c:when>
                                    <c:otherwise>
                                        Inactive
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:out value="${user.createdAt}" />
                            </td>

                            <td>
                                <div class="admin-actions">

                                    <!-- Update account details and role -->
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/users">

                                        <input type="hidden"
                                               name="csrfToken"
                                               value="<c:out value='${csrfToken}' />">

                                        <input type="hidden"
                                               name="action"
                                               value="update">

                                        <input type="hidden"
                                               name="userId"
                                               value="<c:out value='${user.id}' />">

                                        <label>
                                            Full name
                                            <input type="text"
                                                   name="fullName"
                                                   value="<c:out value='${user.fullName}' />"
                                                   maxlength="120"
                                                   required>
                                        </label>

                                        <label>
                                            Email
                                            <input type="email"
                                                   name="email"
                                                   value="<c:out value='${user.email}' />"
                                                   maxlength="254"
                                                   required>
                                        </label>

                                        <label>
                                            Role
                                            <select name="role" required>
                                                <option value="BUYER"
                                                    <c:if test="${user.role == 'BUYER'}">selected</c:if>>
                                                    Buyer
                                                </option>
                                                <option value="SELLER"
                                                    <c:if test="${user.role == 'SELLER'}">selected</c:if>>
                                                    Seller
                                                </option>
                                                <option value="ADMIN"
                                                    <c:if test="${user.role == 'ADMIN'}">selected</c:if>>
                                                    Admin
                                                </option>
                                            </select>
                                        </label>

                                        <button class="button button-secondary"
                                                type="submit">
                                            Save changes
                                        </button>
                                    </form>

                                    <!-- Activate or deactivate the account -->
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/users">

                                        <input type="hidden"
                                               name="csrfToken"
                                               value="<c:out value='${csrfToken}' />">

                                        <input type="hidden"
                                               name="userId"
                                               value="<c:out value='${user.id}' />">

                                        <c:choose>
                                            <c:when test="${user.active}">
                                                <input type="hidden"
                                                       name="action"
                                                       value="deactivate">

                                                <button class="button button-secondary"
                                                        type="submit">
                                                    Deactivate
                                                </button>
                                            </c:when>

                                            <c:otherwise>
                                                <input type="hidden"
                                                       name="action"
                                                       value="activate">

                                                <button class="button button-secondary"
                                                        type="submit">
                                                    Reactivate
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                                                        
                                    <form method="post"
                                        action="${pageContext.request.contextPath}/admin/users"
                                        onsubmit="return confirm('Permanently delete this account? Linked records may prevent deletion.');">

                                        <input type="hidden"
                                            name="csrfToken"
                                            value="<c:out value='${csrfToken}' />">

                                        <input type="hidden"
                                            name="userId"
                                            value="<c:out value='${user.id}' />">

                                        <input type="hidden" name="action" value="delete">

                                        <button class="button button-secondary" type="submit">
                                            Delete
                                        </button>
                                    </form>

                                </div>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty users}">
                        <tr>
                            <td colspan="7">
                                No user accounts were found.
                            </td>
                        </tr>
                    </c:if>

                </tbody>
            </table>

        </div>
    </section>

</main>

<footer class="site-footer">
    <span>MarketSquare · Admin</span>
    <span>Protected, role-specific dashboard</span>
</footer>

</body>
</html>