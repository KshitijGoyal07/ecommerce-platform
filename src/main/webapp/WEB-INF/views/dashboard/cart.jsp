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
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/dashboard">Continue shopping</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/orders">My orders</a>
        <form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form>
    </nav>
</header>
<main class="dashboard-layout cart-page">
    <section class="dashboard-intro">
        <p class="eyebrow">YOUR BASKET</p>
        <h1 class="dashboard-title">Shopping <span>cart</span></h1>
        <p class="muted-copy">Adjust quantities, remove items, and review your total before checkout. All prices are in Indian Rupees (INR).</p>
    </section>
    <c:if test="${not empty buyerCartMessage}"><div class="message message-success" role="status"><c:out value="${buyerCartMessage}" /></div></c:if>
    <c:if test="${not empty buyerCartError}"><div class="message message-error" role="alert"><c:out value="${buyerCartError}" /></div></c:if>
    <section class="panel cart-panel">
        <div class="panel-heading">
            <div><p class="eyebrow">CART ITEMS</p><h2>Your selection</h2><p class="panel-subtitle">Your cart is saved for this browser session.</p></div>
            <span class="count-chip"><c:out value="${cartQuantity}" /> units</span>
        </div>
        <c:choose>
            <c:when test="${empty cartLines}">
                <div class="cart-empty-state"><div class="cart-empty-icon" aria-hidden="true">🛍</div><h3>Your cart is waiting for you</h3><p>Browse the marketplace and add products to get started.</p><a class="button button-primary" href="${pageContext.request.contextPath}/buyer/dashboard">Browse products <span aria-hidden="true">→</span></a></div>
            </c:when>
            <c:otherwise>
                <div class="cart-list">
                    <c:forEach var="line" items="${cartLines}">
                        <article class="cart-line">
                            <div class="cart-line-main">
                                <p class="product-category"><c:out value="${line.product.category}" /></p>
                                <h3><c:out value="${line.product.name}" /></h3>
                                <p class="cart-unit-price"><fmt:formatNumber value="${line.product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /> <span>per item</span></p>
                                <p class="cart-stock-note"><span class="stock-status-dot" aria-hidden="true"></span><c:out value="${line.product.stockQuantity}" /> currently available</p>
                            </div>
                            <div class="cart-line-side">
                                <div class="cart-line-price"><span>Line total</span><strong><fmt:formatNumber value="${line.lineTotal}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong></div>
                                <form method="post" action="${pageContext.request.contextPath}/buyer/cart" class="cart-quantity-form">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="productId" value="<c:out value='${line.product.id}' />">
                                    <div class="quantity-field">
                                        <label for="qty-${line.product.id}">Quantity</label>
                                        <div class="quantity-control">
                                            <button type="button" data-quantity-step="-1" aria-label="Decrease quantity">−</button>
                                            <input id="qty-${line.product.id}" class="quantity-input" name="quantity" type="number" min="1" max="${line.product.stockQuantity < 99 ? line.product.stockQuantity : 99}" value="<c:out value='${line.quantity}' />" required>
                                            <button type="button" data-quantity-step="1" aria-label="Increase quantity">+</button>
                                        </div>
                                    </div>
                                    <button class="button button-secondary update-cart-button" type="submit">Update quantity</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/buyer/cart" class="cart-remove-form">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="productId" value="<c:out value='${line.product.id}' />">
                                    <button class="button button-danger" type="submit">Remove item</button>
                                </form>
                            </div>
                        </article>
                    </c:forEach>
                </div>
                <aside class="cart-summary-card" aria-label="Cart summary">
                    <div><span>Subtotal</span><p>Stock and prices are revalidated when you place the order.</p></div>
                    <strong><fmt:formatNumber value="${subtotal}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong>
                </aside>
                <p class="small-note cart-note">This academic checkout does not process real payments. Your stock is reserved only after the order is successfully placed.</p>
                <div class="cart-actions">
                    <form method="post" action="${pageContext.request.contextPath}/buyer/cart" class="clear-cart-form">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                        <input type="hidden" name="action" value="clear">
                        <button class="button button-secondary" type="submit">Clear cart</button>
                    </form>
                    <a class="button button-primary checkout-link" href="${pageContext.request.contextPath}/buyer/checkout">Continue to checkout <span aria-hidden="true">→</span></a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Buyer</span><span>Session cart · INR pricing</span></footer>
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
