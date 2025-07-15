# Middleware-CBS

Middleware-CBS is a Spring Boot-based middleware application designed to interact with a Core Banking System (CBS). It provides RESTful APIs for consulting account details, customer information, transaction history, and performing fund transfers between accounts.

## Features

- **Consult Account**: Retrieve account details by account ID.
- **Consult Customer**: Retrieve customer details by customer ID.
- **Consult Transaction History**: Retrieve the last 10 transactions for an account.
- **Perform Fund Transfer**: Execute a transfer between accounts and update balances.

## Technologies Used

- **Java**: Programming language.
- **Spring Boot**: Framework for building the application.
- **Maven**: Build and dependency management tool.
- **OpenAPI**: API documentation using `api.yaml`.
- **RestTemplate**: For making HTTP requests to the CBS.

## Project Structure

- `src/main/java/tn/ucar/enicar/middleware/controller`: Contains REST controllers for handling API requests.
- `src/main/java/tn/ucar/enicar/middleware/client`: Contains the `CbsClient` class for interacting with the CBS.
- `src/main/java/tn/ucar/enicar/middleware/model`: Contains data models such as `Account`, `Customer`, `Transaction`, `TransferRequest`, and `TransferResponse`.
- `src/main/resources/api.yaml`: OpenAPI specification for the middleware APIs.

## API Endpoints

### **Consult Account**
- **URL**: `/api/consult-account`
- **Method**: `GET`
- **Parameters**: `id` (string) - Account ID
- **Response**: Account details

### **Consult Customer**
- **URL**: `/api/consult-customer`
- **Method**: `GET`
- **Parameters**: `id` (string) - Customer ID
- **Response**: Customer details

### **Consult Transaction History**
- **URL**: `/api/consult-history`
- **Method**: `GET`
- **Parameters**: `id` (string) - Account ID
- **Response**: List of transactions

### **Perform Fund Transfer**
- **URL**: `/api/do-transfer`
- **Method**: `POST`
- **Request Body**: `TransferRequest` object
- **Response**: `TransferResponse` object

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Middleware-CBS.git
