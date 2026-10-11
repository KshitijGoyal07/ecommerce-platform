<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN" />
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${pageTitle}" /></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/application.css">
</head><body class="app-body">
<header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a><div class="header-user"><span>Seller workspace</span><form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form></div></header>
<main class="dashboard-layout">
    <section class="dashboard-intro"><p class="eyebrow">SELLER WORKSPACE</p><h1 class="dashboard-title">Your store, <span><c:out value="${displayName}" /></span></h1><p class="muted-copy">Create listings, update prices in INR, and maintain your inventory.</p></section>

    <c:if test="${not empty sellerSuccess}"><div class="message message-success" role="status"><c:out value="${sellerSuccess}" /></div></c:if>
    <c:if test="${not empty sellerError}"><div class="message message-error" role="alert"><c:out value="${sellerError}" /></div></c:if>

    <section class="panel">
        <div class="panel-heading"><div><p class="eyebrow">NEW LISTING</p><h2>Add a product</h2></div><span class="count-chip">Prices in INR (₹)</span></div>
        <form method="post" action="${pageContext.request.contextPath}/seller/products" class="app-form seller-create-form">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
            <input type="hidden" name="action" value="create">
            <div class="seller-form-tools">
                <button class="button button-secondary" type="button" id="fillRandomProduct">Fill random test details</button>
                <span class="seller-form-hint" id="sampleProductMessage" role="status">Fills the fields with sample product data; it will not submit the form.</span>
            </div>
            <div class="seller-form-grid">
                <div><label for="newName">Product name</label><input id="newName" name="name" type="text" maxlength="180" required placeholder="e.g. Wireless headphones"></div>
                <div><label for="newCategory">Category</label><input id="newCategory" name="category" type="text" maxlength="100" required placeholder="e.g. Electronics"></div>
                <div><label for="newPrice">Price (₹ INR)</label><input id="newPrice" name="price" type="number" min="0.01" max="9999999999.99" step="0.01" required placeholder="1299.00"></div>
                <div><label for="newStock">Initial stock quantity</label><input id="newStock" name="stockQuantity" type="number" min="0" max="2147483647" step="1" required value="1"></div>
            </div>
            <label for="newDescription">Description</label><textarea id="newDescription" name="description" rows="3" maxlength="5000" placeholder="Describe the product for buyers"></textarea>
            <button class="button button-primary form-submit" type="submit">Create product listing <span aria-hidden="true">→</span></button>
        </form>
    </section>

    <section class="panel">
        <div class="panel-heading"><div><p class="eyebrow">INVENTORY</p><h2>Your product listings</h2></div><span class="count-chip"><c:out value="${productCount}" /> listed</span></div>
        <c:choose>
            <c:when test="${empty products}"><p class="empty-state">You have no product listings yet. Use the form above to create your first listing.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table class="data-table seller-products-table"><thead><tr><th>Product</th><th>Category</th><th>Price (INR)</th><th>Stock</th><th>Status</th><th>Manage</th></tr></thead><tbody>
                <c:forEach var="product" items="${products}">
                    <tr>
                        <td><strong><c:out value="${product.name}" /></strong><c:if test="${not empty product.description}"><div class="seller-product-description"><c:out value="${product.description}" /></div></c:if></td>
                        <td><c:out value="${product.category}" /></td>
                        <td class="price-cell"><fmt:formatNumber value="${product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></td>
                        <td><c:out value="${product.stockQuantity}" /></td>
                        <td><c:choose><c:when test="${product.active}"><span class="status-pill status-active">Active</span></c:when><c:otherwise><span class="status-pill status-inactive">Inactive</span></c:otherwise></c:choose></td>
                        <td class="seller-actions-cell">
                            <details class="listing-edit"><summary>Edit listing</summary>
                                <form method="post" action="${pageContext.request.contextPath}/seller/products" class="app-form compact-form">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="update"><input type="hidden" name="productId" value="<c:out value='${product.id}' />">
                                    <label>Product name<input name="name" type="text" maxlength="180" required value="<c:out value='${product.name}' />"></label>
                                    <label>Category<input name="category" type="text" maxlength="100" required value="<c:out value='${product.category}' />"></label>
                                    <label>Price (₹ INR)<input name="price" type="number" min="0.01" max="9999999999.99" step="0.01" required value="<c:out value='${product.price}' />"></label>
                                    <label>Description<textarea name="description" rows="3" maxlength="5000"><c:out value="${product.description}" /></textarea></label>
                                    <button class="button button-primary" type="submit">Save details</button>
                                </form>
                            </details>
                            <form method="post" action="${pageContext.request.contextPath}/seller/products" class="stock-form">
                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="stock"><input type="hidden" name="productId" value="<c:out value='${product.id}' />">
                                <label for="stock-${product.id}">Set stock quantity</label><div class="stock-input-row"><input id="stock-${product.id}" name="stockQuantity" type="number" min="0" max="2147483647" step="1" required value="<c:out value='${product.stockQuantity}' />"><button class="button button-secondary" type="submit">Update</button></div>
                            </form>
                            <c:if test="${product.active}"><form method="post" action="${pageContext.request.contextPath}/seller/products" class="deactivate-form" onsubmit="return confirm('Hide this product from buyers? Existing order records will be preserved.');"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="action" value="deactivate"><input type="hidden" name="productId" value="<c:out value='${product.id}' />"><button class="button button-danger" type="submit">Deactivate</button></form></c:if>
                            <c:if test="${not product.active}">
                                <form method="post"
                                    action="${pageContext.request.contextPath}/seller/products"
                                    class="reactivate-form">
                                    <input type="hidden" name="csrfToken"
                                        value="<c:out value='${csrfToken}' />"/>
                                    <input type="hidden" name="action" value="activate"/>
                                    <input type="hidden" name="productId"
                                        value="<c:out value='${product.id}' />"/>
                                    <button class="button button-secondary" type="submit">
                                        Reactivate
                                    </button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody></table></div>
            </c:otherwise>
        </c:choose>
    </section>
    <c:if test="${not empty sellerReportError}">
        <section class="panel">
            <div class="message message-error" role="alert">
                <c:out value="${sellerReportError}" />
            </div>
        </section>
    </c:if>

    <c:if test="${empty sellerReportError}">
        <!-- SELLER SALES SUMMARY -->
        <section class="panel seller-sales-panel">
            <div class="panel-heading">
                <div>
                    <p class="eyebrow">BUSINESS OVERVIEW</p>
                    <h2>Sales summary</h2>
                </div>
                <span class="count-chip">INR (₹)</span>
            </div>

            <div class="seller-sales-grid">
                <article class="seller-sales-card">
                    <p class="eyebrow">ORDERS</p>
                    <p class="seller-sales-value">
                        <c:out value="${salesSummary.orderCount}" />
                    </p>
                    <p class="muted-copy">Non-cancelled orders</p>
                </article>

                <article class="seller-sales-card">
                    <p class="eyebrow">UNITS SOLD</p>
                    <p class="seller-sales-value">
                        <c:out value="${salesSummary.unitsSold}" />
                    </p>
                    <p class="muted-copy">Across your product listings</p>
                </article>

                <article class="seller-sales-card">
                    <p class="eyebrow">SALES TOTAL</p>
                    <p class="seller-sales-value">
                        <fmt:formatNumber
                            value="${salesSummary.salesTotal}"
                            type="currency"
                            currencyCode="INR"
                            minFractionDigits="2"
                            maxFractionDigits="2" />
                    </p>
                    <p class="muted-copy">Excludes cancelled orders</p>
                </article>
            </div>
        </section>

        <!-- SELLER ORDER HISTORY -->
        <section class="panel seller-orders-panel">
            <div class="panel-heading">
                <div>
                    <p class="eyebrow">CUSTOMER PURCHASES</p>
                    <h2>Recent orders</h2>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty sellerOrders}">
                    <p class="empty-state">
                        No orders containing your products have been placed yet.
                    </p>
                </c:when>

                <c:otherwise>
                    <div class="table-wrap">
                        <table class="data-table seller-orders-table">
                            <thead>
                                <tr>
                                    <th>Order</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Your products</th>
                                    <th>Seller subtotal</th>
                                </tr>
                            </thead>

                            <tbody>
                                <c:forEach var="order" items="${sellerOrders}">
                                    <tr>
                                        <td>
                                            <strong>
                                                #<c:out value="${order.orderId}" />
                                            </strong>
                                        </td>

                                        <td>
                                            <c:out value="${order.createdAtDisplay}" />
                                        </td>

                                        <td>
                                            <span class="status-pill">
                                                <c:out value="${order.status}" />
                                            </span>
                                        </td>

                                        <td>
                                            <div class="seller-order-items">
                                                <c:forEach
                                                    var="item"
                                                    items="${order.items}">
                                                    <div class="seller-order-item">
                                                        <strong>
                                                            <c:out value="${item.productName}" />
                                                        </strong>

                                                        <span class="seller-order-item-meta">
                                                            Qty:
                                                            <c:out value="${item.quantity}" />
                                                            &middot;
                                                            <fmt:formatNumber
                                                                value="${item.unitPrice}"
                                                                type="currency"
                                                                currencyCode="INR"
                                                                minFractionDigits="2"
                                                                maxFractionDigits="2" />
                                                            each
                                                        </span>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </td>

                                        <td class="price-cell">
                                            <strong>
                                                <fmt:formatNumber
                                                    value="${order.subtotal}"
                                                    type="currency"
                                                    currencyCode="INR"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" />
                                            </strong>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <p class="muted-copy">
                Order totals include only products sold by your store.
                Buyer delivery addresses and other sellers' items are not displayed.
            </p>
        </section>
    </c:if>
    <p class="small-note">A listing can be edited or deactivated only by the seller who owns it. Deactivation hides it from buyers without deleting historical order references. All prices are Indian Rupees (INR).</p>
