<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
    <a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark">M</span><span>MarketSquare</span></a>
    <nav class="header-links"><a href="${pageContext.request.contextPath}/login">Sign in</a></nav>
</header>
<main class="auth-layout">
    <section class="auth-card">
        <p class="eyebrow">JOIN THE MARKETPLACE</p>
        <h1 class="auth-title">Create your <span>account.</span></h1>
        <p class="muted-copy">Create a buyer account to shop, or a seller account to manage your product listings.</p>
        <c:if test="${not empty errorMessage}"><div class="message message-error" role="alert"><c:out value="${errorMessage}" /></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/register" class="app-form">
            <div class="test-data-tools" aria-label="Test account autofill options">
                <button class="button button-secondary" type="button" id="fillBuyerTestDataButton">Fill random Buyer details</button>
                <button class="button button-secondary" type="button" id="fillSellerTestDataButton">Fill random Seller details</button>
                <button class="button button-secondary" type="button" id="fillTestDataButton">Choose a random role</button>
                <p class="small-note" id="testDataMessage" role="status" aria-live="polite">For local testing only. Choose Buyer or Seller, or let the form pick a role. The form will not be submitted automatically.</p>
            </div>
            <label for="fullName">Full name</label>
            <input id="fullName" name="fullName" type="text" autocomplete="name" maxlength="120" required value="<c:out value='${fullName}' />">
            <label for="email">Email address</label>
            <input id="email" name="email" type="email" autocomplete="email" maxlength="254" required value="<c:out value='${email}' />">
            <label for="role">Account type</label>
            <select id="role" name="role" required>
                <option value="BUYER" <c:if test="${empty role or role eq 'BUYER'}">selected</c:if>>Buyer — browse and purchase products</option>
                <option value="SELLER" <c:if test="${role eq 'SELLER'}">selected</c:if>>Seller — list and manage products</option>
            </select>
            <label for="password">Password <span class="label-hint">At least 10 characters</span></label>
            <input id="password" name="password" type="password" autocomplete="new-password" minlength="10" required>
            <label for="confirmPassword">Confirm password</label>
            <input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" minlength="10" required>
            <button class="button button-primary form-submit" type="submit">Create account <span aria-hidden="true">→</span></button>
        </form>
        <p class="form-footnote">Already registered? <a href="${pageContext.request.contextPath}/login">Sign in</a>.</p>
        <p class="small-note">Administrator accounts are provisioned separately and cannot be created through public registration.</p>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Academic Java project</span><a href="${pageContext.request.contextPath}/home">Back to home</a></footer>
<script>
(function () {
    "use strict";

    var buyerButton = document.getElementById("fillBuyerTestDataButton");
    var sellerButton = document.getElementById("fillSellerTestDataButton");
    var randomButton = document.getElementById("fillTestDataButton");
    var message = document.getElementById("testDataMessage");
    if (!buyerButton || !sellerButton || !randomButton || !message) return;

    var names = ["Alex Morgan", "Jordan Taylor", "Sam Parker", "Riley Morgan", "Casey Bennett", "Jamie Reed", "Avery Brooks", "Taylor Quinn"];
    var lower = "abcdefghijkmnopqrstuvwxyz";
    var upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    var digits = "23456789";
    var symbols = "!@#$%*-_";
    var allChars = lower + upper + digits + symbols;

    function randomIndex(max) {
        var value = new Uint32Array(1);
        window.crypto.getRandomValues(value);
        return value[0] % max;
    }

    function pick(chars) {
        return chars.charAt(randomIndex(chars.length));
    }

    function makePassword() {
        var chars = [pick(lower), pick(upper), pick(digits), pick(symbols)];
        while (chars.length < 16) chars.push(pick(allChars));
        for (var i = chars.length - 1; i > 0; i--) {
            var j = randomIndex(i + 1);
            var temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return chars.join("");
    }

    function fillDetails(role) {
        var name = names[randomIndex(names.length)];
        var uniquePart = Date.now().toString(36) + randomIndex(0x1000000).toString(16).padStart(6, "0");
        var password = makePassword();
        var emailPrefix = role === "BUYER" ? "buyer.test." : "seller.test.";

        document.getElementById("fullName").value = name;
        document.getElementById("email").value = emailPrefix + uniquePart + "@example.com";
        document.getElementById("role").value = role;
        document.getElementById("password").value = password;
        document.getElementById("confirmPassword").value = password;

        message.textContent = role.charAt(0) + role.slice(1).toLowerCase()
            + " test details filled. Test password: " + password
            + ". Save it if you want to sign in later. Nothing has been submitted.";
    }

    buyerButton.addEventListener("click", function () { fillDetails("BUYER"); });
    sellerButton.addEventListener("click", function () { fillDetails("SELLER"); });
    randomButton.addEventListener("click", function () {
        fillDetails(randomIndex(2) === 0 ? "BUYER" : "SELLER");
    });
}());
</script>
</body>
</html>
