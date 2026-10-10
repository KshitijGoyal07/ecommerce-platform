<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${pageTitle}" /></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/application.css">
</head><body class="app-body">
<header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a><div class="header-user"><span>Buyer workspace</span><form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form></div></header>
<main class="dashboard-layout">
    <section class="dashboard-intro"><p class="eyebrow">BUYER WORKSPACE</p><h1 class="dashboard-title">Welcome, <span><c:out value="${displayName}" /></span></h1><p class="muted-copy">Explore the active products currently listed on MarketSquare.</p></section>
    <section class="panel"><div class="panel-heading"><div><p class="eyebrow">MARKETPLACE</p><h2>Available products</h2></div><span class="count-chip"><c:out value="${productCount}" /> shown</span></div>
    <c:choose><c:when test="${empty products}"><p class="empty-state">No active products are listed yet. Seller listings will appear here once added.</p></c:when><c:otherwise><div class="product-grid"><c:forEach var="product" items="${products}"><article class="product-card"><div class="product-category"><c:out value="${product.category}" /></div><h3><c:out value="${product.name}" /></h3><p class="product-description"><c:out value="${product.description}" /></p><div class="product-meta"><strong>₹ <c:out value="${product.price}" /></strong><span><c:out value="${product.stockQuantity}" /> in stock</span></div></article></c:forEach></div></c:otherwise></c:choose></section>
    <section class="feature-row"><article class="feature-mini"><p class="eyebrow">NEXT FEATURE</p><h3>Wishlist</h3><p>Save favourite products in the next development checkpoint.</p></article><article class="feature-mini"><p class="eyebrow">NEXT FEATURE</p><h3>Orders</h3><p>Checkout and order tracking will be connected after the catalogue.</p></article></section>
</main><footer class="site-footer"><span>MarketSquare · Buyer</span><span>Database-backed product catalogue</span></footer>
</body></html>
