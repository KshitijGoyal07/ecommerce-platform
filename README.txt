MarketSquare registration test-data button

Copy the enclosed src/main/webapp/WEB-INF/views/register.jsp into the same path in your existing ecommerce-platform project, replacing only the existing register.jsp file. Do not replace pom.xml or other files.

Then run `mvn clean package`, redeploy the WAR to Tomcat, and refresh /ecommerce-platform/register. The new button fills random sample values for testing but never submits the form automatically. It uses a fake @example.com address and generates a fresh password that is shown on screen so you can save it for a subsequent test login.

Important: this only autofills the form. It does not fix the separate “Registration is temporarily unavailable” database error; that will need to be diagnosed independently.
