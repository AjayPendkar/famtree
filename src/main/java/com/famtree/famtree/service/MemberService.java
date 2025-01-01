package com.famtree.famtree.service;

import com.famtree.famtree.dto.MemberRegistrationRequest;
import com.famtree.famtree.dto.MemberVerificationRequest;
import com.famtree.famtree.dto.PendingMemberResponse;
import com.famtree.famtree.entity.PendingMember;
import com.famtree.famtree.entity.User;
import com.famtree.famtree.enums.UserRole;
import com.famtree.famtree.repository.PendingMemberRepository;
import com.famtree.famtree.repository.UserRepository;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.security.JwtUtil;
import com.famtree.famtree.dto.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.famtree.famtree.entity.Family;
import java.time.LocalDate;
import com.famtree.famtree.util.UidGenerator;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final PendingMemberRepository pendingMemberRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public MemberResponse registerMember(MemberRegistrationRequest request) {
        try {
            System.out.println("\n=== Starting Member Registration ===");
            System.out.println("Mobile: " + request.getMobile());
            System.out.println("Family Name: " + request.getFamilyName());

            // First find the family by UID
            Family family = familyRepository.findByFamilyUid(request.getFamilyUid())
                .orElseThrow(() -> new RuntimeException("Family not found with UID: " + request.getFamilyUid()));

            // Verify family name matches
            if (!family.getFamilyName().equals(request.getFamilyName())) {
                throw new RuntimeException("Family name mismatch");
            }

            // Find or create pending member
            PendingMember pendingMember = pendingMemberRepository
                .findByMobileAndFamily(request.getMobile(), family)
                .orElseGet(() -> {
                    PendingMember newMember = new PendingMember();
                    newMember.setFamily(family);
                    newMember.setMobile(request.getMobile());
                    newMember.setMemberUid(request.getMemberUid());
                    newMember.setVerificationCode(UidGenerator.generateVerificationCode());
                    return newMember;
                });

            // Update all member details
            pendingMember.setFirstName(request.getFirstName());
            pendingMember.setLastName(request.getLastName());
            pendingMember.setEmail(request.getEmail());
            pendingMember.setDateOfBirth(request.getDateOfBirth());
            pendingMember.setGender(request.getGender());
            pendingMember.setMaritalStatus(request.getMaritalStatus());
            pendingMember.setOccupation(request.getOccupation());
            pendingMember.setEducation(request.getEducation());
            pendingMember.setAddress(request.getAddress());
            pendingMember.setDescription(request.getDescription());
            
            // ID Documents
            pendingMember.setNationalId(request.getNationalId());
            pendingMember.setPassportNumber(request.getPassportNumber());
            pendingMember.setVoterId(request.getVoterId());
            pendingMember.setBirthCertificateId(request.getBirthCertificateId());
            
            // Images
            pendingMember.setProfileImageUrl(request.getProfileImageUrl());
            pendingMember.setDocumentImageUrls(request.getDocumentImageUrls());

            pendingMember = pendingMemberRepository.save(pendingMember);

            return MemberResponse.builder()
                .memberUid(pendingMember.getMemberUid())
                .firstName(pendingMember.getFirstName())
                .lastName(pendingMember.getLastName())
                .mobile(pendingMember.getMobile())
                .email(pendingMember.getEmail())
                .dateOfBirth(pendingMember.getDateOfBirth())
                .gender(pendingMember.getGender().toString())
                .maritalStatus(pendingMember.getMaritalStatus().toString())
                .occupation(pendingMember.getOccupation())
                .education(pendingMember.getEducation())
                .address(pendingMember.getAddress())
                .description(pendingMember.getDescription())
                .nationalId(pendingMember.getNationalId())
                .passportNumber(pendingMember.getPassportNumber())
                .voterId(pendingMember.getVoterId())
                .birthCertificateId(pendingMember.getBirthCertificateId())
                .profileImageUrl(pendingMember.getProfileImageUrl())
                .documentImageUrls(pendingMember.getDocumentImageUrls())
                .familyUid(family.getFamilyUid())
                .familyName(family.getFamilyName())
                .message("Registration details updated successfully")
                .isVerified(false)
                .success(true)
                .build();
        } catch (Exception e) {
            System.out.println("Error in registerMember: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public MemberResponse verifyMember(String token, MemberVerificationRequest request) {
        try {
            PendingMember pendingMember = pendingMemberRepository.findByMobileAndIsVerifiedFalse(request.getMobile())
                .orElseThrow(() -> new RuntimeException("Pending member not found or already verified"));

            if (!pendingMember.getVerificationCode().equals(request.getVerificationCode())) {
                throw new RuntimeException("Invalid verification code");
            }

            // Create verified member with all data
            User member = new User();
            member.setMemberUid(pendingMember.getMemberUid());
            member.setUserUid(UidGenerator.generateUserId());
            member.setFirstName(pendingMember.getFirstName());
            member.setLastName(pendingMember.getLastName());
            member.setMobile(pendingMember.getMobile());
            member.setEmail(pendingMember.getEmail());
            member.setDateOfBirth(pendingMember.getDateOfBirth() != null ? 
                LocalDate.parse(pendingMember.getDateOfBirth()) : null);
            member.setOccupation(pendingMember.getOccupation());
            member.setEducation(pendingMember.getEducation());
            member.setAddress(pendingMember.getAddress());
            member.setDescription(pendingMember.getDescription());
            member.setNationalId(pendingMember.getNationalId());
            member.setPassportNumber(pendingMember.getPassportNumber());
            member.setVoterId(pendingMember.getVoterId());
            member.setBirthCertificateId(pendingMember.getBirthCertificateId());
            member.setFamily(pendingMember.getFamily());
            member.setRole(UserRole.MEMBER);
            member.setVerified(true);
            member.setFamilyHead(false);
            
            // Save the token
            String newToken = jwtUtil.generateToken(member.getMobile());
            member.setCurrentToken(newToken);
            
            userRepository.save(member);

            // Mark pending member as verified but don't delete
            pendingMember.setVerified(true);
            pendingMemberRepository.save(pendingMember);

            return MemberResponse.builder()
                    .memberUid(member.getMemberUid())
                    .firstName(member.getFirstName())
                    .lastName(member.getLastName())
                    .mobile(member.getMobile())
                    .email(member.getEmail())
                    .role(member.getRole().toString())
                    .token(newToken)
                    .isVerified(true)
                    .success(true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error verifying member: " + e.getMessage());
        }
    }

    public List<PendingMemberResponse> getPendingMembers(String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid token format");
            }
            
            String jwtToken = token.substring(7);
            
            // Validate token first
            if (!jwtUtil.validateToken(jwtToken)) {
                throw new RuntimeException("Invalid or expired token");
            }
            
            String mobile = jwtUtil.getMobileFromToken(jwtToken);
            
            // Get head with detailed error message
            User head = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Head not found for mobile: " + mobile));

            System.out.println("Found head: " + head.getFirstName());
            System.out.println("Is family head: " + head.isFamilyHead());
            System.out.println("Family: " + (head.getFamily() != null ? head.getFamily().getFamilyName() : "null"));

            if (!head.isFamilyHead()) {
                throw new RuntimeException("Only family head can view pending members");
            }

            List<PendingMember> pendingMembers = pendingMemberRepository.findByFamily(head.getFamily());
            System.out.println("Found " + pendingMembers.size() + " pending members");

            return pendingMembers.stream()
                .map(member -> PendingMemberResponse.builder()
                    .firstName(member.getFirstName())
                    .mobile(member.getMobile())
                    .relation(member.getRelation())
                    .verificationCode(member.getVerificationCode())
                    .memberUid(member.getMemberUid())
                    .familyUid(head.getFamily().getFamilyUid())
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error in getPendingMembers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public MemberResponse verifyFamilyCode(String token, MemberVerificationRequest request) {
        try {
            System.out.println("\n=== Starting Family Code Verification ===");
            System.out.println("Request: " + request);
            
            // Get mobile from token
            String jwtToken = token.substring(7);
            String memberMobile = jwtUtil.getMobileFromToken(jwtToken);
            System.out.println("Member mobile from token: " + memberMobile);

            // First find pending member by verification code
            PendingMember pendingMember = pendingMemberRepository
                .findByVerificationCode(request.getVerificationCode())
                .orElseThrow(() -> new RuntimeException("Invalid verification code: " + request.getVerificationCode()));
            
            System.out.println("Found Pending Member: " + pendingMember.getFirstName());

            // Verify family details
            Family family = familyRepository.findByFamilyName(request.getFamilyName())
                .orElseThrow(() -> new RuntimeException("Family not found with name: " + request.getFamilyName()));

            User head = userRepository.findByFamilyAndIsFamilyHeadIsTrue(family)
                .orElseThrow(() -> new RuntimeException("Family head not found for family: " + request.getFamilyName()));

            if (!head.getFirstName().equals(request.getHeadName())) {
                throw new RuntimeException("Invalid head name. Expected: " + head.getFirstName() + ", Got: " + request.getHeadName());
            }

            // Create or update user
            User member = userRepository.findByMobile(memberMobile)
                .orElse(new User());
            
            // Set basic details
            member.setMobile(memberMobile);
            member.setMemberUid(pendingMember.getMemberUid());
            member.setFirstName(pendingMember.getFirstName());
            member.setFamily(family);
            member.setRole(UserRole.MEMBER);
            member.setFamilyHead(false);
            member.setVerified(true);

            userRepository.save(member);
            pendingMemberRepository.delete(pendingMember);
            
            return MemberResponse.builder()
                .message("Family code verified successfully")
                .memberUid(member.getMemberUid())
                .familyUid(family.getFamilyUid())
                .success(true)
                .build();

        } catch (Exception e) {
            System.out.println("\n=== Verification Failed ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Verification failed: " + e.getMessage());
        }
    }

    public void checkDatabaseConnections() {
        try {
            System.out.println("Checking userRepository...");
            userRepository.count();
            System.out.println("userRepository OK");

            System.out.println("Checking familyRepository...");
            familyRepository.count();
            System.out.println("familyRepository OK");

            System.out.println("Checking pendingMemberRepository...");
            pendingMemberRepository.count();
            System.out.println("pendingMemberRepository OK");
        } catch (Exception e) {
            System.out.println("Database connection check failed:");
            e.printStackTrace();
            throw new RuntimeException("Database connection error: " + e.getMessage());
        }
    }
} 