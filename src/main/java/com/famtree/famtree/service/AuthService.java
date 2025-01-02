package com.famtree.famtree.service;

import com.famtree.famtree.dto.InitialRegistrationRequest;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;
import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import java.util.Optional;
import java.time.LocalDateTime;
import com.famtree.famtree.util.UidGenerator;

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
    public AuthResponse sendOtp(InitialRegistrationRequest request) {
        try {
            // Check if user exists
            Optional<User> existingUser = userRepository.findByMobile(request.getMobile());
            
            // Generate OTP
            String otp = generateOtp();
            
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                // If user exists, use their actual role
                
                // Update existing user's OTP
                user.setOtp(otp);
                user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
                // Store family name if not already set
                if (user.getFamily() == null && request.getFamilyName() != null) {
                    user.setFirstName(request.getFamilyName());  // Store temporarily
                }
                userRepository.save(user);
            } else {
                // Create new user
                User newUser = User.builder()
                    .mobile(request.getMobile())
                    .otp(otp)
                    .expiryTime(LocalDateTime.now().plusMinutes(5))
                    .isVerified(false)
                    .isFamilyHead(request.getRole() == UserRole.HEAD)
                    .role(request.getRole())
                    .firstName(request.getFamilyName())  // Store family name
                    .build();
                
                userRepository.save(newUser);
            }

            return AuthResponse.builder()
                .message("OTP sent successfully")
                .otp(otp)
                .role(request.getRole().toString())
                .familyName(request.getFamilyName())
                .success(true)
                .build();
            
        } catch (Exception e) {
            log.error("Error sending OTP: ", e);
            throw new RuntimeException("Error sending OTP: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse verifyOtp(String mobile, String otp) {
        try {
            System.out.println("Verifying OTP for mobile: " + mobile);
            
            // Find or create user
            User user = userRepository.findByMobile(mobile)
                    .orElseGet(() -> {
                        System.out.println("Creating new user for mobile: " + mobile);
                        User newUser = new User();
                        newUser.setMobile(mobile);
                        newUser.setUserUid(UidGenerator.generateUserId());
                        newUser.setMemberUid(UidGenerator.generateMemberId());
                        newUser.setOtp(otp);
                        newUser.setExpiryTime(LocalDateTime.now().plusMinutes(5));
                        newUser.setVerified(false);
                        newUser.setRole(UserRole.MEMBER);
                        return userRepository.save(newUser);
                    });

            if (!user.getOtp().equals(otp)) {
                return AuthResponse.builder()
                        .message("Invalid OTP")
                        .success(false)
                        .build();
            }
            
            // Generate and store token
            String token = jwtUtil.generateToken(mobile);
            user.setCurrentToken(token);
            user.setVerified(true);
            
            // Ensure user is saved
            user = userRepository.save(user);
            System.out.println("User saved with ID: " + user.getId() + ", Mobile: " + user.getMobile());

            return AuthResponse.builder()
                    .message("OTP verified successfully")
                    .token(token)
                    .role(user.getRole().toString())
                    .success(true)
                    .build();
        } catch (Exception e) {
            System.out.println("Error in verifyOtp: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error verifying OTP: " + e.getMessage());
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
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
} 