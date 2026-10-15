
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Admin Product Management | MarketSquare</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/application.css">

    <style>
        .admin-products-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .admin-products-table {
            width: 100%;
            min-width: 1050px;
            border-collapse: collapse;
            margin-top: 20px;
        }

        .admin-products-table th,
        .admin-products-table td {
            padding: 14px 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid #343b52;
        }

        .admin-products-table th {
            font-size: 0.85rem;
            white-space: nowrap;
        }

        .product-edit-form {
            display: grid;
            gap: 10px;
            min-width: 230px;
        }

        .product-edit-form input,
        .product-edit-form textarea {
            box-sizing: border-box;
            width: 100%;
            padding: 9px;
            border: 1px solid #414a65;
            border-radius: 7px;
            font: inherit;
        }

        .product-edit-form textarea {
            min-height: 70px;
            resize: vertical;
        }

        .product-actions {
            display: grid;
            gap: 10px;
            min-width: 135px;
        }

        .product-actions form {
            margin: 0;
        }

        .product-message {
            padding: 12px 16px;
            margin: 14px 0;
            border-radius: 8px;
        }

        .product-success {
            border: 1px solid #398b68;
        }

        .product-error {
            border: 1px solid #c65b65;
        }

        .product-status {
            font-weight: 600;
            white-space: nowrap;
        }

        .product-description {
            min-width: 190px;
            max-width: 280px;
            overflow-wrap: anywhere;
        }

        .admin-navigation {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            align-items: center;
        }
    </style>
</head>

<body class="app-body">

<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/home">
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
            Product management<span>.</span>
        </h1>

        <p class="muted-copy">
            Welcome, <c:out value="${displayName}" />.
            Review marketplace listings and manage product details
            across sellers.
        </p>

        <div class="admin-navigation">
            <a class="button button-secondary"
               href="${pageContext.request.contextPath}/admin/dashboard">
                Admin dashboard
            </a>

            <a class="button button-secondary"
               href="${pageContext.request.contextPath}/admin/users">
                Manage users
            </a>
        </div>
    </section>

    <section class="panel">

        <div class="panel-heading">
            <div>
                <p class="eyebrow">MARKETPLACE CATALOGUE</p>
                <h2>All product listings</h2>
                <p class="muted-copy">
                    Edit listing details or temporarily deactivate
                    and reactivate products. Existing order history
                    is preserved.
                </p>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="product-message product-success" role="status">
                <c:out value="${successMessage}" />
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="product-message product-error" role="alert">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>
                
        <div class="panel-heading">
            <div>
                <p class="eyebrow">NEW LISTING</p>
                <h2>Create product</h2>
                <p class="muted-copy">
                    Enter an existing active Seller ID to assign the listing.
                </p>
            </div>
        </div>

        <form method="post"
            action="${pageContext.request.contextPath}/admin/products"
            style="display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:14px; margin:16px 0 28px;">

            <input type="hidden"
                name="csrfToken"
                value="<c:out value='${csrfToken}' />">

            <input type="hidden" name="action" value="create">

            <label>
                Seller ID
                <input type="number" name="sellerId"
                    min="1" step="1" required>
            </label>

            <label>
                Product name
                <input type="text" name="name"
                    maxlength="150" required>
            </label>

            <label>
                Category
                <input type="text" name="category"
                    maxlength="100" required>
            </label>

            <label>
                Price (₹)
                <input type="number" name="price"
                    min="0.01" step="0.01" required>
            </label>

            <label>
                Initial stock
                <input type="number" name="stockQuantity"
                    min="0" step="1" required>
            </label>

            <label style="grid-column:1/-1;">
                Description
                <textarea name="description"
                        maxlength="5000"></textarea>
            </label>

            <div>
                <button class="button button-secondary" type="submit">
                    Create product
                </button>
            </div>
        </form>

        <div class="admin-products-wrap">
            <table class="admin-products-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Seller ID</th>
                        <th>Product name</th>
                        <th>Category</th>
                        <th>Price (₹)</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th>Edit details</th>
                        <th>Listing actions</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="product" items="${products}">
                        <tr>
                            <td>
                                <c:out value="${product.id}" />
                            </td>

                            <td>
                                <c:out value="${product.sellerId}" />
                            </td>

                            <td>
                                <c:out value="${product.name}" />
                            </td>

                            <td>
                                <c:out value="${product.category}" />
                            </td>

                            <td>
                                ₹<c:out value="${product.price}" />
                            </td>

                            <td>
                                <c:out value="${product.stockQuantity}" />
                            </td>

                            <td class="product-status">
                                <c:choose>
                                    <c:when test="${product.active}">
                                        Active
                                    </c:when>
                                    <c:otherwise>
                                        Inactive
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <form class="product-edit-form"
                                      method="post"
                                      action="${pageContext.request.contextPath}/admin/products">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="<c:out value='${csrfToken}' />">

                                    <input type="hidden"
                                           name="action"
                                           value="update">

                                    <input type="hidden"
                                           name="productId"
                                           value="<c:out value='${product.id}' />">

                                    <label>
                                        Product name
                                        <input type="text"
                                               name="name"
                                               value="<c:out value='${product.name}' />"
                                               maxlength="150"
                                               required>
                                    </label>

                                    <label>
                                        Category
                                        <input type="text"
                                               name="category"
                                               value="<c:out value='${product.category}' />"
                                               maxlength="100"
                                               required>
                                    </label>

                                    <label>
                                        Price (₹)
                                        <input type="number"
                                               name="price"
                                               value="<c:out value='${product.price}' />"
                                               min="0.01"
                                               step="0.01"
                                               required>
                                    </label>

                                    <label>
                                        Description
                                        <textarea name="description"
                                                  maxlength="5000"><c:out value="${product.description}" /></textarea>
                                    </label>

                                    <button class="button button-secondary"
                                            type="submit">
                                        Save changes
                                    </button>
                                </form>
                            </td>

                            <td>
                                <div class="product-actions">

                                    <c:choose>
                                        <c:when test="${product.active}">
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/products">

                                                <input type="hidden"
                                                       name="csrfToken"
                                                       value="<c:out value='${csrfToken}' />">

                                                <input type="hidden"
                                                       name="productId"
                                                       value="<c:out value='${product.id}' />">

                                                <input type="hidden"
                                                       name="action"
                                                       value="deactivate">

                                                <button class="button button-secondary"
                                                        type="submit">
                                                    Deactivate
                                                </button>
                                            </form>
                                        </c:when>

                                        <c:otherwise>
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/products">

                                                <input type="hidden"
                                                       name="csrfToken"
                                                       value="<c:out value='${csrfToken}' />">

                                                <input type="hidden"
                                                       name="productId"
                                                       value="<c:out value='${product.id}' />">

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

                                </div>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty products}">
                        <tr>
                            <td colspan="9">
                                No product listings were found.
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
    <span>Marketplace product administration</span>
</footer>

</body>
</html>
