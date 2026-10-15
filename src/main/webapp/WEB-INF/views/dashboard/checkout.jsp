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
    <nav class="header-user" aria-label="Checkout navigation">
        <a class="button button-secondary" href="${pageContext.request.contextPath}/buyer/cart">Back to cart</a>
        <form method="post" action="${pageContext.request.contextPath}/logout"><button class="button button-secondary" type="submit">Sign out</button></form>
    </nav>
</header>
<main class="dashboard-layout checkout-page">
    <section class="dashboard-intro"><p class="eyebrow">CHECKOUT</p><h1 class="dashboard-title">Almost <span>yours.</span></h1><p class="muted-copy">Confirm your items and delivery address. This academic checkout does not process real payments.</p></section>
    <c:if test="${not empty buyerCartError}"><div class="message message-error" role="alert"><c:out value="${buyerCartError}" /></div></c:if>
    <div class="checkout-layout-grid">
        <section class="panel checkout-items-panel">
            <div class="panel-heading"><div><p class="eyebrow">ORDER SUMMARY</p><h2>Items in your order</h2></div><span class="count-chip"><c:out value="${cartLines.size()}" /> products</span></div>
            <div class="checkout-items">
                <c:forEach var="line" items="${cartLines}">
                    <div class="checkout-item checkout-review-item">
                        <div class="checkout-item-description"><strong><c:out value="${line.product.name}" /></strong><span><c:out value="${line.quantity}" /> × <fmt:formatNumber value="${line.product.price}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></span></div>
                        <strong class="checkout-line-total"><fmt:formatNumber value="${line.lineTotal}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong>
                    </div>
                </c:forEach>
            </div>
            <div class="cart-summary checkout-grand-total"><span>Total payable (demo)</span><strong><fmt:formatNumber value="${subtotal}" type="currency" currencyCode="INR" minFractionDigits="2" maxFractionDigits="2" /></strong></div>
            <p class="small-note">Prices and stock are checked again when you place the order.</p>
        </section>
        <section class="panel checkout-address-panel">
            <p class="eyebrow">DELIVERY</p><h2>Where should it go?</h2>
            <form method="post" action="${pageContext.request.contextPath}/buyer/checkout" class="app-form checkout-form">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                <label for="shippingAddress">Delivery address</label>
                <textarea id="shippingAddress" name="shippingAddress" rows="5" minlength="10" maxlength="1000" required placeholder="Recipient name, house/street, area, city, state, PIN code"></textarea>
                <p class="small-note">Use a test address for this academic project. No payment or delivery provider will be contacted.</p>
                
                <fieldset class="payment-method-section">
                    <legend>Payment method</legend>

                    <p class="small-note">
                        Choose how you would like to pay. UPI and card are demonstrations
                        only; no real payment will be processed.
                    </p>

                    <label class="payment-method-option" for="paymentCod">
                        <input
                            type="radio"
                            id="paymentCod"
                            name="paymentMethod"
                            value="COD"
                            checked
                            required>
                        <span class="payment-method-copy">
                            <strong>Cash on delivery</strong>
                            <small>Pay when your order arrives.</small>
                        </span>
                    </label>

                    <label class="payment-method-option" for="paymentUpi">
                        <input
                            type="radio"
                            id="paymentUpi"
                            name="paymentMethod"
                            value="UPI"
                            required>
                        <span class="payment-method-copy">
                            <strong>UPI — Demo</strong>
                            <small>Simulated payment; no UPI app is opened.</small>
                        </span>
                    </label>

                    <label class="payment-method-option" for="paymentCard">
                        <input
                            type="radio"
                            id="paymentCard"
                            name="paymentMethod"
                            value="CARD"
                            required>
                        <span class="payment-method-copy">
                            <strong>Card — Demo</strong>
                            <small>No card details are requested or stored.</small>
                        </span>
                    </label>
                </fieldset>

                <button class="button button-primary form-submit place-order-button" type="submit">Place demo order <span aria-hidden="true">→</span></button>
            </form>
        </section>
    </div>
</main>
<footer class="site-footer"><span>MarketSquare · Checkout</span><span>Stock is reserved transactionally at order placement</span></footer>
</body>
</html>
