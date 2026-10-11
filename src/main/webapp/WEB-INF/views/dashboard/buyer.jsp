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
    
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/style.css">

    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/application.css?v=marketplace-filters-1">

</head>
<body class="app-body">
<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a>
    <nav class="header-user" aria-label="Buyer navigation">
        <span>Buyer workspace</span>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/cart">Cart (<c:out value="${cartQuantity}" />)</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/wishlist">Wishlist</a>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/orders">My orders</a>
        <form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form>
    </nav>
</header>
<main class="dashboard-layout">
    <section class="dashboard-intro">
        <p class="eyebrow">BUYER WORKSPACE</p>
        <h1 class="dashboard-title">Welcome, <span><c:out value="${displayName}" /></span></h1>
        <p class="muted-copy">Explore active MarketSquare products, save favourites, and revisit products you have viewed.</p>
    </section>
    <section class="panel" id="products">
                
        <div class="panel-heading">
            <div>
                <p class="eyebrow">MARKETPLACE</p>
                <h2>Available products</h2>
            </div>

            <div class="marketplace-heading-actions">
                <button
                    type="button"
                    class="button button-secondary marketplace-filter-toggle"
                    id="marketplaceFilterToggle"
                    aria-controls="marketplaceFilterPanel"
                    aria-expanded="false">
                    <span aria-hidden="true">☷</span>
                    Filters &amp; sort
                    <span aria-hidden="true">⌄</span>
                </button>

                <span class="count-chip" id="marketplaceProductCount">
                    <c:out value="${productCount}" /> shown
                </span>
            </div>
        </div>
        
