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
</body>
</html>
