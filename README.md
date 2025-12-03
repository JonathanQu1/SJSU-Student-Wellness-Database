# SJSU Student Wellness Database – Console App (JDBC)

Group: Huu Tinh Nguyen, Amy Okuma, Linh Pham, Jonathan Qu  
Course: CS 157A – Sec 01  
Instructor: Prof. Ethel Tshukudu

This project is a **menu-driven Java console application** that connects to a **MySQL database** using **JDBC**.  
It models a Student Wellness / Counseling system with students, counselors, self-assessments, referrals, appointments, and feedback.

The app can:

- View students, counselors, and appointments
- Update appointment status
- Insert a new self-assessment + referral in **one transaction** (COMMIT / ROLLBACK)
- Delete a student (with `ON DELETE CASCADE` taking care of related rows)

---

## 1. Tech Stack

- **Java:** JDK 17 (or 21)
- **MySQL Server:** 8.0.x
- **MySQL Workbench:** 8.0.x
- **MySQL Connector/J:** 8.0.x (JDBC driver)
- **IDE (used):** IntelliJ IDEA

You can use any Java IDE as long as you can:

- Add the MySQL connector JAR to the classpath
- Run a simple `public static void main(String[] args)` console app

---

## 2. Configure database connection

This project reads database settings from `db.properties` in the project root.

Before running the app, open `db.properties` and update the values to match your MySQL setup:

```properties
db.url=jdbc:mysql://localhost:3306/sjsustudentwellnessdb  # change schema name if needed
db.user=your_mysql_username
db.password=your_mysql_password
```

## 3. Database Setup (MySQL)

### 3.1. Create the database

In MySQL Workbench (or any MySQL client), run:
```sql
CREATE DATABASE sjsustudentwellnessdb;
USE sjsustudentwellnessdb;
```

To create all tables and insert sample data, run the SQL script
create_and_populate.sql in MySQL Workbench.