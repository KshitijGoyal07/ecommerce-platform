MarketSquare — Phase 4C: Buyer Cart, Checkout, and Order History

WHAT THIS PATCH ADDS
- Session-scoped shopping cart (add, update, remove, clear)
- Checkout that calls OrderDao.placeOrder for transactional stock reservation
- Buyer order history pages
- Cart and checkout links from the Buyer dashboard
- INR formatting for line totals, subtotal, checkout, and order history
- ShoppingCart unit tests

INSTALL
1. Commit/push your existing work first; this patch replaces several existing files.
2. Extract this ZIP directly into the EXISTING inner ecommerce-platform project folder (the one containing pom.xml).
3. Merge the src and docs folders. Allow replacement for these files:
   - src/main/java/com/ecommerce/controller/BuyerDashboardServlet.java
   - src/main/java/com/ecommerce/repository/jdbc/OrderDao.java
   - src/main/webapp/WEB-INF/views/dashboard/buyer.jsp
   - src/main/webapp/assets/css/application.css
4. Other files in the ZIP are additions. Do not replace pom.xml, DatabaseConnection.java, ProductDao.java, register.jsp, or seller.jsp.

DATABASE PERMISSIONS
Run as MySQL root/admin (not in PowerShell):

GRANT SELECT, INSERT ON marketsquare.orders TO 'marketsquare_app'@'localhost';
GRANT SELECT, INSERT ON marketsquare.order_items TO 'marketsquare_app'@'localhost';

The application account already has SELECT/UPDATE on products and SELECT on users from previous phases. Checkout updates products.stock_quantity and inserts into orders/order_items. Do not use the root account for the web application.

BUILD
In VS Code terminal, from the folder containing pom.xml:
  mvn clean test

This patch adds 9 ShoppingCart tests to the existing 36-test suite, for 45 total expected tests. Then package the web app:
  mvn clean package

Maven is not installed in this packaging environment, so verify BUILD SUCCESS locally before deploying.

DEPLOYMENT
The site needs Tomcat restarted with JAVA_HOME, CATALINA_HOME, CATALINA_BASE, and MARKETSQUARE_DB_* variables set in the same PowerShell session. Use the same clean redeployment process used earlier, copying target/ecommerce-platform.war to C:\tools\apache-tomcat-10.1.60\webapps\ecommerce-platform.war after Tomcat has stopped.

FUNCTIONAL CHECK
1. Sign in as a Buyer.
2. Add an in-stock item from /buyer/dashboard.
3. Open /buyer/cart, change quantities and remove items.
4. Proceed to /buyer/checkout; use a test delivery address.
5. Submit the order and confirm the success message.
6. Open /buyer/orders and verify order totals and line prices appear in INR and stock has decreased.

Payments are not processed. The checkout only creates an academic/demo order in MySQL. The cart itself is session-scoped and is not persisted until checkout.