</main><footer class="site-footer"><span>MarketSquare · Seller</span><span>Product and inventory management</span></footer>
<script>
(function () {
    "use strict";

    const samples = [
        { name: "Wireless Headphones", category: "Electronics", price: "1299.00", description: "Comfortable wireless headphones with clear sound and a long-lasting battery." },
        { name: "Everyday Backpack", category: "Accessories", price: "899.00", description: "A lightweight backpack with useful compartments for work, college, or travel." },
        { name: "Stainless Steel Water Bottle", category: "Home & Kitchen", price: "499.00", description: "A reusable stainless-steel bottle for everyday hydration." },
        { name: "Adjustable Desk Lamp", category: "Home & Office", price: "749.00", description: "An adjustable desk lamp for reading, studying, and focused work." },
        { name: "Portable Bluetooth Speaker", category: "Electronics", price: "1599.00", description: "A compact portable speaker with balanced audio for everyday listening." },
        { name: "Cotton T-Shirt", category: "Clothing", price: "599.00", description: "A soft cotton T-shirt designed for comfortable everyday wear." },
        { name: "Mechanical Keyboard", category: "Electronics", price: "2499.00", description: "A responsive keyboard suitable for work, study, and gaming." },
        { name: "Ceramic Coffee Mug", category: "Home & Kitchen", price: "299.00", description: "A reusable ceramic mug for coffee, tea, and other hot drinks." },
        { name: "Yoga Mat", category: "Sports & Fitness", price: "999.00", description: "A cushioned exercise mat for stretching, yoga, and home workouts." },
        { name: "LED Study Light", category: "Home & Office", price: "649.00", description: "A compact LED study light for desks and bedside tables." }
    ];

    const form = document.querySelector(".seller-create-form");
    const button = document.getElementById("fillRandomProduct");
    const status = document.getElementById("sampleProductMessage");
    if (!form || !button) return;

    const fields = {
        name: form.querySelector('[name="name"]'),
        category: form.querySelector('[name="category"]'),
        price: form.querySelector('[name="price"]'),
        stock: form.querySelector('[name="stockQuantity"]'),
        description: form.querySelector('[name="description"]')
    };

    let previousIndex = -1;
    button.addEventListener("click", function () {
        let index = Math.floor(Math.random() * samples.length);
        if (samples.length > 1 && index === previousIndex) {
            index = (index + 1 + Math.floor(Math.random() * (samples.length - 1))) % samples.length;
        }
        previousIndex = index;
        const sample = samples[index];
        fields.name.value = sample.name;
        fields.category.value = sample.category;
        fields.price.value = sample.price;
        fields.stock.value = String(Math.floor(Math.random() * 40) + 1);
        fields.description.value = sample.description;
        if (status) status.textContent = "Sample details filled. Review them, then click Create product listing to save.";
    });
})();
</script>
</body></html>
