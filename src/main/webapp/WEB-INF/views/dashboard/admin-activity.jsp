
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Activity Monitoring | MarketSquare</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/application.css">

    <style>
        .activity-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .activity-table {
            width: 100%;
            min-width: 850px;
            border-collapse: collapse;
            margin-top: 20px;
        }

        .activity-table th,
        .activity-table td {
            padding: 14px 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid #343b52;
        }

        .activity-table th {
            font-size: 0.85rem;
            white-space: nowrap;
        }

        .activity-muted {
            overflow-wrap: anywhere;
        }
    </style>
</head>

<body class="app-body">

<header class="site-header">
    <a class="brand" href="${pageContext.request.contextPath}/home">
        <span class="brand-mark">M</span>
        <span>MarketSquare</span>
    </a>

    <div class="header-user">
        <span>Admin workspace</span>

        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/admin/dashboard">
            Dashboard
        </a>

        <form method="post"
              action="${pageContext.request.contextPath}/logout">
            <button class="button button-secondary" type="submit">
                Sign out
            </button>
        </form>
    </div>
</header>

<main class="dashboard-layout">

    <section class="dashboard-intro">
        <p class="eyebrow">ADMIN WORKSPACE</p>
        <h1 class="dashboard-title">Activity monitoring<span>.</span></h1>

        <p class="muted-copy">
            Welcome, <c:out value="${displayName}" />.
            Review the latest recorded actions across MarketSquare.
            The page shows up to 100 recent records.
        </p>

        <a class="button button-secondary"
           href="${pageContext.request.contextPath}/admin/dashboard">
            Admin dashboard
        </a>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <div>
                <p class="eyebrow">SYSTEM AUDIT TRAIL</p>
                <h2>Recent activity</h2>
            </div>
        </div>

        <div class="activity-table-wrap">
            <table class="activity-table">
                <thead>
                    <tr>
                        <th>Time</th>
                        <th>Actor</th>
                        <th>Email</th>
                        <th>Action</th>
                        <th>Entity</th>
                        <th>Entity ID</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="entry" items="${activities}">
                        <tr>
                            <td>
                                <c:out value="${entry.createdAtDisplay}" />
                            </td>

                            <td>
                                <c:out value="${entry.actorName}" />
                                <c:if test="${not empty entry.actorUserId}">
                                    <span class="activity-muted">
                                        (ID <c:out value="${entry.actorUserId}" />)
                                    </span>
                                </c:if>
                            </td>

                            <td class="activity-muted">
                                <c:out value="${entry.actorEmail}" />
                            </td>

                            <td>
                                <c:out value="${entry.action}" />
                            </td>

                            <td>
                                <c:out value="${entry.entityType}" />
                            </td>

                            <td>
                                <c:out value="${entry.entityId}" />
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty activities}">
                        <tr>
                            <td colspan="6">
                                No activity has been recorded yet.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </section>

</main>

<footer class="site-footer">
    <span>MarketSquare · Admin</span>
    <span>Recent system activity</span>
</footer>

</body>
</html>
