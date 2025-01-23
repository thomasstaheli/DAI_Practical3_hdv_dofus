# Web Application README

## Project Overview
This web application is designed to provide a marketplace for users to trade items securely and efficiently. The application is built using Java, Javalin for the web framework, and SQLite for database management.

**Authors:**  
- Chef Léonard Jouve
- Ali Zoubir  
- Thomas Stäheli

---

## Installation and Setup

### Prerequisites
Ensure you have the following installed on your system:
- **Java (JDK 11 or later)**
- **Git**
- **Maven**
- **Docker & Docker Compose**

### Cloning the Repository
To clone the project, run the following command:

```bash
git clone <repository-url>
cd <repository-folder>
```

### Building the Project
Use Maven to build the application:

```bash
mvn clean package
```

### Running the Application
Start the application using:

```bash
java -jar target/web-application.jar --init --seed
```

---

## API Documentation
The web application provides the following endpoints:

### Authentication
- **POST** `/register` - Register a new user
- **POST** `/login` - Log in a user
- **GET** `/disconnect` - Log out a user

### User Management
- **GET** `/users` - Get all users
- **GET** `/users/me` - Get the authenticated user's information
- **PUT** `/users/me` - Update authenticated user's information
- **PATCH** `/users/me` - Partial update of authenticated user's information
- **DELETE** `/users/me` - Remove authenticated user
- **GET** `/users/{id}` - Get user by ID

### Marketplace (Hdv)
- **GET** `/hdv` - Get all offers
- **GET** `/hdv/me` - Get user's offers
- **GET** `/hdv/{id}` - Buy an offer
- **DELETE** `/hdv/{id}` - Remove an offer
- **POST** `/hdv` - Create a new offer
- **PATCH** `/hdv/{id}` - Update an offer
- **PUT** `/hdv/{id}` - Partially update an offer

---

## Using the Web Application

You can interact with the web application using `curl` commands:

### Example: Register a User
```bash
curl -X POST http://localhost:8080/register -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123"}'
```

**Response:**
```json
{"status":"ok"}
```

### Example: Login a User
```bash
curl -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123"}'
```

**Response:**
```json
{"status":"ok"}
```

---

## Virtual Machine Setup
To set up a virtual machine:

1. Install VirtualBox and Vagrant.
2. Clone the repository.
3. Run:
   ```bash
   vagrant up
   vagrant ssh
   ```

---

## DNS Configuration
To configure the DNS zone, add the following entries to your DNS provider:

```
A record -> example.com -> <server-ip>
CNAME record -> www.example.com -> example.com
```

---

## Deployment with Docker Compose

1. Install Docker and Docker Compose.
2. Clone the repository.
3. Run the following command:

```bash
docker-compose up --build -d
```

---

## Accessing the Application
Once the application is deployed, access it via:

```
http://example.com
```

---

## Domain Name Validation
To verify domain name configuration, use:

```bash
dig example.com +short
```

