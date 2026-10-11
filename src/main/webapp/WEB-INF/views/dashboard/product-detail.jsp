<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/application.css">
</head>
<body class="app-body">
<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a>
    <nav class="header-user" aria-label="Buyer navigation">
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/dashboard">Shop more</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/wishlist">Wishlist</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/cart">Cart (<c:out value="${cartQuantity}" />)</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/orders">My orders</a>
    </nav>
</header>
<main class="dashboard-layout">
    <p><a href="${pageContext.request.contextPath}/buyer/dashboard">← Back to marketplace</a></p>
    <section class="panel product-detail-panel">
        <p class="eyebrow"><c:out value="${product.category}" /></p>
        <h1 class="dashboard-title"><c:out value="${product.name}" /></h1>
        <p class="muted-copy"><c:out value="${product.description}" /></p>
        <div class="product-meta"><strong><fmt:formatNumber value="${product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong><span><c:out value="${product.stockQuantity}" /> in stock</span></div>
        <c:choose>
            <c:when test="${product.stockQuantity > 0}">
                <form method="post" action="${pageContext.request.contextPath}/buyer/cart" class="add-to-cart-form">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="add"><input type="hidden" name="productId" value="<c:out value='${product.id}' />">
                    <label class="quantity-label" for="detail-quantity">Quantity</label>
                    <div class="quantity-control quantity-control-small"><button type="button" data-quantity-step="-1" aria-label="Decrease quantity">−</button><input id="detail-quantity" class="quantity-input" name="quantity" type="number" min="1" max="${product.stockQuantity < 99 ? product.stockQuantity : 99}" value="1" required><button type="button" data-quantity-step="1" aria-label="Increase quantity">+</button></div>
                    <button class="button button-primary" type="submit">Add to cart</button>
                </form>
            </c:when>
            <c:otherwise><span class="status-pill status-inactive">Out of stock</span></c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${isWishlisted}"><p>This product is already in your wishlist.</p><p><a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/wishlist">View wishlist</a></p></c:when>
            <c:otherwise><form method="post" action="${pageContext.request.contextPath}/buyer/wishlist"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="add"><input type="hidden" name="productId" value="<c:out value='${product.id}' />"><input type="hidden" name="returnTo" value="product-${product.id}"><button class="button button-secondary" type="submit">♡ Save to wishlist</button></form></c:otherwise>
        </c:choose>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Buyer</span><span>Product details · INR pricing</span></footer>
<script>
(() => { document.addEventListener('click', (event) => { const button = event.target.closest('[data-quantity-step]'); if (!button) return; const control = button.closest('.quantity-control'); const input = control && control.querySelector('input[type="number"]'); if (!input) return; const min = Number(input.min || 1); const max = Number(input.max || 99); input.value = String(Math.min(max, Math.max(min, Number(input.value || min) + Number(button.dataset.quantityStep || 0)))); }); })();
</script>
</body>
</html>
