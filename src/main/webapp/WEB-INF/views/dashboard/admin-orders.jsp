
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Order Management | MarketSquare</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/application.css">

    <style>
        .orders-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .orders-table {
            width: 100%;
            min-width: 1000px;
            border-collapse: collapse;
            margin-top: 20px;
        }

        .orders-table th,
        .orders-table td {
            padding: 14px 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid #343b52;
        }

        .orders-table th {
            font-size: 0.85rem;
            white-space: nowrap;
        }

        .order-update-form {
            display: grid;
            gap: 10px;
            min-width: 150px;
        }

        .order-update-form select {
            box-sizing: border-box;
            width: 100%;
            padding: 9px;
            border: 1px solid #414a65;
            border-radius: 7px;
            font: inherit;
        }

        .order-address {
            min-width: 180px;
            max-width: 260px;
            overflow-wrap: anywhere;
        }

        .order-message {
            padding: 12px 16px;
            margin: 14px 0;
            border-radius: 8px;
        }

        .order-success {
            border: 1px solid #398b68;
        }

        .order-error {
            border: 1px solid #c65b65;
        }

        .order-status {
            font-weight: 600;
            white-space: nowrap;
        }

        .order-payment {
            white-space: nowrap;
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
        <h1 class="dashboard-title">Order management<span>.</span></h1>

        <p class="muted-copy">
            Welcome, <c:out value="${displayName}" />.
            Review all marketplace orders, payment information, and
            shipping details. Order status changes follow the order lifecycle.
        </p>

        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/admin/dashboard">
            Admin dashboard
        </a>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <div>
                <p class="eyebrow">MARKETPLACE OPERATIONS</p>
                <h2>All orders</h2>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="order-message order-success" role="status">
                <c:out value="${successMessage}" />
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="order-message order-error" role="alert">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <div class="orders-table-wrap">
            <table class="orders-table">
                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Buyer ID</th>
                        <th>Placed</th>
                        <th>Total</th>
                        <th>Payment</th>
                        <th>Shipping address</th>
                        <th>Status</th>
                        <th>Update status</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="order" items="${orders}">
                        <tr>
                            <td>
                                #<c:out value="${order.id}" />
                            </td>

                            <td>
                                <c:out value="${order.buyerId}" />
                            </td>

                            <td>
                                <c:out value="${order.createdAtDisplay}" />
                            </td>

                            <td>
                                ₹<c:out value="${order.totalAmount}" />
                            </td>

                            <td class="order-payment">
                                <strong>
                                    <c:out value="${order.paymentMethod}" />
                                </strong>
                                <br>
                                <span>
                                    <c:out value="${order.paymentStatus}" />
                                </span>
                            </td>

                            <td class="order-address">
                                <details>
                                    <summary>View address</summary>
                                    <p>
                                        <c:out value="${order.shippingAddress}" />
                                    </p>
                                </details>
                            </td>

                            <td class="order-status">
                                <c:out value="${order.status}" />
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${not empty order.nextStatuses}">
                                        <form class="order-update-form"
                                              method="post"
                                              action="${pageContext.request.contextPath}/admin/orders">

                                            <input type="hidden"
                                                   name="csrfToken"
                                                   value="<c:out value='${csrfToken}' />">

                                            <input type="hidden"
                                                   name="orderId"
                                                   value="<c:out value='${order.id}' />">

                                            <label>
                                                Next status
                                                <select name="nextStatus" required>
                                                    <option value="" selected disabled>
                                                        Select status
                                                    </option>

                                                    <c:forEach var="nextStatus"
                                                               items="${order.nextStatuses}">
                                                        <option value="<c:out value='${nextStatus}' />">
                                                            <c:out value="${nextStatus}" />
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </label>

                                            <button class="button button-secondary"
                                                    type="submit">
                                                Update order
                                            </button>
                                        </form>
                                    </c:when>

                                    <c:otherwise>
                                        No further status changes
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty orders}">
                        <tr>
                            <td colspan="8">No orders found.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </section>

</main>

<footer class="site-footer">
    <span>MarketSquare · Admin</span>
    <span>Marketplace order administration</span>
</footer>

</body>
</html>
