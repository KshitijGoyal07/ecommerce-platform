# Authentication and role dashboards

## Current slice

- Public registration supports buyer and seller roles. Public users cannot assign themselves the administrator role.
- Passwords are hashed with PBKDF2-HMAC-SHA-256 using an independently generated random salt and a stored work factor. Plaintext passwords are never written to the database.
- Login verifies the password hash, rejects inactive users, rotates the HTTP session ID on successful authentication, and stores only the safe `UserAccount` model in the session.
- Dashboard URLs are protected by a role filter. Buyers can view active products; sellers can view only products associated with their account; administrators can view aggregate account/product counts.
- Database credentials are read from `MARKETSQUARE_DB_URL` (optional), `MARKETSQUARE_DB_USER`, and `MARKETSQUARE_DB_PASSWORD` environment variables.

## Local runtime environment

Start Tomcat from a PowerShell terminal where these variables have been set. The Tomcat process inherits the environment of its parent process:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2"
$env:CATALINA_HOME = "C:\tools\apache-tomcat-10.1.60"
$env:CATALINA_BASE = "C:\tools\apache-tomcat-10.1.60"
$env:MARKETSQUARE_DB_URL = "jdbc:mysql://localhost:3306/marketsquare?serverTimezone=UTC"
$env:MARKETSQUARE_DB_USER = "marketsquare_app"
# Set the password interactively; do not put it in source control.
$securePassword = Read-Host "Database password" -AsSecureString
$env:MARKETSQUARE_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
& "$env:CATALINA_HOME\bin\startup.bat"
```

Use a separate restricted MySQL account for the web application rather than the `root` account. During local setup, `root` may be used briefly to verify the connection, but create a least-privilege application account before using the web app.

## Routes

- `/home`: public landing page
- `/register`: buyer/seller registration
- `/login`: sign-in
- `/logout` (POST): sign out
- `/dashboard`: redirect to the dashboard corresponding to the signed-in role
- `/buyer/dashboard`: active catalogue
- `/seller/dashboard`: the signed-in seller's product listings
- `/admin/dashboard`: active-account and product counts

Administrator accounts are provisioned separately by a trusted administrator; there is no public administrator registration form. This checkpoint's dashboards are the integration foundation. Wishlist, checkout, editable listings, and full user/order management are subsequent features.
