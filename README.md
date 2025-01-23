# Web Application README

## Project Overview
This web application is designed to provide a marketplace for users to trade items securely and efficiently. The application is built using Java, Javalin for the web framework, and SQLite for database management.

**Authors:**  
- Léonard Jouve
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
git clone https://github.com/thomasstaheli/DAI_Practical3_hdv_dofus.git
cd DAI_Practical3_hdv_dofus
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

The API supports the following operations:

- Authentication: Register, login, and logout users.
- User Management: Retrieve all users, get details of the current user or a specific user, update user details (partially or fully), and delete the current user's account.
- Auction House (HDV): Retrieve all offers, view the current user's offers, create new offers, update or partially update existing offers, buy items, and remove offers.
- User Inventory : Retrieve, add, update and remove item from user's inventory

## Endpoints

### Register a new user

- **`POST /register`**

Registers a new user.

#### Request

The request body must contain a JSON object with the following properties:

- `username` - The username of the user (string, required)
- `password` - The password of the user (string, required)

#### Response

The response body contains a status object:

- `status` - A message indicating the operation was successful

#### Status codes

- `201` (Created) - The user has been successfully registered
- `409` (Conflict) - The username is already taken

---

### Login a user

- **`POST /login`**

Logs in a user.

#### Request

The request body must contain a JSON object with the following properties:

- `username` - The username of the user (string, required)
- `password` - The password of the user (string, required)

#### Response

A `token` cookie is set for authentication. The response body contains a status object:

- `status` - A message indicating the operation was successful

#### Status codes

- `200` (OK) - The user has been successfully logged in
- `401` (Unauthorized) - Invalid credentials

---

### Disconnect a user

- **`GET /disconnect`**

Logs out the currently authenticated user.

#### Request

No request body is required.

#### Response

The `token` cookie is removed. The response body contains a status object:

- `status` - A message indicating the operation was successful

#### Status codes

- `200` (OK) - The user has been successfully logged out

---

### Users

### Get all users

- **`GET /users`**

Retrieves a list of all users.

#### Request

No request body is required.

#### Response

The response body contains a JSON array of user objects:

- `id` - The unique identifier of the user
- `username` - The username of the user
- `kamas` - The user’s in-game currency balance

#### Status codes

- `200` (OK) - Users have been successfully retrieved

---

### Get current user

- **`GET /users/me`**

Retrieves the profile of the currently authenticated user.

#### Request

No request body is required.

#### Response

The response body contains a JSON object with the following properties:

- `id` - The unique identifier of the user
- `username` - The username of the user
- `kamas` - The user’s in-game currency balance

#### Status codes

- `200` (OK) - The user has been successfully retrieved

---

### Update current user partially

- **`PUT /users/me`**

Updates the current user’s profile partially.

#### Request

The request body can contain any combination of the following properties:

- `username` - The new username of the user (string, optional)
- `password` - The new password of the user (string, optional)

#### Response

The response body contains a status object:

- `status` - A message indicating the operation was successful

#### Status codes

- `200` (OK) - The user has been successfully updated

---

### Update current user fully

- **`PATCH /users/me`**

Updates the current user’s profile fully.

#### Request

The request body must contain the following properties:

- `username` - The new username of the user (string, required)
- `password` - The new password of the user (string, required)

#### Response

The response body contains a status object:

- `status` - A message indicating the operation was successful

#### Status codes

- `200` (OK) - The user has been successfully updated

---

### Delete current user

- **`DELETE /users/me`**

Deletes the profile of the currently authenticated user.

#### Request

No request body is required.

#### Response

The response body contains a status object:

- `status` - A message indicating the operation was successful

#### Status codes

- `200` (OK) - The user has been successfully deleted

---

### Get a specific user by ID

- **`GET /users/{id}`**

Retrieves a specific user by their ID.

#### Request

The request path must include the ID of the user.

#### Response

The response body contains a JSON object with the following properties:

- `id` - The unique identifier of the user
- `username` - The username of the user
- `kamas` - The user’s in-game currency balance

#### Status codes

- `200` (OK) - The user has been successfully retrieved
- `404` (Not Found) - The user does not exist

---

### Get User Inventory

- `GET /myinventory`

Retrieve the list of items in the current user's inventory.

#### Request

No request body is required.

#### Response

The response body contains a JSON array of objects with the following properties:

- `id` - The unique identifier of the item
- `nom` - The name of the item
- `quantity` - The quantity of the item in the inventory

#### Status Codes

- `200` (OK) - The inventory was successfully retrieved
- `404` (Not Found) - No items were found in the inventory

---

### Add Item to Inventory

- `POST /myinventory`

Add a new item to the current user's inventory.

#### Request

The request body must contain a JSON object with the following properties:

