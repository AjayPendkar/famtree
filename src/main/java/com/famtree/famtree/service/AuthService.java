package com.famtree.famtree.service;

import com.famtree.famtree.dto.AdminAuthRequest;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.User;
import com.famtree.famtree.enums.UserRole;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.UserRepository;
import com.famtree.famtree.repository.PendingMemberRepository;
import com.famtree.famtree.security.JwtUtil;
import com.famtree.famtree.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.famtree.famtree.dto.AuthRequest;
import jakarta.persistence.EntityManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import com.famtree.famtree.dto.OtpRequest;
import com.famtree.famtree.dto.OtpResponse;
import com.famtree.famtree.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import com.famtree.famtree.dto.OtpVerificationRequest;
import com.famtree.famtree.dto.MobileLoginRequest;
import com.famtree.famtree.dto.LoginResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;
import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import java.util.Optional;
import java.time.LocalDateTime;
import com.famtree.famtree.util.UidGenerator;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PendingMemberRepository pendingMemberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void initializeDatabase() {
        try {
            // Drop existing constraint
            entityManager.createNativeQuery(
                "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check"
            ).executeUpdate();

            // Add new constraint
            entityManager.createNativeQuery(
                "ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'HEAD', 'MEMBER'))"
            ).executeUpdate();
        } catch (Exception e) {
            log.error("Error initializing database: ", e);
        }
    }

    @Transactional
    public ApiResponse<OtpResponse> sendOtp(OtpRequest request) {
        try {
            // Validate request
            if (request.getMobile() == null || request.getMobile().trim().isEmpty()) {
                throw new RuntimeException("Mobile number is required");
            }

            // Check if new user
            boolean isNewUser = !userRepository.existsByMobile(request.getMobile());
            
            if (isNewUser) {
                // Validate required fields for new registration
                if (request.getRole() == null) {
                    throw new RuntimeException("Please select role (HEAD/MEMBER) for new registration");
                }
                if (request.getFamilyName() == null || request.getFamilyName().trim().isEmpty()) {
                    throw new RuntimeException("Please enter family name for new registration");
                }
            }

            String otp = generateOtp();
            User user = userRepository.findByMobile(request.getMobile())
                .orElseGet(() -> {
                    // Create new user
                    User newUser = new User();
                    newUser.setMobile(request.getMobile());
                    newUser.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
                    newUser.setFirstName(request.getFamilyName());
                    newUser.setUserUid(UidGenerator.generateUserId());
                    newUser.setMemberUid(UidGenerator.generateMemberId());
                    newUser.setFamilyHead(UserRole.valueOf(request.getRole().toUpperCase()) == UserRole.HEAD);
                    return userRepository.save(newUser);
                });
            
            user.setOtp(otp);
            user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);

            return ApiResponse.success(
                OtpResponse.builder()
                    .message("OTP sent successfully")
                    .otp(otp)  // Remove in production
                    .role(user.getRole().toString())
                    .familyName(user.getFirstName())
                    .mobile(user.getMobile())
                    .isVerified(user.isVerified())
                    .isFamilyHead(user.isFamilyHead())
                    .success(true)
                    .build(),
                isNewUser ? "New user registration initiated" : "OTP sent successfully"
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Please use HEAD or MEMBER");
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || message.contains("null")) {
                message = "Invalid request. Please check all required fields";
            }
            throw new RuntimeException(message);
        }
    }

    @Transactional
    public ApiResponse<OtpResponse> verifyOtp(OtpVerificationRequest request) {
        try {
            User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getOtp().equals(request.getOtp())) {
                return ApiResponse.error("Invalid OTP", HttpStatus.BAD_REQUEST);
            }

            if (user.getExpiryTime().isBefore(LocalDateTime.now())) {
                return ApiResponse.error("OTP has expired", HttpStatus.BAD_REQUEST);
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(request.getMobile());
            user.setCurrentToken(token);
            
            // Create family if user is HEAD and doesn't have a family
            if (user.getRole() == UserRole.HEAD && user.getFamily() == null) {
                Family family = new Family();
                family.setFamilyUid("FAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
                family.setFamilyName(user.getFirstName()); // Using firstName as familyName from registration
                family.setMembers(new ArrayList<>());
                family = familyRepository.save(family);
                
                user.setFamily(family);
                user.setVerified(true);
            }
            
            userRepository.save(user);

            return ApiResponse.success(
                OtpResponse.builder()
                    .message("OTP verified successfully")
                    .token(token)
                    .role(user.getRole().toString())
                    .familyName(user.getFamily() != null ? user.getFamily().getFamilyName() : user.getFirstName())
                    .familyUid(user.getFamily() != null ? user.getFamily().getFamilyUid() : null)
                    .mobile(user.getMobile())
                    .isVerified(user.isVerified())
                    .isFamilyHead(user.isFamilyHead())
                    .success(true)
                    .build(),
                "OTP verified successfully"
            );
        } catch (Exception e) {
            throw new RuntimeException("Error verifying OTP: " + e.getMessage());
        }
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private String generateFamilyId() {
        return "FAM" + System.currentTimeMillis();
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        try {
            // Check if user already exists
            if (userRepository.existsByMobile(request.getMobile())) {
                throw new RuntimeException("Mobile number already registered");
            }

            User user = User.builder()
                .mobile(request.getMobile())
                .userUid(UidGenerator.generateUserId())
                .memberUid(UidGenerator.generateMemberId())
                .role(UserRole.MEMBER)
                .otp(generateOtp())
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .isVerified(false)
                .isFamilyHead(false)
                .build();

            // Generate and store token
            String token = jwtUtil.generateToken(user.getMobile());
            user.setCurrentToken(token);
            
            userRepository.save(user);

            return AuthResponse.builder()
                    .token(token)
                    .mobile(user.getMobile())
                    .otp(user.getOtp())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("Error in registration: ", e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse registerAdmin(AdminAuthRequest request) {
        // Check if admin exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // For first time setup, check if there's any SUPER_ADMIN
        if (request.getRole() == UserRole.SUPER_ADMIN && 
            userRepository.existsByRole(UserRole.SUPER_ADMIN)) {
            throw new RuntimeException("Super Admin already exists");
        }

        // Create new admin user
        User admin = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(request.getRole())
            .userUid(UidGenerator.generateUserId())
            .isVerified(true)
            .build();

        // Generate token
        String token = jwtUtil.generateToken(admin.getEmail());
        admin.setCurrentToken(token);
        
        userRepository.save(admin);

        return AuthResponse.builder()
            .token(token)
            .role(admin.getRole().toString())
            .success(true)
            .build();
    }

    @Transactional
    public AuthResponse loginAdmin(AdminAuthRequest request) {
        User admin = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (admin.getRole() != UserRole.SUPER_ADMIN && admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Unauthorized access");
        }

        String token = jwtUtil.generateToken(admin.getEmail());
        admin.setCurrentToken(token);
        userRepository.save(admin);

        return AuthResponse.builder()
            .token(token)
            .role(admin.getRole().toString())
            .success(true)
            .build();
    }

    @Transactional
    public ApiResponse<LoginResponse> mobileLogin(MobileLoginRequest request) {
        try {
            User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getOtp().equals(request.getOtp())) {
                return ApiResponse.error("Invalid OTP", HttpStatus.BAD_REQUEST);
            }

            String token = jwtUtil.generateToken(user.getMobile());
            user.setCurrentToken(token);
            userRepository.save(user);

            LoginResponse response = buildLoginResponse(user);
            return ApiResponse.success(response, "Login successful");
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    private LoginResponse buildLoginResponse(User user) {
        Family family = user.getFamily();
        List<LoginResponse.FamilyMemberDetails> familyMembers = new ArrayList<>();

        if (family != null && user.isFamilyHead()) {
            familyMembers = family.getMembers().stream()
                .filter(member -> !member.getId().equals(user.getId()))
                .<LoginResponse.FamilyMemberDetails>map(member -> LoginResponse.FamilyMemberDetails.builder()
                    .memberUid(member.getMemberUid())
                    .firstName(member.getFirstName())
                    .lastName(member.getLastName())
                    .mobile(member.getMobile())
                    .relation(member.getRelation())
                    .profilePicture(member.getProfilePicture())
                    .photos(member.getPhotos())
                    .build())
                .collect(Collectors.toList());
        }

        return LoginResponse.builder()
            .token(user.getCurrentToken())
            .mobile(user.getMobile())
            .role(user.getRole().toString())
            .familyUid(family != null ? family.getFamilyUid() : null)
            .familyName(family != null ? family.getFamilyName() : null)
            .userDetails(LoginResponse.UserDetails.builder()
                .userUid(user.getUserUid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .photos(user.getPhotos())
                .isFamilyHead(user.isFamilyHead())
                .build())
            .familyMembers(familyMembers)
            .build();
    }

    @Transactional
    public ApiResponse<OtpResponse> sendLoginOtp(OtpRequest request) {
        try {
            // Check if mobile number exists
            if (!userRepository.existsByMobile(request.getMobile())) {
                return ApiResponse.error(
                    "Mobile number not registered. Please register first", 
                    HttpStatus.NOT_FOUND
                );
            }

            User user = userRepository.findByMobile(request.getMobile()).get();
            
            // Generate and save OTP
            String otp = generateOtp();
            user.setOtp(otp);
            user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);

            return ApiResponse.success(
                OtpResponse.builder()
                    .message("Login OTP sent successfully")
                    .otp(otp)  // Remove in production
                    .role(user.getRole().toString())
                    .familyName(user.getFamily() != null ? user.getFamily().getFamilyName() : null)
                    .familyUid(user.getFamily() != null ? user.getFamily().getFamilyUid() : null)
                    .mobile(user.getMobile())
                    .isVerified(user.isVerified())
                    .isFamilyHead(user.isFamilyHead())
                    .success(true)
                    .build(),
                "OTP sent successfully"
            );
        } catch (Exception e) {
            throw new RuntimeException("Error sending OTP: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<OtpResponse> handleOtpRequest(OtpRequest request) {
        try {
            // Validate mobile number
            if (request.getMobile() == null || request.getMobile().trim().isEmpty()) {
                throw new RuntimeException("Mobile number is required");
            }

            boolean userExists = userRepository.existsByMobile(request.getMobile());

            // Case 1: Only mobile number provided (Login attempt)
            if (request.getRole() == null && request.getFamilyName() == null) {
                if (!userExists) {
                    return ApiResponse.error(
                        "Mobile number not registered. Please register with role and family name", 
                        HttpStatus.NOT_FOUND
                    );
                }
                return handleLogin(request.getMobile());
            }

            // Case 2: All parameters provided (Registration attempt)
            if (userExists) {
                return ApiResponse.error(
                    "Mobile number already registered. Please login with mobile number only", 
                    HttpStatus.BAD_REQUEST
                );
            }
            return handleRegistration(request);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ApiResponse<OtpResponse> handleLogin(String mobile) {
        User user = userRepository.findByMobile(mobile).get();
        String otp = generateOtp();
        user.setOtp(otp);
        user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        return ApiResponse.success(
            OtpResponse.builder()
                .message("Login OTP sent successfully")
                .otp(otp)  // Remove in production
                .role(user.getRole().toString())
                .familyName(user.getFamily() != null ? user.getFamily().getFamilyName() : null)
                .mobile(user.getMobile())
                .isVerified(user.isVerified())
                .isFamilyHead(user.isFamilyHead())
                .success(true)
                .build(),
            "Login OTP sent successfully"
        );
    }

    private ApiResponse<OtpResponse> handleRegistration(OtpRequest request) {
        // Validate registration fields
        if (request.getRole() == null) {
            throw new RuntimeException("Please select role (HEAD/MEMBER) for registration");
        }
        if (request.getFamilyName() == null || request.getFamilyName().trim().isEmpty()) {
            throw new RuntimeException("Please enter family name for registration");
        }

        // Create new user
        User newUser = new User();
        newUser.setMobile(request.getMobile());
        newUser.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        newUser.setFirstName(request.getFamilyName());
        newUser.setUserUid(UidGenerator.generateUserId());
        newUser.setMemberUid(UidGenerator.generateMemberId());
        newUser.setFamilyHead(UserRole.valueOf(request.getRole().toUpperCase()) == UserRole.HEAD);

        String otp = generateOtp();
        newUser.setOtp(otp);
        newUser.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        newUser = userRepository.save(newUser);

        return ApiResponse.success(
            OtpResponse.builder()
                .message("Registration OTP sent successfully")
                .otp(otp)  // Remove in production
                .role(newUser.getRole().toString())
                .familyName(newUser.getFirstName())
                .mobile(newUser.getMobile())
                .isVerified(false)
                .isFamilyHead(newUser.isFamilyHead())
                .success(true)
                .build(),
            "New user registration initiated"
        );
    }
} 