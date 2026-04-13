# Smart Parking System

A **Java Servlet** developed as a part of JAVA and Database Management System course.
A very simple coding and design pattern has ben used to autoate the browsing through the parking lots, checking out empty spots, finding out whether it is available for advance booking, etc and fare calculation.

## Technologies used:
- Front end: HTML5, Javascript, Bootstrap and CSS3
- Validations: JavaScript
- Back end: Java Servlets
- Database: MySQl
- Server :  Apache Tomcat

## Runtime configuration

Database access is now configured via environment variables instead of hardcoded credentials:

- `DB_URL` (default: `jdbc:mysql://localhost:3306/parking_system_db`)
- `DB_USER` (default: `parking_app`)
- `DB_PASSWORD` (required; default placeholder is rejected at runtime)

## Security hardening

- Passwords are now stored using PBKDF2 hashing.
- State-changing actions use POST with CSRF token validation.
- User-facing dynamic content is HTML-escaped in servlet responses.
- SQL statements with request/session inputs use parameterized queries.

## Features

- **Login Register**: Users and parking lot owners can register into the portal and will be allowed to browse through the list of places-> parking lots-> parking spots

- **Current Booking** : Users can check out the availibilty of empty spots currently and book the spot immedietly.

- **Future Booking**:  If a user needs an advance booking, he can do so by booking the car/bike from a fixed time to a fixed time.

- **Fare calculation**: The fare is automatically calculated as per the time booked and per hour charge and is generated at the time of leaving.
