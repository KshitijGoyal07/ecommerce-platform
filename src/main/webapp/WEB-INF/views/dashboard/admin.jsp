<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${pageTitle}" /></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/application.css">
</head><body class="app-body">
<header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a><div class="header-user"><span>Admin workspace</span><form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form></div></header>
<main class="dashboard-layout">
    <section class="dashboard-intro"><p class="eyebrow">ADMIN WORKSPACE</p><h1 class="dashboard-title">Marketplace overview<span>.</span></h1><p class="muted-copy">Welcome, <c:out value="${displayName}" />. Review the current size of the marketplace.</p></section>
    <section class="metric-grid"><article class="metric-card"><p class="eyebrow">ACTIVE ACCOUNTS</p><strong><c:out value="${activeUserCount}" /></strong><span>Buyer, seller, and admin accounts</span></article><article class="metric-card"><p class="eyebrow">PRODUCT RECORDS</p><strong><c:out value="${productCount}" /></strong><span>Active and inactive listings</span></article></section>

<c:if test="${not empty adminSuccess}">
    <section class="panel">
        <p role="status">
            <c:out value="${adminSuccess}" />
        </p>
    </section>
</c:if>

<c:if test="${not empty adminError}">
    <section class="panel">
        <p role="alert">
            <c:out value="${adminError}" />
        </p>
    </section>
</c:if>

<section class="panel">
    <div class="panel-heading">
        <div>
            <p class="eyebrow">ADMINISTRATION</p>
            <h2>User management</h2>
        </div>
    </div>

    <p class="muted-copy">
        Edit account details, manage roles, and activate or
        deactivate accounts. Existing order history is preserved.
    </p>

    <div style="overflow-x: auto;">
        <table style="width: 100%; min-width: 850px; border-collapse: collapse;">
            <thead>
                <tr>
                    <th style="text-align: left; padding: 12px;">ID</th>
                    <th style="text-align: left; padding: 12px;">Full name</th>
                    <th style="text-align: left; padding: 12px;">Email</th>
                    <th style="text-align: left; padding: 12px;">Role</th>
                    <th style="text-align: left; padding: 12px;">Status / actions</th>
                </tr>
            </thead>

            <tbody>
                <c:forEach var="user" items="${users}">
                    <tr>
                        <td style="padding: 12px;">
                            <form id="update-user-${user.id}"
                                  method="post"
                                  action="${pageContext.request.contextPath}/admin/users">
                            </form>

                            <input type="hidden"
                                   form="update-user-${user.id}"
                                   name="csrfToken"
                                   value="<c:out value='${csrfToken}' />">

                            <input type="hidden"
                                   form="update-user-${user.id}"
                                   name="action"
                                   value="update">

                            <input type="hidden"
                                   form="update-user-${user.id}"
                                   name="userId"
                                   value="${user.id}">

                            <c:out value="${user.id}" />
                        </td>

                        <td style="padding: 12px;">
                            <input form="update-user-${user.id}"
                                   name="fullName"
                                   aria-label="Full name"
                                   value="<c:out value='${user.fullName}' />"
                                   maxlength="120"
                                   required>
                        </td>

                        <td style="padding: 12px;">
                            <input form="update-user-${user.id}"
                                   type="email"
                                   name="email"
                                   aria-label="Email"
                                   value="<c:out value='${user.email}' />"
                                   maxlength="254"
                                   required>
                        </td>

                        <td style="padding: 12px;">
                            <select form="update-user-${user.id}"
                                    name="role"
                                    aria-label="Account role">
                                <option value="BUYER"
                                    <c:if test="${user.roleName eq 'BUYER'}">selected</c:if>>
                                    Buyer
                                </option>
                                <option value="SELLER"
                                    <c:if test="${user.roleName eq 'SELLER'}">selected</c:if>>
                                    Seller
                                </option>
                                <option value="ADMIN"
                                    <c:if test="${user.roleName eq 'ADMIN'}">selected</c:if>>
                                    Admin
                                </option>
                            </select>

                            <button class="button button-secondary"
                                    type="submit"
                                    form="update-user-${user.id}">
                                Save
                            </button>
                        </td>

                        <td style="padding: 12px;">
                            <c:choose>
                                <c:when test="${user.active}">
                                    <p>Active</p>

                                    <c:if test="${user.id ne currentAdminId}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/users">
                                            <input type="hidden"
                                                   name="csrfToken"
                                                   value="<c:out value='${csrfToken}' />">
                                            <input type="hidden"
                                                   name="userId"
                                                   value="${user.id}">
                                            <input type="hidden"
                                                   name="action"
                                                   value="deactivate">

                                            <button class="button button-danger"
                                                    type="submit">
                                                Deactivate
                                            </button>
                                        </form>
                                    </c:if>
                                </c:when>

                                <c:otherwise>
                                    <p>Inactive</p>

                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/users">
                                        <input type="hidden"
                                               name="csrfToken"
                                               value="<c:out value='${csrfToken}' />">
                                        <input type="hidden"
                                               name="userId"
                                               value="${user.id}">
                                        <input type="hidden"
                                               name="action"
                                               value="activate">

                                        <button class="button button-secondary"
                                                type="submit">
                                            Reactivate
                                        </button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</section>


<section class="panel">
    <div class="panel-heading">
        <div>
            <p class="eyebrow">ADMIN TOOLS</p>
            <h2>Management areas</h2>
        </div>
    </div>

    <div class="feature-row">

        <article class="feature-mini">
            <h3>User management</h3>
            <p>
                View registered accounts, update user details and roles,
                and activate or deactivate accounts.
            </p>
            <a class="button button-secondary"
               href="${pageContext.request.contextPath}/admin/users">
                Manage users
            </a>
        </article>

        <article class="feature-mini">
            <h3>Product management</h3>
            <p>
                Review product listings across sellers, edit product
                details, and activate or deactivate listings.
            </p>
            <a class="button button-secondary"
               href="${pageContext.request.contextPath}/admin/products">
                Manage products
            </a>
        </article>

        <article class="feature-mini">
            <h3>Order operations</h3>
                <p>
                    Review marketplace orders, update valid order statuses,
                    and preserve order history when accounts or listings
                    are deactivated.
                </p>

        <a class="button button-secondary"
        href="${pageContext.request.contextPath}/admin/orders">
            Manage orders
        </a>

        <a class="button button-secondary"
        href="${pageContext.request.contextPath}/admin/activity">
            View activity
        </a>

        </article>

    </div>
</section>

</main><footer class="site-footer"><span>MarketSquare · Admin</span><span>Protected, role-specific dashboard</span></footer>
</body></html>
