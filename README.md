# MarketSquare — Online E-Commerce Platform

MarketSquare is a Java-based e-commerce web application developed as an academic team project by a team of four. We built it to bring buyer, seller, and administrator workflows together in one place, from browsing products and placing demo orders to managing listings, inventory, and marketplace activity.

## Project team

We developed MarketSquare collaboratively as a team of four. Our shared work covered the buyer, seller, and administrator workflows, Java web application structure, JDBC/MySQL persistence, testing, and project documentation.

## Features

### Buyer Workspace
- Browse products and view product details.
- Filter products by category and price range.
- Sort products by price.
- Optionally display out-of-stock products.
- Save products to a wishlist and revisit viewed products.
- Add products to the cart and adjust quantities using plus/minus controls.
- Review checkout details, delivery addresses, and order totals.
- View order history, payment methods, and payment statuses.

### Seller Workspace
- Create, edit, and manage product listings.
- Upload, replace, and remove optional product images.
- Update inventory quantities and manage listing availability.
- Reactivate inactive product listings.
- Review orders and sales summaries.
- View buyer, delivery, and payment information associated with orders.

### Payment Demonstration
The application supports Cash on Delivery (COD), UPI Demo, and Card Demo. UPI and card payments are simulated for demonstration purposes; no real payment is processed.

## Technology Stack

- **Language:** Java
- **Web layer:** Jakarta Servlets and JSP
- **Build tool:** Apache Maven
- **Database:** MySQL with JDBC
- **Application server:** Apache Tomcat 10.1
- **Frontend:** HTML and CSS, with JavaScript for interactive controls
- **Testing:** JUnit through Maven

The project uses Jakarta Servlet APIs and targets Java 17 compatibility.

## Prerequisites

Install and configure the following before running the application:

1. JDK 17 or later.
2. Apache Maven.
3. MySQL Server.
4. Apache Tomcat 10.1.
5. Git, if cloning the repository.

## Database Configuration

MarketSquare uses MySQL 8.4 with JDBC for database connectivity.

### Initialize a fresh database

1. Start MySQL Server and open the MySQL command-line client.
2. Run the schema file using its absolute path (replace the example path with the location where you cloned the repository):

   ```sql
   SOURCE C:/path/to/ecommerce-platform/src/main/resources/db/schema.sql;
   ```

3. Apply the payment and wishlist-price migrations. These are required for a fresh database because the base schema does not yet define those columns:

   ```sql
   USE marketsquare;
   SOURCE C:/path/to/ecommerce-platform/src/main/resources/db/migration/V2__order_payment_details.sql;
   SOURCE C:/path/to/ecommerce-platform/src/main/resources/db/migration/V3__wishlist_price_snapshot.sql;
   ```

   Replace `C:/path/to/ecommerce-platform` with your actual project path. MySQL's `SOURCE` command accepts forward slashes in Windows paths.

4. Verify the database and tables:

   ```sql
   USE marketsquare;
   SHOW TABLES;
   SHOW COLUMNS FROM wishlists LIKE 'saved_price';
   ```

   The last command should show the `saved_price` column, which the wishlist uses to compare saved prices with current prices.

The base schema already includes the nullable `products.image_path` column, so **do not run `V1__product_images.sql` on a fresh database**.

### Applying migrations to an existing database

Back up the database before changing its schema. Do not blindly run every migration against an existing database:

- `V1__product_images.sql`: run once only if `products.image_path` is missing. Check with `SHOW COLUMNS FROM products LIKE 'image_path';`.
- `V2__order_payment_details.sql`: adds missing order payment columns and skips columns that already exist.
- `V3__wishlist_price_snapshot.sql`: required for wishlist price tracking, but it is **not safe to rerun** after `saved_price` already exists. Check with `SHOW COLUMNS FROM wishlists LIKE 'saved_price';` and run V3 only if that column is missing.

Select the `marketsquare` database before applying V3:

```sql
USE marketsquare;
```

Then use `SOURCE` with the absolute path to the specific migration file you need.

For the complete schema overview, see [`docs/database-schema.md`](docs/database-schema.md).

### Configure database credentials

The application reads its database connection settings from environment variables:

- `MARKETSQUARE_DB_URL` — JDBC connection URL; defaults to `jdbc:mysql://localhost:3306/marketsquare?serverTimezone=UTC`.
- `MARKETSQUARE_DB_USER` — MySQL username.
- `MARKETSQUARE_DB_PASSWORD` — MySQL password.

For Windows PowerShell, set the variables in the terminal session used to start Tomcat:

```powershell
$env:MARKETSQUARE_DB_URL = "jdbc:mysql://localhost:3306/marketsquare?serverTimezone=UTC"
$env:MARKETSQUARE_DB_USER = "marketsquare_app"

$securePassword = Read-Host "Enter the database password" -AsSecureString
$env:MARKETSQUARE_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
```

Use the username and password configured for your own MySQL installation. The `marketsquare_app` username above is an example; create and configure that account with the database privileges the application requires before using it.

Keep passwords out of source code and Git commits. For additional JDBC and persistence details, see [`docs/jdbc-and-product-persistence.md`](docs/jdbc-and-product-persistence.md).

## Build and Test

Open a terminal in the directory containing `pom.xml`.

Run the automated tests:

```powershell
mvn clean test
```

Build the deployable WAR file:

```powershell
mvn clean package
```

A successful build generates:

`target/ecommerce-platform.war`

## Running the Application

1. Configure the Java, Tomcat, and database environment variables.
2. Build the application using Maven.
3. Deploy `target/ecommerce-platform.war` to the `webapps` directory of your Tomcat installation.
4. Start Tomcat and check its logs if startup or deployment fails.
5. Open the application in your browser:

   `http://localhost:8080/ecommerce-platform/`

The application context path may differ if the WAR file is deployed under a different name.

## Project Structure

```text
src/
├── main/
│   ├── java/com/ecommerce/
│   │   ├── controller/       # Servlet controllers
│   │   ├── db/               # Database connectivity
│   │   ├── exception/        # Application exceptions
│   │   ├── filter/           # Request filters
│   │   ├── model/            # Domain models
│   │   ├── repository/       # Data access and repositories
│   │   ├── security/         # Security utilities
│   │   └── service/          # Business logic
│   ├── resources/
│   │   └── db/migration/     # Database migration scripts
│   └── webapp/
│       ├── WEB-INF/
│       │   └── views/        # JSP views
│       └── assets/           # CSS and other frontend assets
└── test/                     # Automated tests
```

## Security and Data Handling

- Configure database credentials through environment variables.
- Use the application's existing authentication and authorization workflows.
- Preserve CSRF protections on applicable state-changing requests.
- Do not commit passwords, access tokens, or local environment files.
- Keep a database backup before applying schema changes.

## Current Limitations

- UPI Demo and Card Demo simulate payment outcomes; they are not connected to a real payment gateway.
- Successful compilation and unit tests do not replace end-to-end browser testing.
- Database setup and deployment must be configured for the local environment.

## License

Add the applicable project license here if one is required by the project or academic submission guidelines.
