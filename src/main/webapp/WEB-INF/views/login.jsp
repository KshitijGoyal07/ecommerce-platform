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
    <nav class="header-links"><a href="${pageContext.request.contextPath}/register">Create account</a></nav>
</header>
<main class="auth-layout">
    <section class="auth-card">
        <p class="eyebrow">WELCOME BACK</p>
        <h1 class="auth-title">Sign in to your <span>workspace.</span></h1>
        <p class="muted-copy">Use the email address and password associated with your MarketSquare account.</p>
        <c:if test="${not empty errorMessage}"><div class="message message-error" role="alert"><c:out value="${errorMessage}" /></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/login" class="app-form">
            <label for="email">Email address</label>
            <input id="email" name="email" type="email" autocomplete="username" maxlength="254" required value="<c:out value='${email}' />">
            <label for="password">Password</label>
            <input id="password" name="password" type="password" autocomplete="current-password" required>
            <button class="button button-primary form-submit" type="submit">Sign in <span aria-hidden="true">→</span></button>
        </form>
        <p class="form-footnote">New to MarketSquare? <a href="${pageContext.request.contextPath}/register">Create an account</a>.</p>
    </section>
</main>
<footer class="site-footer"><span>MarketSquare · Academic Java project</span><a href="${pageContext.request.contextPath}/home">Back to home</a></footer>
</body>
</html>
