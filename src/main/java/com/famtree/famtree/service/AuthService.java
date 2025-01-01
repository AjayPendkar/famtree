package com.famtree.famtree.service;

import com.famtree.famtree.dto.InitialRegistrationRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.famtree.famtree.dto.AuthRequest;

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

    @Transactional
    public AuthResponse sendOtp(InitialRegistrationRequest request) {
        try {
            // Check if user exists
            Optional<User> existingUser = userRepository.findByMobile(request.getMobile());
            
            // Generate OTP
            String otp = generateOtp();
            
            // Determine role
            String role = "MEMBER";  // Default role is MEMBER
            
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                // If user exists, use their actual role
                role = user.isFamilyHead() ? "HEAD" : "MEMBER";
                
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
                    .isFamilyHead(request.isFamilyHead())
                    .role(request.isFamilyHead() ? UserRole.HEAD : UserRole.MEMBER)
                    .firstName(request.getFamilyName())  // Store family name
                    .build();
                
                userRepository.save(newUser);
                role = request.isFamilyHead() ? "HEAD" : "MEMBER";
            }

            return AuthResponse.builder()
                .message("OTP sent successfully")
                .otp(otp)
                .role(role)
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
} 