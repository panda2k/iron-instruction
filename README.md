# Iron Instruction: A Platform for Strength Coaches and Athletes
Want to view the live site? Click [here](https://iron-instruction.vercel.app).

## Overview
Typically, strength coaches develop their training programs in spreadsheet applications
like Excel and Google Sheets. However, using a spreadsheet program requires 
a lot of initial setup and the end result is not very aesthetic or user
friendly.

As a result, Iron Instruction was designed to help strength coaches develop training
programs in a more aesthetic and user friendly way. 

## Testing the Site
Feel free to visit the [site](https://iron-instruction.vercel.app)
and test it your own way. However, if you'd like to interact with pre-setup user
accounts, login with the following test accounts: 

#### Test Coach Account
Email: `testcoach@gmail.com`

Password: `password`

#### Test Athlete Account
Email: `testathlete@gmail.com`

Password: `password`

## Authentication System
Iron Instruction uses [JWTs](https://jwt.io) (JSON Web Tokens)
for user authentication. When logging in, the client saves a short lived 
access token (10 minutes) and a long lived refresh token (30 days).
The access token is sent with every request and when it expires, the refresh 
token is exchanged for a new access and refresh token.

## Tech Stack Explained 
### Backend
Iron Instruction's backend is built with [Spring Boot](https://spring.io/projects/spring-boot).
User data is stored in [MongoDB](https://www.mongodb.com) and user uploaded 
videos are stored in [AWS S3](https://aws.amazon.com/s3/) buckets. 

The backend is built with Spring Boot because it is sensibly opinionated and 
comes with excellent built in request type casting. It requires minimal configuration 
and works pretty much out of the box.

MongoDB was chosen over postgreSQL because it has a faster development process.
Data models are flexible and the database doesn't require mass amounts of 
reconfiguration when changing data structure. 

User uploaded videos are uploaded and served with presigned URLs. This allows
videos to be relatively private and only visible to the coach and athlete.

### Frontend
Iron Instruction's frontend is built with [NextJS](https://nextjs.org).

I chose NextJS because it requires minimal boilerplate and setup 
compared to Create React App. Next's preconfigured router, minimal configuration, 
and out of the box TypeScript support made it an easy choice. 

## API Testing
The backend's security and CRUD methods are fully tested using JUnit 
assertions and Spring Boot's Mock MVC. 

Tests can be run with `mvn test`. Every time code is pushed to `main`, a
Github action runs `mvn test`.

## Assets
Website icons are provided by [Heroicons](https://heroicons.com)

