<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${pageTitle}" /></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/application.css">
</head><body class="app-body">
<header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a><div class="header-user"><span>Seller workspace</span><form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form></div></header>
<main class="dashboard-layout">
    <section class="dashboard-intro"><p class="eyebrow">SELLER WORKSPACE</p><h1 class="dashboard-title">Your store, <span><c:out value="${displayName}" /></span></h1><p class="muted-copy">Review product listings owned by your seller account.</p></section>
    <section class="panel"><div class="panel-heading"><div><p class="eyebrow">INVENTORY</p><h2>Your product listings</h2></div><span class="count-chip"><c:out value="${productCount}" /> listed</span></div>
    <c:choose><c:when test="${empty products}"><p class="empty-state">You have no product listings yet. Product creation and editing are coming in the next checkpoint.</p></c:when><c:otherwise><div class="table-wrap"><table class="data-table"><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th></tr></thead><tbody><c:forEach var="product" items="${products}"><tr><td><c:out value="${product.name}" /></td><td><c:out value="${product.category}" /></td><td>₹ <c:out value="${product.price}" /></td><td><c:out value="${product.stockQuantity}" /></td><td><c:choose><c:when test="${product.active}">Active</c:when><c:otherwise>Inactive</c:otherwise></c:choose></td></tr></c:forEach></tbody></table></div></c:otherwise></c:choose></section>
    <p class="small-note">Only products linked to your seller account are shown.</p>
</main><footer class="site-footer"><span>MarketSquare · Seller</span><span>Inventory overview</span></footer>
</body></html>
