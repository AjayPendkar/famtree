package com.famtree.famtree.docs;

/**
 * Family Tree Management System - Development Documentation
 * 
 * 1. Authentication Flow
 * ----------------------
 * 
 * 1.1 Initial Registration:
 * - User sends mobile, role, familyName
 * - System generates OTP
 * - Creates unverified user
 * Example:
 * POST /api/auth/send-otp
 * {
 *     "mobile": "9059050018",
 *     "role": "HEAD",
 *     "familyName": "Pendkar"
 * }
 * 
 * 1.2 Login Flow:
 * - User sends only mobile
 * - System checks if user exists
 * - Generates OTP for existing user
 * Example:
 * POST /api/auth/send-otp
 * {
 *     "mobile": "9059050018"
 * }
 * 
 * 1.3 OTP Verification:
 * - Verifies OTP
 * - Returns JWT token
 * Example:
 * POST /api/auth/verify-otp
 * {
 *     "mobile": "9059050018",
 *     "otp": "123456"
 * }
 * 
 * 2. Family Registration Flow
 * --------------------------
 * 
 * 2.1 Complete Registration:
 * - After OTP verification
 * - Creates family
 * - Updates head details
 * - Adds pending members
 * Example:
 * POST /api/families/complete-registration
 * Headers: Authorization: Bearer {token}
 * {
 *     "familyName": "Pendkar",
 *     "address": "123 Main St",
 *     "description": "Family Description",
 *     "memberCount": 2,
 *     "familyHead": {
 *         "firstName": "Pendkar",
 *         "lastName": "Shankar",
 *         "email": "pendkar@gmail.com",
 *         "mobile": "9059050018",
 *         "dateOfBirth": "1980-01-01",
 *         "gender": "MALE",
 *         "maritalStatus": "MARRIED",
 *         "occupation": "Business",
 *         "education": "Graduate",
 *         "address": "Home Address",
 *         "profilePicture": "http://example.com/profile.jpg",
 *         "photos": ["http://example.com/photo1.jpg"]
 *     },
 *     "members": [
 *         {
 *             "firstName": "VijayaLaxmi",
 *             "mobile": "9346715157",
 *             "relation": "wife",
 *             "profilePicture": "http://example.com/wife.jpg",
 *             "photos": ["http://example.com/wife1.jpg"]
 *         }
 *     ]
 * }
 * 
 * 2.2 Update Registration:
 * - Updates existing family details
 * - Updates head information
 * - Updates pending members
 * Example:
 * PUT /api/families/update/complete-registration
 * (Same format as complete registration)
 * 
 * 3. Database Schema
 * -----------------
 * 
 * 3.1 Users Table:
 * - id (PK)
 * - userUid (unique)
 * - memberUid (unique)
 * - firstName
 * - lastName
 * - email
 * - mobile (unique)
 * - dateOfBirth
 * - gender
 * - maritalStatus
 * - occupation
 * - education
 * - address
 * - role (HEAD/MEMBER)
 * - isVerified
 * - isFamilyHead
 * - profilePicture
 * - photos
 * - family_id (FK)
 * - otp
 * - expiryTime
 * - currentToken
 * 
 * 3.2 Families Table:
 * - id (PK)
 * - familyUid (unique)
 * - familyName
 * - address
 * - description
 * - totalMemberCount
 * - isBlocked
 * - createdAt
 * - updatedAt
 * 
 * 3.3 PendingMembers Table:
 * - id (PK)
 * - memberUid (unique)
 * - firstName
 * - lastName
 * - mobile
 * - relation
 * - verificationCode
 * - family_id (FK)
 * - profilePicture
 * - photos
 * - createdAt
 * - updatedAt
 * 
 * 4. Security Implementation
 * -------------------------
 * 
 * 4.1 JWT Authentication:
 * - Token generation on OTP verification
 * - Token validation on protected endpoints
 * - Token expiration handling
 * 
 * 4.2 OTP System:
 * - 6-digit OTP generation
 * - 5-minute expiry
 * - Stored in user record
 * 
 * 4.3 Role-Based Access:
 * - HEAD: Can manage family and members
 * - MEMBER: Limited access
 * 
 * 5. Current Features
 * ------------------
 * 
 * 5.1 Implemented:
 * - User registration with OTP
 * - User login with OTP
 * - Complete family registration
 * - Update family registration
 * - Add pending members
 * - JWT authentication
 * - Role-based access control
 * 
 * 5.2 Pending/TODO:
 * - Member verification
 * - Family search
 * - Profile updates
 * - Photo upload
 * - Member relationships
 * 
 * 6. API Response Format
 * ---------------------
 * 
 * Success Response:
 * {
 *     "status": 200,
 *     "message": "Success message",
 *     "data": {
 *         // Response data
 *     }
 * }
 * 
 * Error Response:
 * {
 *     "status": 400/401/404/500,
 *     "message": "Error message",
 *     "data": null
 * }
 * 
 * 7. Environment Setup
 * -------------------
 * 
 * 7.1 Requirements:
 * - Java 11+
 * - PostgreSQL
 * - Spring Boot 3.x
 * 
 * 7.2 Configuration:
 * - Database: famtree
 * - Port: 8080
 * - JWT expiry: 10 days
 * - OTP expiry: 5 minutes
 */
public class Documentation {
    // This class serves as development documentation
} 