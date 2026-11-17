# HospitaCare — Hospital Management System

A desktop Hospital Management System built with **Java 17**, **Java Swing**, **JDBC**, and **MySQL**. Designed with a clean layered architecture (Model-DAO-Service-UI), the application provides a modern dashboard, patient registration, physician records management, and conflict-free appointment scheduling.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
3. [Technologies Used](#technologies-used)
4. [Project Architecture](#project-architecture)
5. [Database Schema](#database-schema)
6. [Prerequisites](#prerequisites)
7. [MySQL Setup Instructions](#mysql-setup-instructions)
8. [Database Configuration](#database-configuration)
9. [IntelliJ IDEA Setup](#intellij-idea-setup)
10. [How to Run](#how-to-run)
11. [Screenshots](#screenshots)
12. [Future Enhancements](#future-enhancements)

---

## Project Overview

**HospitaCare** is designed for clinic and hospital administration. It streamlines front-desk operations by providing:
- Fast patient registration and lookup.
- Doctor profiles categorized by medical specialization.
- Intelligent appointment booking with automatic conflict detection.
- A live administrative dashboard tracking key operational metrics.

---

## Key Features

### 1. 📊 Executive Dashboard
- **Dynamic Metric Cards**: Real-time counts for Total Patients, Total Doctors, Total Appointments, and Today's Consultations.
- **Today's Schedule Table**: Quickly view patients scheduled for the current day.
- **Upcoming Appointments Table**: Chronological list of upcoming scheduled visits.
- **Asynchronous Data Loading**: Powered by `SwingWorker` to prevent UI freezing during database queries.

### 2. 👥 Patient Management (Full CRUD)
- **Register Patients**: Collects full name, age, gender (Male, Female, Other), and phone number.
- **Search**: Instant search by patient ID or partial name matching.
- **Update Records**: Edit patient demographics and contact details.
- **Safe Deletion**: Protects medical history by preventing deletion of patients who have active or historical appointments.

### 3. 👨‍⚕️ Doctor Management (Full CRUD)
- **Physician Directory**: Manages doctor name, clinical specialization, phone, and email.
- **Filter & Search**: Search by doctor name or filter by specialization.
- **Validation**: Enforces valid phone numbers and email formatting.
- **Referential Integrity**: Prevents accidental deletion of doctors who have existing scheduled appointments.

### 4. 📅 Appointment Scheduling
- **Conflict Prevention**: Automatically prevents double-booking a doctor for the same date and time slot.
- **Past Date Validation**: Prevents scheduling appointments in the past.
- **Dynamic Dropdowns**: Populates patients and doctors directly from MySQL.
- **Status Lifecycle**: Supports `SCHEDULED`, `COMPLETED`, and `CANCELLED` statuses.
- **Historical Integrity**: Cancelling an appointment updates its status rather than deleting the audit trail.
- **Multi-Filter Engine**: Filter appointments by date, status, or keyword.

---

## Technologies Used

- **Language**: Java 17 (LTS)
- **User Interface**: Java Swing (Custom professional navy theme)
- **Database**: MySQL 9.5 / 8.0+
- **Connectivity**: Java Database Connectivity (JDBC) via `mysql-connector-j:9.5.0`
- **Build & Dependency Management**: Apache Maven
- **Architecture**: Layered architecture (Model-DAO-Service-UI)

---

## Project Architecture

```
com.hospital
├── Main.java                        # Application entry point (EDT launcher)
├── TestConnection.java              # Database connectivity test utility
├── config/
│   └── DatabaseConnection.java      # JDBC connection manager & schema initializer
├── model/
│   ├── Patient.java                 # Patient entity POJO
│   ├── Doctor.java                  # Doctor entity POJO
│   └── Appointment.java             # Appointment entity POJO with joined view fields
├── dao/
│   ├── PatientDAO.java              # JDBC PreparedStatements for patients table
│   ├── DoctorDAO.java               # JDBC PreparedStatements for doctors table
│   └── AppointmentDAO.java          # JDBC PreparedStatements for appointments table
├── service/
│   ├── PatientService.java          # Business logic, validation & deletion checks
│   ├── DoctorService.java           # Specialization validation & doctor rules
│   └── AppointmentService.java       # Conflict detection, time & date validation
└── ui/
    ├── UITheme.java                 # Centralized color palette, fonts, table styling
    ├── MainFrame.java               # Root window with CardLayout navigation
    ├── DashboardPanel.java          # Live statistics and appointment overviews
    ├── PatientPanel.java            # Patient search, table, and modal dialogs
    ├── DoctorPanel.java             # Doctor search, table, and modal dialogs
    ├── AppointmentPanel.java        # Booking forms, filters, and status management
    └── components/
        ├── Sidebar.java             # Dark navy navigation drawer
        └── StatCard.java            # Styled dashboard metric cards
```

---

## Database Schema

Located in `src/main/resources/schema.sql`:

```sql
CREATE DATABASE IF NOT EXISTS hospital_management;
USE hospital_management;

-- 1. Patients Table
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    gender VARCHAR(10),
    phone VARCHAR(15)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Doctors Table
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Appointments Table
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(255),
    CONSTRAINT fk_appointment_patient 
        FOREIGN KEY (patient_id) REFERENCES patients(patient_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_appointment_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_appointment_status (status),
    INDEX idx_appointment_conflict (doctor_id, appointment_date, appointment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## Prerequisites

1. **Java Development Kit (JDK)**: Version 17 or higher.
2. **MySQL Server**: Version 8.0 or 9.5 running locally or on a network host.
3. **IDE**: IntelliJ IDEA (Community or Ultimate) or Eclipse / VS Code.

---

## MySQL Setup Instructions

1. Start your local MySQL server (via MySQL Workbench, Homebrew, or system service).
2. Log in to the MySQL CLI:
   ```bash
   mysql -u root -p
   ```
3. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS hospital_management;
   ```
4. Tables will be **automatically created** when you launch the application for the first time via `DatabaseConnection.initializeDatabase()`. Alternatively, you can run the commands in `src/main/resources/schema.sql` manually.

---

## Database Configuration

1. In `src/main/resources/`, ensure a file named `db.properties` exists:
   ```properties
   db.url=jdbc:mysql://localhost:3306/hospital_management
   db.username=YOUR_MYSQL_USERNAME
   db.password=YOUR_MYSQL_PASSWORD
   ```
2. Replace `YOUR_MYSQL_USERNAME` and `YOUR_MYSQL_PASSWORD` with your actual MySQL credentials.
3. *Note*: `db.properties` is already listed in `.gitignore` to prevent committing sensitive passwords to version control. An example template is provided in `src/main/resources/db.properties.example`.

---

## IntelliJ IDEA Setup

1. Open **IntelliJ IDEA**.
2. Select **File > Open...** and select the `HospitalManagementSystem` directory.
3. Allow IntelliJ to import the project as a **Maven** project.
4. Go to **File > Project Structure > Project**:
   - Ensure the **SDK** is set to **Java 17**.
   - Language level: **17 - Sealed types, always-strict floating-point, etc.**
5. Open the Maven tool window (right sidebar) and click the **Reload All Maven Projects** icon (🔄).

---

## How to Run

### Step 1: Verify Database Connection (Optional but Recommended)
1. Open `src/main/java/com/hospital/TestConnection.java`.
2. Click the green **Run (▶)** button next to `main(String[] args)`.
3. Confirm that the console prints:
   ```
   ✅ Connection Successful!
   Database Product : MySQL
   ...
   ```

### Step 2: Launch the Application
1. Open `src/main/java/com/hospital/Main.java`.
2. Click the green **Run (▶)** button next to `main(String[] args)` (or press `Shift + F10` / `Ctrl + R`).
3. The desktop GUI will open centered on your display.

---

## Screenshots

> *Placeholder: Add screenshots of the running application below for presentation.*

| Dashboard Overview | Patient Management |
| :---: | :---: |
| `[Screenshot Placeholder: Dashboard Panel with Stat Cards and Schedule Tables]` | `[Screenshot Placeholder: Patient Panel with Search and Modal Dialog]` |

| Doctor Directory | Appointment Scheduler |
| :---: | :---: |
| `[Screenshot Placeholder: Doctor Panel with Specialization Filter]` | `[Screenshot Placeholder: Appointment Booking Form and Conflict Validation]` |

---

## Future Enhancements

- [ ] **Role-Based Authentication**: Secure login screen for Administrators, Receptionists, and Doctors.
- [ ] **Billing & Invoicing**: Automated invoice calculation based on consultation fees and diagnostic tests.
- [ ] **Prescription Tracking**: Digital prescription records attached to completed appointments.
- [ ] **PDF Export**: Export patient medical summaries and appointment slips.
- [ ] **Dark / Light Mode Toggle**: User-selectable GUI theme settings.