<section
    class="marketplace-filter-panel"
    id="marketplaceFilterPanel"
    aria-label="Marketplace filters"
    hidden>

    <div class="marketplace-filter-grid">

        <div class="marketplace-filter-field">
            <label for="marketplaceSort">Sort by price</label>
            <select id="marketplaceSort">
                <option value="default">Default order</option>
                <option value="price-asc">Price: low to high</option>
                <option value="price-desc">Price: high to low</option>
            </select>
        </div>

        <fieldset class="marketplace-filter-field marketplace-category-field">
            <legend>Categories</legend>
            <div
                id="marketplaceCategoryOptions"
                class="marketplace-category-options">
            </div>
        </fieldset>

        
        <div class="marketplace-filter-field marketplace-price-filter">

            <div class="marketplace-price-heading">
                <span>Price range</span>
                <output id="marketplacePriceRangeLabel">
                    Loading range...
                </output>
            </div>

            <div class="marketplace-price-inputs">
                <label for="marketplaceMinAmount">
                    Minimum amount (₹)
                    <input
                        type="number"
                        id="marketplaceMinAmount"
                        min="0"
                        step="0.01"
                        value="0">
                </label>

                <label for="marketplaceMaxAmount">
                    Maximum amount (₹)
                    <input
                        type="number"
                        id="marketplaceMaxAmount"
                        min="0"
                        step="0.01"
                        value="0">
                </label>
            </div>

            <label for="marketplaceMinPrice">
                Minimum price:
                <span id="marketplaceMinPriceValue"></span>
            </label>

            <input
                type="range"
                id="marketplaceMinPrice"
                min="0"
                max="1"
                value="0"
                step="0.01">

            <label for="marketplaceMaxPrice">
                Maximum price:
                <span id="marketplaceMaxPriceValue"></span>
            </label>

            <input
                type="range"
                id="marketplaceMaxPrice"
                min="0"
                max="1"
                value="1"
                step="0.01">

            <button
                type="button"
                class="button button-secondary marketplace-reset-price"
                id="marketplaceResetPrice">
                Reset price
            </button>

        </div>


                <label class="marketplace-stock-toggle">
                    <input type="checkbox" id="includeOutOfStock">
                    <span>
                        <strong>Include out-of-stock products</strong>
                        <small>
                            Show products with zero available stock too.
                        </small>
                    </span>
                </label>

            </div>

            <div class="marketplace-filter-actions">
                <button
                    type="button"
                    class="button button-secondary"
                    id="marketplaceResetFilters">
                    Reset filters
                </button>
            </div>
        </section>

        <c:choose>
            <c:when test="${empty products}">
                <p class="empty-state">No active products are listed yet. Seller listings will appear here once added.</p>
            </c:when>
            <c:otherwise>
                
                <div class="product-grid" id="marketplaceProductGrid">
                    <c:forEach var="product" items="${products}">           
                    <article
                        class="product-card marketplace-product-card"
                        data-category="<c:out value='${product.category}' />"
                        data-price="<c:out value='${product.price}' />"
                        data-stock="<c:out value='${product.stockQuantity}' />">
                            <c:if test="${not empty product.imagePath}"><img class="product-card-image" src="${pageContext.request.contextPath}/product-images/<c:out value='${product.imagePath}' />" alt="Photo of <c:out value='${product.name}' />" loading="lazy"></c:if>
                            <div class="product-category"><c:out value="${product.category}" /></div>
                            <h3><a href="${pageContext.request.contextPath}/buyer/product?id=${product.id}"><c:out value="${product.name}" /></a></h3>
                            <p class="product-description"><c:out value="${product.description}" /></p>
                            <div class="product-meta">
                                <strong><fmt:formatNumber value="${product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong>
                                <span><c:out value="${product.stockQuantity}" /> in stock</span>
                            </div>
                            <p><a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/product?id=${product.id}">View details</a></p>
                            <c:choose>
                                <c:when test="${wishlistProductIds.contains(product.id)}">
                                    <p><a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/wishlist">Saved to wishlist ✓</a></p>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" action="${pageContext.request.contextPath}/buyer/wishlist" class="wishlist-inline-form">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                        <input type="hidden" name="action" value="add">
                                        <input type="hidden" name="productId" value="<c:out value='${product.id}' />">
                                        <input type="hidden" name="returnTo" value="dashboard">
                                        <button class="button button-secondary" type="submit">♡ Save to wishlist</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
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
                <p id="marketplaceNoResults" class="empty-state" hidden>
                    No products match these filters. Try widening the price range
                    or resetting the filters.
                </p>
            </c:otherwise>
        </c:choose>
    </section>
    <c:if test="${not empty recentlyViewedProducts}">
        <section class="panel">
            <div class="panel-heading"><div><p class="eyebrow">YOUR ACTIVITY</p><h2>Recently viewed</h2></div><span class="count-chip"><c:out value="${recentlyViewedProducts.size()}" /> products</span></div>
            <div class="product-grid">
                <c:forEach var="recentProduct" items="${recentlyViewedProducts}">
                    <article class="product-card">
                        <c:if test="${not empty recentProduct.imagePath}"><img class="product-card-image" src="${pageContext.request.contextPath}/product-images/<c:out value='${recentProduct.imagePath}' />" alt="Photo of <c:out value='${recentProduct.name}' />" loading="lazy"></c:if>
                        <div class="product-category"><c:out value="${recentProduct.category}" /></div>
                        <h3><a href="${pageContext.request.contextPath}/buyer/product?id=${recentProduct.id}"><c:out value="${recentProduct.name}" /></a></h3>
                        <p class="product-description"><c:out value="${recentProduct.description}" /></p>
                        <div class="product-meta"><strong><fmt:formatNumber value="${recentProduct.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong><span><c:out value="${recentProduct.stockQuantity}" /> in stock</span></div>
                        <p><a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/product?id=${recentProduct.id}">View again</a></p>
                    </article>
                </c:forEach>
            </div>
        </section>
    </c:if>
    <section class="feature-row">
        <article class="feature-mini"><p class="eyebrow">SHOPPING</p><h3>Your cart</h3><p>Review quantities and continue to checkout with INR totals.</p><p><a href="${pageContext.request.contextPath}/buyer/cart">Open shopping cart →</a></p></article>
        <article class="feature-mini"><p class="eyebrow">PURCHASES</p><h3>Order history</h3><p>Review previous orders, item prices, and order status.</p><p><a href="${pageContext.request.contextPath}/buyer/orders">View my orders →</a></p></article>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Buyer</span><span>Wishlist and recently viewed · INR pricing</span></footer>
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

