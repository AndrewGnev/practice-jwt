# practice-jwt
2021 ITMO practice project. Authorization using JWT

Project consists of 3 applications: target api, authentication server and client and allows to send requests to target api with providing access by JWT.

## Requirements
- PostgreSQL
- Java 11+
- Maven

## Build

Execute the following command in each subdirectory of root:
```bash
mvn clean package
```

Target application will be in `target/` directories of each project.

## Run

To run any of applications you need to execute following command:
```bash
java -jar path/to/file.jar
```

## First start

After starting Auth server you need to create the first user and after that change it's role in DB table `user_roles` from `ROLE_USER` to `ROLE_ADMIN`.

## API

To use authentication add following header in request:
```
Authorization: Bearer <JWT access token>
```

### Auth server

- `POST /api/auth/login` - Create JWT tokens for user

Request body:
```json
{
    "username" : "username",
    "password" : "password"
}
```

Response body:
```json
{
    "accessToken" : "JWT access token",
    "refreshToken" : "JWT refresh token"
}
```

- `POST /api/auth/register/user` - Register new user with `ROLE_USER` role 

Request body:
```json
{
    "username" : "username",
    "password" : "password"
}
```

Responce body
```json
{
    "username" : "username"
}
```

- `POST /api/auth/register/admin` - Register new user with `ROLE_ADMIN` role, needs authentication

Request body:
```json
{
    "username" : "username",
    "password" : "password"
}
```

Responce body
```json
{
    "username" : "username"
}
```

- `POST /api/auth/refresh` - Update access and refresh tokens, needs authentication

Request body:
```json
{
    "refreshToken" : "JWT refresh token"
}
```

Response body:
```json
{
    "accessToken" : "JWT access token",
    "refreshToken" : "JWT refresh token"
}
```

- `POST /api/auth/logout` - Delete user`s refresh token, needs authentication

Request body:
```json
{
    "username" : "username"
}
```

Response body is empty (void method)

### Target api

- `GET /api/endpoint0` - endpoint with access for all roles

- `GET /api/endpoint1` - endpoint with access for ROLE_ADMIN role

- `GET /api/endpoint2` - endpoint without access for all roles

### Client commands

- `login` - send login request to auth server, requests username and password from user
- `register` - send register user or register admin request to auth server, requests new user role, username and password from user. New admin registration is allowed only after login as admin
- `logout` - send logout request to auth server

- `everybodyEndpoint` - send request to 0-th endpoint of target api
- `adminsOnlyEndpoint` - send request to 1-st endpoint of target api
- `nobodyEndpoint` - send request to 2-th endpoint of target api

- `quit` - stop using and turn off the client
