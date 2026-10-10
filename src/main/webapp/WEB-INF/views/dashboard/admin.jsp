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
    <section class="panel"><div class="panel-heading"><div><p class="eyebrow">ADMIN TOOLS</p><h2>Management areas</h2></div></div><div class="feature-row"><article class="feature-mini"><h3>User management</h3><p>Account review and deactivation operations are available in the user DAO; the admin interface will be added next.</p></article><article class="feature-mini"><h3>Product management</h3><p>Product persistence is connected; the listing management interface is coming next.</p></article><article class="feature-mini"><h3>Order operations</h3><p>Transactional order persistence is in place; order overview UI is planned for the next checkpoint.</p></article></div></section>
</main><footer class="site-footer"><span>MarketSquare · Admin</span><span>Protected, role-specific dashboard</span></footer>
</body></html>
