<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/home" aria-label="MarketSquare home">
        <span class="brand-mark">M</span>
        <span>MarketSquare</span>
    </a>
    <span class="header-note">Java E-commerce Project</span>
</header>

<main>
    <section class="hero">
        <p class="eyebrow">ONE MARKETPLACE · THREE WORKSPACES</p>
        <h1>Everything you need.<br><span>All in one marketplace.</span></h1>
        <p class="hero-copy">
            A role-based e-commerce platform where buyers discover products, sellers manage their stores,
            and administrators oversee the marketplace.
        </p>
        <div class="hero-actions">
            <a class="button button-primary" href="#workspaces">Explore the platform <span aria-hidden="true">→</span></a>
            <span class="build-status"><span class="status-dot"></span> Project foundation initialized</span>
        </div>
    </section>

    <section class="workspace-section" id="workspaces">
        <div class="section-heading">
            <div>
                <p class="eyebrow">PLATFORM OVERVIEW</p>
                <h2>Built around your role</h2>
            </div>
            <p>These workspaces will be connected to the database in the upcoming phases.</p>
        </div>

        <div class="workspace-grid">
            <article class="workspace-card buyer-card">
                <div class="card-icon" aria-hidden="true">↗</div>
                <p class="card-label">01 / CUSTOMER</p>
                <h3>Buyer workspace</h3>
                <p>Browse and search products, manage a wishlist, place orders, and track deliveries.</p>
                <span class="feature-tag">Planned feature set</span>
            </article>
            <article class="workspace-card seller-card">
                <div class="card-icon" aria-hidden="true">▦</div>
                <p class="card-label">02 / MERCHANT</p>
                <h3>Seller workspace</h3>
                <p>Create listings, update inventory, process orders, and review sales performance.</p>
                <span class="feature-tag">Planned feature set</span>
            </article>
            <article class="workspace-card admin-card">
                <div class="card-icon" aria-hidden="true">⌘</div>
                <p class="card-label">03 / CONTROL</p>
                <h3>Admin workspace</h3>
                <p>Manage user accounts, oversee products and orders, and review system activity.</p>
                <span class="feature-tag">Planned feature set</span>
            </article>
        </div>
    </section>

    <section class="architecture-strip">
        <div>
            <p class="eyebrow">TECH STACK</p>
            <h2>Java at the core</h2>
        </div>
        <div class="tech-list" aria-label="Planned technology stack">
            <span>Java 17+</span><span>Servlets</span><span>JSP</span><span>JDBC</span><span>MySQL</span><span>Tomcat 10.1</span>
        </div>
    </section>
</main>

<footer class="site-footer">
    <span>MarketSquare · Academic Java project</span>
    <span>Built in phases, tested at every step.</span>
</footer>
</body>
</html>
