MarketSquare — Seller Product Form Autofill Patch

WHAT IT DOES
Adds a “Fill random test details” button above the Seller's new product form. Each click selects a sample product and fills name, category, price in INR, a random stock quantity, and description. It only fills the form; it never submits it.

INSTALL
1. Back up/commit your current work first if you have uncommitted changes.
2. Extract this ZIP into your EXISTING inner ecommerce-platform folder (the folder containing pom.xml).
3. Merge src folders and allow replacement of only:
   - src/main/webapp/WEB-INF/views/dashboard/seller.jsp
   - src/main/webapp/assets/css/application.css
4. No Java or database files are changed.

BUILD
In VS Code terminal at the folder containing pom.xml, run:
  mvn clean package

DEPLOY
If you already have Tomcat running, the safest option is to use your usual clean Tomcat redeployment workflow and copy target/ecommerce-platform.war into C:\tools\apache-tomcat-10.1.60\webapps\ after removing the previous deployed WAR/folder and JSP cache. Start Tomcat from a terminal with JAVA_HOME, CATALINA_HOME, CATALINA_BASE, and MARKETSQUARE_DB_* environment variables set.

TEST
Sign in as a Seller, open /ecommerce-platform/seller/dashboard, click “Fill random test details”, verify all product fields are populated, then decide whether to submit the test listing. Product prices are in INR (₹).