<script>
(() => {
    const grid = document.getElementById("marketplaceProductGrid");
    const toggle = document.getElementById("marketplaceFilterToggle");
    const panel = document.getElementById("marketplaceFilterPanel");

    // Open and close the filter panel.
    if (toggle && panel) {
        toggle.addEventListener("click", () => {
            panel.hidden = !panel.hidden;
            toggle.setAttribute("aria-expanded", String(!panel.hidden));
        });
    }

    if (!grid) return;

    const cards = Array.from(
        grid.querySelectorAll(".marketplace-product-card")
    );

    const options = document.getElementById("marketplaceCategoryOptions");
    const sortSelect = document.getElementById("marketplaceSort");
    const minInput = document.getElementById("marketplaceMinPrice");
    const maxInput = document.getElementById("marketplaceMaxPrice");
    const minAmountInput =
        document.getElementById("marketplaceMinAmount");

    const maxAmountInput =
        document.getElementById("marketplaceMaxAmount");

    const resetPriceButton =
        document.getElementById("marketplaceResetPrice");
    const stockToggle = document.getElementById("includeOutOfStock");
    const count = document.getElementById("marketplaceProductCount");
    const noResults = document.getElementById("marketplaceNoResults");

    if (!options || !sortSelect || !minInput || !maxInput || !minAmountInput || !maxAmountInput || !resetPriceButton || !stockToggle) {
        console.error("Marketplace filter controls are missing.");
        return;
    }

    if (cards.length === 0) {
        if (count) count.textContent = "0 shown";
        if (noResults) noResults.hidden = false;
        return;
    }

    // Remember the original product order and read each category.
    cards.forEach((card, index) => {
        card.dataset.originalIndex = String(index);
        card.dataset.category =
            (card.dataset.category || "").trim() || "Other";
    });

    // Build category checkboxes from the products currently displayed.
    function addCategory(labelText, value, isAll = false) {
        const label = document.createElement("label");
        label.className = "marketplace-category-option";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.value = value;

        if (isAll) {
            checkbox.dataset.allCategories = "true";
            checkbox.checked = true;
        } else {
            checkbox.dataset.categoryOption = "true";
        }

        const text = document.createElement("span");
        text.textContent = labelText;

        label.append(checkbox, text);
        options.appendChild(label);

        return checkbox;
    }

    // Avoid duplicate category options if initialization runs again.
    options.replaceChildren();

    const allCategories = addCategory(
        "All categories",
        "all",
        true
    );

    const categoryNames = [...new Set(
        cards.map(card => card.dataset.category)
    )].sort((a, b) => a.localeCompare(b));

    categoryNames.forEach(category => {
        addCategory(category, category);
    });

    const categoryCheckboxes = Array.from(
        options.querySelectorAll("[data-category-option]")
    );

    // Determine the real price range from the displayed products.
    const prices = cards
        .map(card => Number(card.dataset.price))
        .filter(price => Number.isFinite(price) && price >= 0);

    if (prices.length === 0) {
        console.error("No valid marketplace product prices were found.");
        return;
    }

    const lowestPrice = Math.floor(Math.min(...prices));
    let highestPrice = Math.ceil(Math.max(...prices));

    if (highestPrice <= lowestPrice) {
        highestPrice = lowestPrice + 1;
    }

    [minInput, maxInput, minAmountInput, maxAmountInput]
    .forEach(input => {
        input.min = String(lowestPrice);
        input.max = String(highestPrice);
    });

    minInput.step = "0.01";
    maxInput.step = "0.01";

    minAmountInput.step = "0.01";
    maxAmountInput.step = "0.01";

    minInput.value = String(lowestPrice);
    maxInput.value = String(highestPrice);

    minAmountInput.value = String(lowestPrice);
    maxAmountInput.value = String(highestPrice);

    minInput.value = String(lowestPrice);
    maxInput.value = String(highestPrice);

    const currency = new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    function updatePriceLabels() {
        const min = Number(minInput.value);
        const max = Number(maxInput.value);

        document.getElementById("marketplaceMinPriceValue")
            .textContent = currency.format(min);

        document.getElementById("marketplaceMaxPriceValue")
            .textContent = currency.format(max);

        document.getElementById("marketplacePriceRangeLabel")
            .textContent =
                `${currency.format(min)} – ${currency.format(max)}`;

        minAmountInput.value = String(Number(minInput.value));
        maxAmountInput.value = String(Number(maxInput.value));
    }

    // Apply category, price, stock and sorting choices together.
    function applyFilters() {
        const selectedCategories = categoryCheckboxes
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value);

        const allSelected =
            allCategories.checked || selectedCategories.length === 0;

        const minPrice = Number(minInput.value);
        const maxPrice = Number(maxInput.value);
        const includeStockZero = stockToggle.checked;
        const sort = sortSelect.value;

        const sortedCards = [...cards].sort((a, b) => {
            const priceA = Number(a.dataset.price);
            const priceB = Number(b.dataset.price);

            if (sort === "price-asc") return priceA - priceB;
            if (sort === "price-desc") return priceB - priceA;

            return Number(a.dataset.originalIndex) -
                Number(b.dataset.originalIndex);
        });

        let visibleCount = 0;

        sortedCards.forEach(card => {
            const category = card.dataset.category;
            const price = Number(card.dataset.price);
            const stock = Number(card.dataset.stock);

            const categoryMatches =
                allSelected || selectedCategories.includes(category);

            const priceMatches =
                Number.isFinite(price) &&
                price >= minPrice &&
                price <= maxPrice;

            const stockMatches =
                includeStockZero ||
                (Number.isFinite(stock) && stock > 0);

            const visible =
                categoryMatches && priceMatches && stockMatches;

            card.hidden = !visible;
            card.style.display = visible ? "" : "none";

            if (visible) visibleCount++;

            // Append cards in sorted order.
            grid.appendChild(card);
        });

        if (count) {
            count.textContent = `${visibleCount} shown`;
        }

        if (noResults) {
            noResults.hidden = visibleCount !== 0;
        }
    }

    // Selecting All categories clears individual category selections.
    options.addEventListener("change", event => {
        const changed = event.target;

        if (changed.matches("[data-all-categories]")) {
            if (changed.checked) {
                categoryCheckboxes.forEach(
                    checkbox => checkbox.checked = false
                );
            }
        } else if (changed.matches("[data-category-option]")) {
            if (changed.checked) {
                allCategories.checked = false;
            }

            if (!categoryCheckboxes.some(
                checkbox => checkbox.checked
            )) {
                allCategories.checked = true;
            }
        }

        applyFilters();
    });

    sortSelect.addEventListener("change", applyFilters);
    stockToggle.addEventListener("change", applyFilters);

    // Prevent the minimum price from exceeding the maximum,
    // and vice versa.
    function handlePriceChange(changedInput) {
        if (Number(minInput.value) > Number(maxInput.value)) {
            if (changedInput === minInput) {
                minInput.value = maxInput.value;
            } else {
                maxInput.value = minInput.value;
            }
        }

        updatePriceLabels();
        applyFilters();
    }

    minInput.addEventListener("input", () => {
        handlePriceChange(minInput);
    });

    maxInput.addEventListener("input", () => {
        handlePriceChange(maxInput);
    });

    
    function handleAmountChange(changedInput) {
        if (changedInput.value.trim() === "") {
            updatePriceLabels();
            return;
        }

        let value = Number(changedInput.value);

        if (!Number.isFinite(value)) {
            updatePriceLabels();
            return;
        }

        value = Math.max(
            lowestPrice,
            Math.min(highestPrice, value)
        );

        if (changedInput === minAmountInput) {
            minInput.value = String(value);

            if (value > Number(maxAmountInput.value)) {
                maxInput.value = String(value);
                maxAmountInput.value = String(value);
            }
        } else {
            maxInput.value = String(value);

            if (value < Number(minAmountInput.value)) {
                minInput.value = String(value);
                minAmountInput.value = String(value);
            }
        }

        updatePriceLabels();
        applyFilters();
    }

    minAmountInput.addEventListener("change", () => {
        handleAmountChange(minAmountInput);
    });

    maxAmountInput.addEventListener("change", () => {
        handleAmountChange(maxAmountInput);
    });

    function resetPriceRange() {
        minInput.value = String(lowestPrice);
        maxInput.value = String(highestPrice);

        updatePriceLabels();
        applyFilters();
    }

    resetPriceButton.addEventListener("click", resetPriceRange);

    // Reset every filter and restore the original order.
    document.getElementById("marketplaceResetFilters")
        .addEventListener("click", () => {
            allCategories.checked = true;

            categoryCheckboxes.forEach(
                checkbox => checkbox.checked = false
            );

            sortSelect.value = "default";
            minInput.value = String(lowestPrice);
            maxInput.value = String(highestPrice);
            stockToggle.checked = false;

            updatePriceLabels();
            applyFilters();
        });

    updatePriceLabels();
    applyFilters();
})();
</script>

</body>
</html>
