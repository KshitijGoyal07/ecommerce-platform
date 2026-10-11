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
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/cart">Cart (<c:out value="${cartQuantity}" />)</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/orders">My orders</a>
        <form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form>
    </nav>
</header>
<main class="dashboard-layout">
    <section class="dashboard-intro"><p class="eyebrow">SAVED FOR LATER</p><h1 class="dashboard-title">Your <span>wishlist</span></h1><p class="muted-copy">Keep products you like in one place. Saved items are linked to your buyer account.</p></section>
    <c:if test="${not empty wishlistMessage}"><div class="message message-success" role="status"><c:out value="${wishlistMessage}" /></div></c:if>
    <c:if test="${not empty wishlistError}"><div class="message message-error" role="alert"><c:out value="${wishlistError}" /></div></c:if>
    <section class="panel">
        <div class="panel-heading"><div><p class="eyebrow">YOUR SAVED PRODUCTS</p><h2>Wishlist items</h2></div><span class="count-chip"><c:out value="${wishlistProducts.size()}" /> saved</span></div>
        <c:choose>
            <c:when test="${empty wishlistProducts}"><p class="empty-state">Your wishlist is empty. Browse the marketplace and save products to see them here.</p><p><a class="button button-primary" href="${pageContext.request.contextPath}/buyer/dashboard">Explore products →</a></p></c:when>
            <c:otherwise>
                <div class="product-grid">
                    <c:forEach var="product" items="${wishlistProducts}">
                        <article class="product-card">
                            <div class="product-category"><c:out value="${product.category}" /></div>
                            <h3><c:out value="${product.name}" /></h3>
                            <p class="product-description"><c:out value="${product.description}" /></p>
                            <div class="product-meta"><strong><fmt:formatNumber value="${product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong><span><c:out value="${product.stockQuantity}" /> in stock</span></div>
                            <c:choose><c:when test="${product.active}"><p><a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/product?id=${product.id}">View details</a></p></c:when><c:otherwise><p class="status-pill status-inactive">Currently unavailable</p></c:otherwise></c:choose>
                            <c:if test="${product.active and product.stockQuantity > 0}">
                                <form method="post" action="${pageContext.request.contextPath}/buyer/cart" class="add-to-cart-form">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="add"><input type="hidden" name="productId" value="<c:out value='${product.id}' />">
                                    <label class="quantity-label" for="wishlist-qty-${product.id}">Quantity</label>
                                    <div class="quantity-control quantity-control-small"><button type="button" data-quantity-step="-1" aria-label="Decrease quantity">−</button><input id="wishlist-qty-${product.id}" class="quantity-input" name="quantity" type="number" min="1" max="${product.stockQuantity < 99 ? product.stockQuantity : 99}" value="1" required><button type="button" data-quantity-step="1" aria-label="Increase quantity">+</button></div>
                                    <button class="button button-primary" type="submit">Add to cart</button>
                                </form>
                            </c:if>
                            <form method="post" action="${pageContext.request.contextPath}/buyer/wishlist">
                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="remove"><input type="hidden" name="productId" value="<c:out value='${product.id}' />"><input type="hidden" name="returnTo" value="wishlist">
                                <button class="button button-secondary" type="submit">Remove from wishlist</button>
                            </form>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Buyer</span><span>Your saved products · INR pricing</span></footer>
<script>
(() => { document.addEventListener('click', (event) => { const button = event.target.closest('[data-quantity-step]'); if (!button) return; const control = button.closest('.quantity-control'); const input = control && control.querySelector('input[type="number"]'); if (!input) return; const min = Number(input.min || 1); const max = Number(input.max || 99); input.value = String(Math.min(max, Math.max(min, Number(input.value || min) + Number(button.dataset.quantityStep || 0)))); }); })();
</script>
</body>
</html>
