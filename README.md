# MarketSquare — Online E-commerce Platform

A role-based e-commerce web application for an academic Java project. The planned roles are **Buyer**, **Seller**, and **Admin**.

> **Current status:** Phase 1 starter scaffold. The landing page and Maven web-app structure are in place. Authentication, dashboards, data persistence, and shopping workflows will be added in later phases; this repository is not yet the complete application.

## Planned technology stack

- Java 17 or later
- Maven for dependency management and builds
- Jakarta Servlets and JSP for the web layer
- Apache Tomcat 10.1 for local deployment
- MySQL with JDBC for persistence
- HTML and CSS for the user interface

## Features planned

### Buyer
- Browse and search products
- Purchase products and view order confirmations
- Track orders and delivery status
- Manage wishlist and recently viewed products
- Manage account details, payment-method records, and addresses

### Seller
- Create, edit, and remove product listings
- Track inventory and low-stock levels
- Process orders for their products
- Review sales summaries

### Admin
- Manage users and roles
- Oversee product listings
- Manage and update orders
- Review system activity

## Project structure

```text
src/main/java/com/ecommerce/controller/   # HTTP controllers (Servlets)
src/main/webapp/WEB-INF/views/             # JSP views, not directly public
src/main/webapp/assets/css/                # Stylesheets
src/main/webapp/WEB-INF/web.xml            # Web application configuration
pom.xml                                    # Maven configuration
```

## Prerequisites

1. A JDK 17+ installed and available in the VS Code terminal.
2. Git installed.
3. Apache Maven installed, or the VS Code Maven extension configured to use Maven.
4. Apache Tomcat 10.1 configured for local deployment. The Servlet API dependency is supplied by Tomcat at runtime.
5. MySQL will be required from the database phase onward.

## Build the project

Open the project folder in VS Code, open **Terminal → New Terminal**, and run:

```bash
mvn clean package
```

A successful build creates `target/ecommerce-platform.war`.

To run it locally, deploy the WAR to a Tomcat 10.1 server and start the server. Then open:

```text
http://localhost:8080/ecommerce-platform/
```

The exact context path can differ if the WAR is deployed under a different name. The landing page is a scaffold at this stage.

## GitHub setup

Create an empty repository on GitHub named `ecommerce-platform` (do not add a README or `.gitignore`, because those files already exist locally). In the VS Code terminal, run these commands from the project root, replacing `YOUR-USERNAME` with your GitHub username:

```bash
git init
git add .
git commit -m "chore: initialize e-commerce project scaffold"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/ecommerce-platform.git
git push -u origin main
```

If Git asks you to identify yourself, configure the name and email you use for commits:

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Do not commit database passwords, access tokens, or other secrets. The `.gitignore` excludes local `.env` files and build output.

## Development phases

1. **Repository and project scaffold** — folder structure, Maven web application, landing-page shell, GitHub.
2. **Core Java domain model** — OOP, interfaces, inheritance, polymorphism, exceptions, collections/generics, and thread-safe inventory/order logic.
3. **Database and JDBC** — schema, connection configuration, DAO classes, CRUD, and seed data.
4. **Web workflows** — authentication, role-based access, Servlets/JSP, and Buyer/Seller/Admin dashboards.
5. **End-to-end features and quality** — product search, wishlist, cart/checkout, orders, validation, testing, UI polish, and documentation.
6. **Submission preparation** — final GitHub review, run instructions, screenshots, and the presentation (when requested).

## Notes

- Use Jakarta imports (`jakarta.servlet.*`) with Tomcat 10.1; do not use the older `javax.servlet.*` namespace.
- Keep SQL queries in database-access classes rather than placing them in JSP pages.
- Use prepared statements for SQL values and password hashing for account credentials when authentication is added.
