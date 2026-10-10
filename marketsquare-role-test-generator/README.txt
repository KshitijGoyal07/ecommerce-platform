MarketSquare registration test-account autofill

This patch updates only:
  src/main/webapp/WEB-INF/views/register.jsp

It adds buttons to fill a Buyer test account, a Seller test account, or a random role. It only fills the form and does not submit it. The email addresses use the reserved example.com domain and are intended for local testing only. Generated passwords are shown in the page so the tester can save them for the later sign-in test.

Install:
1. Extract this ZIP.
2. Copy the included register.jsp to your existing project's
   src/main/webapp/WEB-INF/views/register.jsp, replacing that file only.
3. In the project folder (where pom.xml is located), run: mvn clean package
4. Redeploy target/ecommerce-platform.war to Tomcat using the project's usual deployment steps.
5. Open /ecommerce-platform/register and test the Buyer or Seller autofill buttons.
