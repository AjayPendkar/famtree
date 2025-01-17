# Family Tree Management System

A Spring Boot application for managing family trees and member relationships.

## Features

### 1. User Roles
- **SUPER_ADMIN**: System administrator with full access
- **ADMIN**: Can manage users and families
- **HEAD**: Family head who can manage their family
- **MEMBER**: Regular family member

### 2. Authentication
- Admin/Super Admin: Email & Password based
- Family Head/Member: Mobile & OTP based

### 3. Family Management
- Complete family registration
- Add/Update family members
- Manage pending member invitations
- Update family profiles

### 4. Member Management
- Member registration with verification
- Member profile updates
- Role-based access control
- Family code verification for new members

### 5. Admin Features
- View all users
- View users by role
- Delete users
- Update user roles
- View all families
- Delete families

## API Endpoints

### Authentication
- `/api/auth/admin/register`: Register admin users
- `/api/auth/admin/login`: Admin login
- `/api/auth/send-otp`: Send OTP for mobile verification
- `/api/auth/verify-otp`: Verify OTP

### Family Management
- `/api/families/complete-registration`: Complete family registration
- `/api/families/update-profile`: Update family profile
- `/api/families/heads`: Get all family heads

### Member Management
- `/api/members/register`: Register new member
- `/api/members/verify`: Verify member
- `/api/members/verify-family-code`: Verify family invitation code
- `/api/members/pending`: Get pending member invitations

### Admin Operations
- `/api/admin/users`: Manage users
- `/api/admin/families`: Manage families

## Security
- JWT based authentication
- Role-based access control
- OTP verification for mobile users
- Password encryption for admin users

## Database Schema
- Users table with role constraints
- Family relationships
- Pending member management
- Token and OTP handling

## Technologies
- Spring Boot
- Spring Security
- PostgreSQL
- JWT Authentication
- JPA/Hibernate