- `item_id` - The unique identifier of the item
- `quantity` - The quantity of the item to add (must be greater than 0)

#### Response

The response body contains a JSON object indicating success.

#### Status Codes

- `201` (Created) - The item was successfully added to the inventory
- `400` (Bad Request) - The request body is invalid
- `409` (Conflict) - The item already exists in the inventory

---

### Delete Item from Inventory

- `DELETE /myinventory/{item_id}`

Remove an item from the current user's inventory.

#### Request

The request path must include the `item_id` of the item to delete.

#### Response

The response body contains a JSON object indicating success.

#### Status Codes

- `200` (OK) - The item was successfully removed from the inventory
- `404` (Not Found) - The item does not exist in the inventory

---

### Update Item Quantity in Inventory

- `PATCH /myinventory/{item_id}`
- `PUT /myinventory/{item_id}`

Update the quantity of an item in the current user's inventory.

#### Request

The request path must include the `item_id` of the item to update.

The request body must contain a JSON object with the following property:

- `quantity` - The new quantity of the item (must be greater than 0)

#### Response

The response body contains a JSON object indicating success.

#### Status Codes

- `200` (OK) - The item quantity was successfully updated
- `400` (Bad Request) - The request body is invalid
- `404` (Not Found) - The item does not exist in the inventory

---

### HDV

#### **Get All Offers**
**`GET /hdv`**
- **Description**: Retrieve all offers available in the system.
- **Response**: A list of all offers.

##### Example Response:
```json
[
  {
    "offerId": 1,
    "itemId": 42,
    "userId": 5,
    "price": 1000,
    "amount": 10
  },
  {
    "offerId": 2,
    "itemId": 43,
    "userId": 6,
    "price": 1500,
    "amount": 5
  }
]
```

---

#### **Get User's Offers**
**`GET /hdv/me`**
- **Description**: Retrieve all offers created by the authenticated user.
- **Response**: A list of the user's offers.

---

#### **Buy an Offer**
**`GET /hdv/{id}`**
- **Description**: Purchase an item from an offer by its ID.
- **Parameters**:
  - `id` (path): The ID of the offer to purchase.
- **Response**: A success status if the purchase is completed.
- **Errors**:
  - Unauthorized if the user does not have enough funds.
  - Offer not found.

---

#### **Remove an Offer**
**`DELETE /hdv/{id}`**
- **Description**: Remove an offer by its ID. The offer must belong to the authenticated user.
- **Parameters**:
  - `id` (path): The ID of the offer to delete.
- **Response**: A success status if the offer is deleted.
- **Errors**:
  - Unauthorized if the offer does not belong to the user.
  - Offer not found.

---

#### **Create a New Offer**
**`POST /hdv`**
- **Description**: Create a new offer.
- **Request Body**:
  - `itemId` (integer, required): The ID of the item.
  - `price` (integer, required): The price of the item in kamas.
  - `amount` (integer, required): The quantity of the item.
- **Response**: A success status if the offer is created.
- **Errors**:
  - Unauthorized if the user does not have enough inventory.

##### Example Request:
```json
{
  "itemId": 42,
  "price": 1000,
  "amount": 10
}
```

---

#### **Update an Offer**
**`PATCH /hdv/{id}`**
- **Description**: Update an existing offer by its ID. The offer must belong to the authenticated user.
- **Parameters**:
  - `id` (path): The ID of the offer to update.
- **Request Body**:
  - `price` (integer, optional): The new price of the item.
  - `amount` (integer, optional): The new quantity of the item.
- **Response**: A success status if the offer is updated.
- **Errors**:
  - Unauthorized if the offer does not belong to the user.
  - Offer not found.

##### Example Request:
```json
{
  "price": 1200,
  "amount": 8
}
```

---

#### **Replace an Offer**
**`PUT /hdv/{id}`**
- **Description**: Fully replace an existing offer by its ID. The offer must belong to the authenticated user.
- **Parameters**:
  - `id` (path): The ID of the offer to replace.
- **Request Body**:
  - `price` (integer, required): The new price of the item.
  - `amount` (integer, required): The new quantity of the item.
- **Response**: A success status if the offer is replaced.
- **Errors**:
  - Unauthorized if the offer does not belong to the user.
  - Offer not found.

##### Example Request:
```json
{
  "price": 1500,
  "amount": 5
}
```

---

### Error Handling
Common errors include:
- **UnauthorizedResponse**: User is not authenticated or lacks the required permissions.
- **Not Found**: The requested resource (e.g., offer) does not exist.

### Notes

- All endpoints require authentication to identify the current user.
- The `auth.getMe(ctx)` function retrieves the authenticated user's ID.
- Errors are handled with appropriate HTTP status codes and descriptive error messages.


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

