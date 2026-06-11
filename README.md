# 🌸 Todo Petal - Task Management System

A desktop-based productivity application developed using **Java Swing** and **MySQL** to help users efficiently manage tasks, track goals, organize schedules, save contacts, and securely store passwords.

**Developed by:** Nafeesathul Misriya & Ishan Thomas Eapen

**Lead Developer:** Nafeesathul Misriya
**Project Collaborator:** Ishan Thomas Eapen

## 📌 Project Overview

Todo Petal is an all-in-one personal productivity platform designed to simplify daily task management through a clean and intuitive interface. The application allows users to manage tasks, track long-term goals, organize schedules, store contacts, and securely save passwords in a single standalone desktop application.

The project was developed as part of the B.Tech Computer Science and Engineering curriculum and demonstrates practical implementation of Java GUI development, database management, and software engineering principles.

---

## ✨ Features

### 👤 User Management
- User Registration
- Secure Login System
- Profile Management
- Role-Based Access Control (Admin/User)

### ✅ Task Management
- Add Tasks
- Edit Tasks
- Delete Tasks
- Mark Tasks as Completed
- Track Task Status
- Organize Daily Activities

### 🎯 Goal Tracking
- Create Long-Term Goals
- Monitor Progress
- Visual Progress Indicators

### 📅 Calendar Management
- Monthly Calendar View
- Schedule Tracking
- Deadline Monitoring

### 🔐 Password Saver
- Store Login Credentials
- Manage Saved Passwords
- Secure Data Storage

### 📞 Contact Manager
- Save Important Contacts
- Update Contact Information
- Quick Access to Contacts

### 🛠️ Admin Dashboard
- View Registered Users
- Manage User Accounts
- Monitor System Data

---

## 🏗️ System Architecture

The application follows a Three-Tier Architecture:

### Presentation Layer
- Java Swing GUI
- User Interaction Components
- Dashboard and Forms

### Application Logic Layer
- Core Java
- Event Handling
- Business Logic
- Data Validation

### Data Storage Layer
- MySQL Database
- JDBC Connectivity
- Persistent Data Storage

---

## 🧰 Technologies Used

| Technology | Purpose |
|------------|----------|
| Java | Application Development |
| Java Swing | GUI Design |
| MySQL | Database Management |
| JDBC | Database Connectivity |
| VS Code | Development Environment |
| Git & GitHub | Version Control |

---

## 🗄️ Database Structure

### Main Tables

```sql
users
tasks
goals
contacts
passwords
```

### Relationships

- One User → Many Tasks
- One User → Many Goals
- One User → Many Contacts
- One User → Many Saved Passwords

---

## 🚀 Installation & Setup

### Prerequisites

- Java JDK 17 or above
- MySQL Server
- MySQL Connector/J
- Visual Studio Code (Optional)

### Clone Repository

```bash
git clone https://github.com/MISRIYA-7/TodoPetal-Task-Management-System.git
cd TodoPetal-Task-Management-System
```

### Create Database

```sql
CREATE DATABASE topetal;
USE topetal;
```

### Configure Database Connection

```java
String url = "jdbc:mysql://localhost:3307/topetal";
String username ="your_userame";
String password = "your_password";
```

### Run Application

```bash
javac *.java
java Main
```

---

## 📸 Screenshots

### Welcome / Login Page
<img width="1366" alt="Welcome Login Page" src="https://github.com/user-attachments/assets/05069d8c-0efe-472c-92bd-944f12e7680c" />

### Admin Dashboard
<img width="1366" alt="Admin Dashboard" src="https://github.com/user-attachments/assets/7aa25506-cc88-45ca-8619-0faa17b2c38f" />

### User Dashboard
<img width="1366" alt="User Dashboard" src="https://github.com/user-attachments/assets/4e053bde-cc85-4fef-b93b-a675d50fcacb" />

### Task Management View
<img width="1366" alt="Task Management" src="https://github.com/user-attachments/assets/ad5816a9-6bc7-4c68-ac76-144024b31723" />

### User Settings
<img width="1366" alt="User Settings" src="https://github.com/user-attachments/assets/e417c81d-5e3f-4d3f-bedd-9f9499434961" />

---

## 👩‍💻 Lead Developer

### Nafeesathul Misriya

- Designed and developed the complete Java Swing user interface.
- Implemented authentication and authorization modules.
- Designed and integrated the MySQL database.
- Developed JDBC connectivity and database operations.
- Implemented CRUD functionalities for tasks, goals, contacts, and passwords.
- Developed Admin and User dashboards.
- Performed testing, debugging, and deployment.

### Skills Demonstrated

- Java Programming
- Object-Oriented Programming (OOP)
- Java Swing
- MySQL
- JDBC
- Database Design
- Software Development
- Git & GitHub

---

## 🤝 Team Members

### Nafeesathul Misriya 
**Lead Developer**

### Ishan Thomas Eapen 
**Project Collaborator**

- Documentation
- System Analysis
- Testing & Validation
- Project Support

---

## 🎓 Academic Information

**Project:** Todo Petal – Task Management System

**Course:** B.Tech Computer Science and Engineering

**Institution:** SCMS School of Engineering and Technology

**University:** APJ Abdul Kalam Technological University (KTU)

**Academic Year:**  2024–2025

---

## 🔮 Future Enhancements

- Cloud Backup & Synchronization
- Desktop Notifications
- Dark Mode & Theme Customization
- Multi-User Collaboration
- Mobile Application Version
- Advanced Analytics Dashboard

---
## 🌐 Connect With Me

### Nafeesathul Misriya

- GitHub: https://github.com/MISRIYA-7
- LinkedIn: https://www.linkedin.com/in/nafeesathul-misriya7
- Portfolio: https://misriya-7.github.io/personal-portfolio-website/

---

## 📄 License

This project was developed for educational and academic purposes.

---

## 🙏 Acknowledgements

Special thanks to the faculty members and mentors of the Department of Computer Science and Engineering, SCMS School of Engineering and Technology, for their guidance and support throughout the project.

---

⭐ If you found this project interesting, consider giving it a star!
