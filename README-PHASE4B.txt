MarketSquare — Seller Product Management Patch (Phase 4B)

INSTALL
1. Back up/commit current work first if you have uncommitted changes.
2. Extract this ZIP directly into the EXISTING inner ecommerce-platform project folder (the folder containing pom.xml).
3. Merge the src and docs folders. Allow replacement for these modified files:
   - src/main/java/com/ecommerce/repository/jdbc/ProductDao.java
   - src/main/java/com/ecommerce/controller/SellerDashboardServlet.java
   - src/main/webapp/WEB-INF/views/dashboard/seller.jsp
   - src/main/webapp/WEB-INF/views/dashboard/buyer.jsp
   - src/main/webapp/assets/css/application.css
4. This patch does not replace pom.xml, DatabaseConnection.java, schema.sql, or register.jsp.

DATABASE PERMISSIONS
Log into MySQL as root/admin and run:
GRANT SELECT, INSERT, UPDATE ON marketsquare.products TO 'marketsquare_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON marketsquare.categories TO 'marketsquare_app'@'localhost';

These grant product/category writes needed by the seller UI. Product deletion is not used; products are deactivated to preserve order history.

BUILD & TEST
In the existing project folder in VS Code, run:
  mvn clean test

The existing tests plus ProductListingInputTest should pass.

DEPLOYMENT
Use the same Tomcat redeploy process previously used for this project. Start Tomcat from a terminal in which JAVA_HOME, CATALINA_HOME, CATALINA_BASE and the MARKETSQUARE_DB_* environment variables are set. The deployed WAR must include the MySQL JDBC driver. The existing DatabaseConnection.java fix that explicitly loads com.mysql.cj.jdbc.Driver should be retained.

FUNCTIONAL CHECK
1. Sign in with a Seller account (not a Buyer account).
2. Open /ecommerce-platform/seller/dashboard.
3. Add a product with a price in INR (example: 1299.00) and a stock quantity.
4. Confirm it appears in the seller list.
5. Sign in with a Buyer account and confirm active products appear in /buyer/dashboard.
6. Test edit details, setting stock to zero or another non-negative quantity, and deactivation.
