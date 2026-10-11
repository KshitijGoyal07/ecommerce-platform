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
    <a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a>
    <nav class="header-user" aria-label="Buyer navigation">
        <span>Buyer workspace</span>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/cart">Cart (<c:out value="${cartQuantity}" />)</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/orders">My orders</a>
        <form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form>
    </nav>
</header>
<main class="dashboard-layout">
    <section class="dashboard-intro">
        <p class="eyebrow">BUYER WORKSPACE</p>
        <h1 class="dashboard-title">Welcome, <span><c:out value="${displayName}" /></span></h1>
        <p class="muted-copy">Explore the active products listed on MarketSquare. Choose your quantity before adding an item to your cart.</p>
    </section>
    <section class="panel">
        <div class="panel-heading">
            <div><p class="eyebrow">MARKETPLACE</p><h2>Available products</h2></div>
            <span class="count-chip"><c:out value="${productCount}" /> shown</span>
        </div>
        <c:choose>
            <c:when test="${empty products}">
                <p class="empty-state">No active products are listed yet. Seller listings will appear here once added.</p>
            </c:when>
            <c:otherwise>
                <div class="product-grid">
                    <c:forEach var="product" items="${products}">
                        <article class="product-card">
                            <div class="product-category"><c:out value="${product.category}" /></div>
                            <h3><c:out value="${product.name}" /></h3>
                            <p class="product-description"><c:out value="${product.description}" /></p>
                            <div class="product-meta">
                                <strong><fmt:formatNumber value="${product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong>
                                <span><c:out value="${product.stockQuantity}" /> in stock</span>
                            </div>
                            <c:choose>
                                <c:when test="${product.stockQuantity > 0}">
                                    <form method="post" action="${pageContext.request.contextPath}/buyer/cart" class="add-to-cart-form">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                        <input type="hidden" name="action" value="add">
                                        <input type="hidden" name="productId" value="<c:out value='${product.id}' />">
                                        <label class="quantity-label" for="product-qty-${product.id}">Quantity</label>
                                        <div class="quantity-control quantity-control-small">
                                            <button type="button" data-quantity-step="-1" aria-label="Decrease quantity">−</button>
                                            <input id="product-qty-${product.id}" class="quantity-input" name="quantity" type="number" min="1" max="${product.stockQuantity < 99 ? product.stockQuantity : 99}" value="1" required>
                                            <button type="button" data-quantity-step="1" aria-label="Increase quantity">+</button>
                                        </div>
                                        <button class="button button-primary add-cart-button" type="submit">Add to cart <span aria-hidden="true">→</span></button>
                                    </form>
                                </c:when>
                                <c:otherwise><span class="status-pill status-inactive">Out of stock</span></c:otherwise>
                            </c:choose>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
    <section class="feature-row">
        <article class="feature-mini"><p class="eyebrow">SHOPPING</p><h3>Your cart</h3><p>Review quantities and continue to checkout with INR totals.</p><p><a href="${pageContext.request.contextPath}/buyer/cart">Open shopping cart →</a></p></article>
        <article class="feature-mini"><p class="eyebrow">PURCHASES</p><h3>Order history</h3><p>Review previous orders, item prices, and order status.</p><p><a href="${pageContext.request.contextPath}/buyer/orders">View my orders →</a></p></article>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Buyer</span><span>Database-backed product catalogue · INR pricing</span></footer>
<script>
(() => {
  document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-quantity-step]');
    if (!button) return;
    const control = button.closest('.quantity-control');
    const input = control && control.querySelector('input[type="number"]');
    if (!input) return;
    const min = Number(input.min || 1);
    const max = Number(input.max || 99);
    const current = Number(input.value || min);
    const step = Number(button.dataset.quantityStep || 0);
    input.value = String(Math.min(max, Math.max(min, current + step)));
    input.dispatchEvent(new Event('change', { bubbles: true }));
  });
})();
</script>
</body>
</html>
