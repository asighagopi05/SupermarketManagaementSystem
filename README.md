# Supermarket Management System

A desktop-based Supermarket Management System built with **Java Swing** and **Oracle Database**, designed to manage day-to-day supermarket operations efficiently.

## Features

- **Login Module** — Secure role-based authentication for Admin and Cashier
- **Admin Dashboard** — Full control panel for store management
- **Cashier Dashboard** — POS interface for billing and transactions
- **Product Module** — Add, update, and manage product catalog
- **Inventory Module** — Track stock levels and manage inventory
- **Billing Module** — Generate bills and process customer purchases
- **Payment Module** — Handle payment transactions
- **Customer Module** — Manage customer records
- **Employee Module** — Manage employee information
- **Supplier Module** — Manage supplier details and orders
- **Department Module** — Organize store departments
- **Store Module** — Store configuration and settings
- **Returns Module** — Handle product returns
- **Report Module** — Generate sales and inventory reports
- **User Module** — Manage system users and roles

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java |
| UI Framework | Java Swing |
| Database | Oracle Database |
| JDBC Driver | Oracle JDBC (`ojdbc`) |

## Prerequisites

- Java JDK 8 or higher
- Oracle Database (XE or full) running on `localhost:1521`
- Oracle JDBC driver (`ojdbc.jar`)

## Database Setup

The application connects to Oracle DB with the following default config (see `DBConnection.java`):

```
Host     : localhost
Port     : 1521
Service  : orcl1
Username : system
Password : pass#123
```

Update `src/smms/DBConnection.java` if your Oracle credentials differ.

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/asighagopi05/SupermarketManagaementSystem.git
   ```
2. Open the project in your Java IDE (Eclipse / IntelliJ / NetBeans)
3. Add the Oracle JDBC driver (`ojdbc.jar`) to the build path
4. Make sure Oracle Database is running
5. Run `LoginModule.java` as the main entry point

## Project Structure

```
SMMS/
├── src/
│   └── smms/
│       ├── LoginModule.java
│       ├── AdminDashboard.java
│       ├── CashierDashboard.java
│       ├── ProductModule.java
│       ├── InventoryModule.java
│       ├── BillingModule.java
│       ├── PaymentModule.java
│       ├── CustomerModule.java
│       ├── EmployeeModule.java
│       ├── SupplierModule.java
│       ├── DeptModule.java
│       ├── StoreModule.java
│       ├── ReturnsModule.java
│       ├── ReportModule.java
│       ├── UserModule.java
│       └── DBConnection.java
└── README.md
```

## License

This project is open source and available under the [MIT License](LICENSE).
