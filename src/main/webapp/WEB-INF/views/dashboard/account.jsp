
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Account | MarketSquare</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/application.css">

    <style>
        .account-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
        }
        .account-form {
            display: grid;
            gap: 14px;
        }
        .account-form label {
            display: grid;
            gap: 7px;
        }
        .account-form input,
        .account-form select,
        .account-form textarea {
            width: 100%;
            box-sizing: border-box;
        }
        .account-record {
            padding: 16px 0;
            border-bottom: 1px solid var(--border, #343b52);
        }
        .account-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 12px;
        }
        .account-actions form {
            display: inline-flex;
        }
        .account-note {
            font-size: 0.9rem;
            line-height: 1.5;
            opacity: 0.8;
        }
        .account-section {
            margin-bottom: 24px;
        }
        @media (max-width: 600px) {
            .account-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body class="app-body">

<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/buyer/dashboard">
        <span class="brand-mark">M</span>
        <span>MarketSquare</span>
    </a>

    <div class="header-user">
        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/buyer/dashboard">Shop</a>
        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/buyer/orders">Orders</a>
        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/buyer/wishlist">Wishlist</a>
        <form method="post" action="${pageContext.request.contextPath}/logout">
            <button class="button button-secondary" type="submit">Sign out</button>
        </form>
    </div>
</header>

<main class="dashboard-layout">

    <section class="dashboard-intro">
        <p class="eyebrow">BUYER WORKSPACE</p>
        <h1 class="dashboard-title">My account<span>.</span></h1>
        <p class="muted-copy">
            Manage your personal details, delivery addresses,
            and saved demo payment methods.
        </p>
    </section>

    <c:if test="${not empty successMessage}">
        <div class="message message-success" role="status">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="message message-error" role="alert">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <section class="panel account-section">
        <div class="panel-heading">
            <div>
                <p class="eyebrow">PROFILE</p>
                <h2>Personal details</h2>
            </div>
        </div>

        <form class="account-form" method="post"
              action="${pageContext.request.contextPath}/buyer/account">

            <input type="hidden" name="csrfToken"
                   value="<c:out value='${csrfToken}' />">
            <input type="hidden" name="action" value="updateProfile">

            <label>
                Full name
                <input type="text" name="fullName"
                       maxlength="120" required
                       value="<c:out value='${displayName}' />">
            </label>

            <label>
                Email address
                <input type="email" name="email"
                       maxlength="254" required
                       value="<c:out value='${email}' />">
            </label>

            <div>
                <button class="button button-primary" type="submit">
                    Save profile
                </button>
            </div>
        </form>
    </section>

    <div class="account-grid">

        <section class="panel account-section">
            <div class="panel-heading">
                <div>
                    <p class="eyebrow">DELIVERY</p>
                    <h2>Address book</h2>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty addresses}">
                    <p class="muted-copy">No saved addresses yet.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="address" items="${addresses}">
                        <div class="account-record">
                            <strong><c:out value="${address.label}" /></strong>

                            <c:if test="${address.isDefault}">
                                <span class="status-pill">Default</span>
                            </c:if>

                            <p>
                                <c:out value="${address.recipientName}" /><br>
                                <c:out value="${address.phone}" /><br>
                                <c:out value="${address.line1}" /><br>

                                <c:if test="${not empty address.line2}">
                                    <c:out value="${address.line2}" /><br>
                                </c:if>

                                <c:out value="${address.city}" />,
                                <c:out value="${address.state}" />
                                <c:out value="${address.postalCode}" /><br>
                                <c:out value="${address.country}" />
                            </p>

                            <div class="account-actions">
                                <c:if test="${not address.isDefault}">
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/buyer/account">
                                        <input type="hidden" name="csrfToken"
                                               value="<c:out value='${csrfToken}' />">
                                        <input type="hidden" name="action"
                                               value="defaultAddress">
                                        <input type="hidden" name="addressId"
                                               value="${address.id}">
                                        <button class="button button-secondary"
                                                type="submit">Make default</button>
                                    </form>
                                </c:if>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/buyer/account"
                                      onsubmit="return confirm('Remove this saved address?');">
                                    <input type="hidden" name="csrfToken"
                                           value="<c:out value='${csrfToken}' />">
                                    <input type="hidden" name="action"
                                           value="deleteAddress">
                                    <input type="hidden" name="addressId"
                                           value="${address.id}">
                                    <button class="button button-secondary"
                                            type="submit">Remove</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>

            <h3>Add an address</h3>

            <form class="account-form" method="post"
                  action="${pageContext.request.contextPath}/buyer/account">

                <input type="hidden" name="csrfToken"
                       value="<c:out value='${csrfToken}' />">
                <input type="hidden" name="action" value="addAddress">

                <label>
                    Label
                    <input name="label" maxlength="50"
                           placeholder="Home, Work..." required>
                </label>

                <label>
                    Recipient name
                    <input name="recipientName" maxlength="120" required>
                </label>

                <label>
                    Phone
                    <input name="phone" maxlength="30"
                           autocomplete="tel" required>
                </label>

                <label>
                    Address line 1
                    <input name="line1" maxlength="255" required>
                </label>

                <label>
                    Address line 2 (optional)
                    <input name="line2" maxlength="255">
                </label>

                <label>
                    City
                    <input name="city" maxlength="100" required>
                </label>

                <label>
                    State
                    <input name="state" maxlength="100" required>
                </label>

                <label>
                    Postal code
                    <input name="postalCode" maxlength="20" required>
                </label>

                <label>
                    Country
                    <input name="country" maxlength="100" value="India" required>
                </label>

                <button class="button button-primary" type="submit">
                    Save address
                </button>
            </form>
        </section>

        <section class="panel account-section">
            <div class="panel-heading">
                <div>
                    <p class="eyebrow">CHECKOUT PREFERENCES</p>
                    <h2>Saved payment methods</h2>
                </div>
            </div>

            <p class="account-note">
                Demo payment preferences only. Never enter a full card
                number, CVV, banking password, or UPI PIN here.
            </p>

            <c:choose>
                <c:when test="${empty paymentMethods}">
                    <p class="muted-copy">No saved payment methods yet.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="method" items="${paymentMethods}">
                        <div class="account-record">
                            <strong><c:out value="${method.label}" /></strong>

                            <p>
                                Type: <c:out value="${method.type}" />
                                <c:if test="${not empty method.lastFour}">
                                    <br>Ending in
                                    <c:out value="${method.lastFour}" />
                                </c:if>
                            </p>

                            <c:if test="${method.isDefault}">
                                <span class="status-pill">Default</span>
                            </c:if>

                            <div class="account-actions">
                                <c:if test="${not method.isDefault}">
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/buyer/account">
                                        <input type="hidden" name="csrfToken"
                                               value="<c:out value='${csrfToken}' />">
                                        <input type="hidden" name="action"
                                               value="defaultPayment">
                                        <input type="hidden" name="paymentMethodId"
                                               value="${method.id}">
                                        <button class="button button-secondary"
                                                type="submit">Make default</button>
                                    </form>
                                </c:if>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/buyer/account"
                                      onsubmit="return confirm('Remove this saved payment method?');">
                                    <input type="hidden" name="csrfToken"
                                           value="<c:out value='${csrfToken}' />">
                                    <input type="hidden" name="action"
                                           value="deletePayment">
                                    <input type="hidden" name="paymentMethodId"
                                           value="${method.id}">
                                    <button class="button button-secondary"
                                            type="submit">Remove</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>

            <h3>Add a payment method</h3>

            <form class="account-form" method="post"
                  action="${pageContext.request.contextPath}/buyer/account">

                <input type="hidden" name="csrfToken"
                       value="<c:out value='${csrfToken}' />">
                <input type="hidden" name="action" value="addPayment">

                <label>
                    Payment type
                    <select name="type" required>
                        <option value="CARD">Card demo</option>
                        <option value="UPI">UPI demo</option>
                        <option value="COD">Cash on delivery</option>
                        <option value="OTHER">Other</option>
                    </select>
                </label>

                <label>
                    Display label
                    <input name="label" maxlength="100"
                           placeholder="e.g. Personal UPI demo" required>
                </label>

                <label>
                    Last four digits (cards only)
                    <input name="lastFour" inputmode="numeric"
                           pattern="[0-9]{4}" maxlength="4"
                           placeholder="Optional">
                </label>

                <button class="button button-primary" type="submit">
                    Save payment method
                </button>
            </form>
        </section>

    </div>

</main>

<footer class="site-footer">
    <span>MarketSquare · Buyer</span>
    <span>Account and checkout preferences</span>
</footer>

</body>
</html>
