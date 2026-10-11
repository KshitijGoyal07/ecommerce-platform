<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/application.css">
</head>
<body class="app-body">
<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/buyer/dashboard"><span class="brand-mark">M</span><span>MarketSquare</span></a>
    <nav class="header-user" aria-label="Buyer navigation">
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/cart">Cart</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/dashboard">Shop more</a>
        <form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form>
    </nav>
</header>
<main class="dashboard-layout orders-page">
    <section class="dashboard-intro"><p class="eyebrow">PURCHASES</p><h1 class="dashboard-title">Your <span>orders</span></h1><p class="muted-copy">Review previous purchases, item prices, delivery details, and order status. All amounts are in INR.</p></section>
    <c:if test="${not empty buyerCartMessage}"><div class="message message-success" role="status"><c:out value="${buyerCartMessage}" /></div></c:if>
    <c:if test="${not empty buyerCartError}"><div class="message message-error" role="alert"><c:out value="${buyerCartError}" /></div></c:if>
    <section class="panel orders-panel">
        <div class="panel-heading"><div><p class="eyebrow">ORDER HISTORY</p><h2>Previous purchases</h2></div><span class="count-chip"><c:out value="${orders.size()}" /> orders</span></div>
        <c:choose>
            <c:when test="${empty orders}">
                <div class="cart-empty-state"><div class="cart-empty-icon" aria-hidden="true">📦</div><h3>No orders yet</h3><p>Products you purchase will appear here with their status and INR totals.</p><a class="button button-primary" href="${pageContext.request.contextPath}/buyer/dashboard">Browse products <span aria-hidden="true">→</span></a></div>
            </c:when>
            <c:otherwise>
                <div class="order-list">
                    <c:forEach var="order" items="${orders}">
                        <article class="order-card">
                            <div class="order-card-heading">
                                <div class="order-heading-copy"><p class="eyebrow">ORDER #<c:out value="${order.id}" /></p><h3><c:out value="${order.status}" /></h3><p class="muted-copy order-date">Placed <c:out value="${order.createdAtDisplay}" /></p></div>
                                <div class="order-total-block"><span>Order total</span><strong class="order-total"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong></div>
                            </div>
                            
                            <div class="order-payment-summary">
                                <div class="order-payment-method">
                                    <span class="order-payment-label">PAYMENT METHOD</span>

                                    <strong>
                                        <c:choose>
                                            <c:when test="${order.paymentMethod eq 'COD'}">
                                                Cash on delivery
                                            </c:when>
                                            <c:when test="${order.paymentMethod eq 'UPI'}">
                                                UPI · Demo
                                            </c:when>
                                            <c:when test="${order.paymentMethod eq 'CARD'}">
                                                Card · Demo
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${order.paymentMethod}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>

                                <div class="order-payment-state">
                                    <span class="order-payment-label">PAYMENT STATUS</span>

                                    <c:choose>
                                        <c:when test="${order.paymentStatus eq 'SIMULATED'}">
                                            <span class="payment-status-pill payment-status-demo">
                                                Demo recorded
                                            </span>
                                        </c:when>

                                        <c:when test="${order.paymentStatus eq 'PAID'}">
                                            <span class="payment-status-pill payment-status-paid">
                                                Paid
                                            </span>
                                        </c:when>

                                        <c:when test="${order.paymentStatus eq 'FAILED'}">
                                            <span class="payment-status-pill payment-status-failed">
                                                Failed
                                            </span>
                                        </c:when>

                                        <c:when test="${order.paymentStatus eq 'REFUNDED'}">
                                            <span class="payment-status-pill">
                                                Refunded
                                            </span>
                                        </c:when>

                                        <c:otherwise>
                                            <span class="payment-status-pill payment-status-pending">
                                                Pending
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="order-lines">
                                <c:forEach var="item" items="${order.items}">
                                    <div class="checkout-item order-line-item">
                                        <div class="checkout-item-description"><strong><c:out value="${item.productName}" /></strong><span>Qty <c:out value="${item.quantity}" /> · <fmt:formatNumber value="${item.unitPrice}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /> each</span></div>
                                        <strong><fmt:formatNumber value="${item.lineTotal}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong>
                                    </div>
                                </c:forEach>
                            </div>
                            <div class="order-address"><strong>Delivery address</strong><p><c:out value="${order.shippingAddressSnapshot}" /></p></div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Buyer</span><span>Order history · INR totals</span></footer>
</body>
</html>
