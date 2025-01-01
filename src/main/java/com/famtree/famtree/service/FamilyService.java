package com.famtree.famtree.service;

import com.famtree.famtree.dto.BasicMemberRequest;
import com.famtree.famtree.dto.FamilyDetailsRequest;
import com.famtree.famtree.dto.FamilyResponse;
import com.famtree.famtree.dto.FamilyHeadResponse;
import com.famtree.famtree.dto.UserDetailsRequest;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.User;
import com.famtree.famtree.entity.PendingMember;
import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import com.famtree.famtree.exception.InvalidTokenException;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.UserRepository;
import com.famtree.famtree.repository.PendingMemberRepository;
import com.famtree.famtree.security.JwtUtil;
import com.famtree.famtree.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.famtree.famtree.dto.PendingMemberRequest;
import com.famtree.famtree.dto.PendingMemberResponse;
import com.famtree.famtree.enums.UserRole;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PendingMemberRepository pendingMemberRepository;

    @Transactional
    public FamilyResponse completeFamilyRegistration(String token, FamilyDetailsRequest request) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new InvalidTokenException("Invalid token format");
            }
            
            String jwtToken = token.substring(7);
            String mobile = jwtUtil.getMobileFromToken(jwtToken);
            
            System.out.println("Processing registration for mobile: " + mobile);
            
            User existingUser = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create or update family
            Family family = existingUser.getFamily();
            if (family == null) {
                family = new Family();
                family.setFamilyUid(UidGenerator.generateFamilyId());
                family.setUniqueIdentifier(UidGenerator.generateFamilyId());
            }
            
            family.setFamilyName(request.getFamilyName());
            family.setAddress(request.getAddress());
            family.setDescription(request.getDescription());
            family.setTotalMemberCount(request.getMemberCount() + 1); // +1 for head
            family = familyRepository.save(family);
            System.out.println("Saved family: " + family.getFamilyName());

            // Update head details
            UserDetailsRequest headDetails = request.getFamilyHead();
            existingUser.setFirstName(headDetails.getFirstName());
            existingUser.setLastName(headDetails.getLastName());
            existingUser.setEmail(headDetails.getEmail());
            existingUser.setDateOfBirth(LocalDate.parse(headDetails.getDateOfBirth()));
            existingUser.setGender(Gender.valueOf(headDetails.getGender()));
            existingUser.setMaritalStatus(MaritalStatus.valueOf(headDetails.getMaritalStatus()));
            existingUser.setOccupation(headDetails.getOccupation());
            existingUser.setEducation(headDetails.getEducation());
            existingUser.setFamily(family);
            existingUser.setFamilyHead(true);
            existingUser.setRole(UserRole.HEAD);
            existingUser = userRepository.save(existingUser);
            System.out.println("Updated head: " + existingUser.getFirstName());

            List<FamilyResponse.MemberResponse> memberResponses = new ArrayList<>();
            
            // Handle members
            if (request.getMembers() != null) {
                // Clear existing pending members first
                pendingMemberRepository.deleteByFamily(family);
                
                for (BasicMemberRequest memberRequest : request.getMembers()) {
                    // Skip if member already exists as verified user
                    if (userRepository.existsByMobile(memberRequest.getMobile())) {
                        continue;
                    }
                    
                    // Check if pending member already exists
                    PendingMember pendingMember = pendingMemberRepository
                        .findByMobileAndFamily(memberRequest.getMobile(), family)
                        .orElse(new PendingMember());
                    
                    pendingMember.setFamily(family);
                    pendingMember.setFirstName(memberRequest.getFirstName());
                    pendingMember.setMobile(memberRequest.getMobile());
                    pendingMember.setRelation(memberRequest.getRelation());
                    
                    // Only generate new IDs if this is a new pending member
                    if (pendingMember.getMemberUid() == null) {
                        pendingMember.setMemberUid(UidGenerator.generateMemberId());
                        pendingMember.setVerificationCode(UidGenerator.generateVerificationCode());
                    }
                    
                    pendingMember = pendingMemberRepository.save(pendingMember);
                    System.out.println("Added/Updated pending member: " + pendingMember.getFirstName());
                    
                    memberResponses.add(FamilyResponse.MemberResponse.builder()
                        .firstName(pendingMember.getFirstName())
                        .mobile(pendingMember.getMobile())
                        .relation(pendingMember.getRelation())
                        .verificationCode(pendingMember.getVerificationCode())
                        .build());
                }
            }

            return FamilyResponse.builder()
                    .familyUid(family.getFamilyUid())
                    .familyName(family.getFamilyName())
                    .address(family.getAddress())
                    .description(family.getDescription())
                    .familyHead(mapUserToResponse(existingUser))
                    .members(memberResponses.isEmpty() ? null : memberResponses)
                    .build();
        } catch (Exception e) {
            System.out.println("Error in completeFamilyRegistration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error processing request: " + e.getMessage());
        }
    }

    private String generateVerificationCode() {
        // Generate a random 5-digit number
        int randomNumber = (int) (Math.random() * 90000) + 10000; // This ensures 5 digits
        return "FAM" + randomNumber;
    }

    public List<FamilyHeadResponse> getAllFamilyHeads() {
        return userRepository.findByIsFamilyHeadTrue()
            .stream()
            .map(head -> FamilyHeadResponse.builder()
                .familyName(head.getFamily().getFamilyName())
                .headName(head.getFirstName())
                .headMobile(head.getMobile())
                .familyCount(head.getFamily().getTotalMemberCount())
                .familyUid(head.getFamily().getFamilyUid())
                .description(head.getFamily().getDescription())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public FamilyResponse updateFamilyProfile(String token, FamilyDetailsRequest request) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new InvalidTokenException("Invalid token format");
            }
            
            String jwtToken = token.substring(7);
            if (!jwtUtil.validateToken(jwtToken)) {
                throw new InvalidTokenException("Invalid or expired token");
            }

            String mobile = jwtUtil.getMobileFromToken(jwtToken);
            
            // Get user and verify they are a family head
            User user = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.isFamilyHead()) {
                throw new RuntimeException("Only family heads can update family profile");
            }

            // Update user details
            UserDetailsRequest headDetails = request.getFamilyHead();
            user.setFirstName(headDetails.getFirstName());
            user.setLastName(headDetails.getLastName());
            user.setEmail(headDetails.getEmail());
            user.setDateOfBirth(LocalDate.parse(headDetails.getDateOfBirth()));
            user.setGender(Gender.valueOf(headDetails.getGender()));
            user.setMaritalStatus(MaritalStatus.valueOf(headDetails.getMaritalStatus()));
            user.setOccupation(headDetails.getOccupation());
            user.setEducation(headDetails.getEducation());
            
            // Update family details
            Family family = user.getFamily();
            
            // Check if another family is using the new name
            if (!family.getFamilyName().equals(request.getFamilyName()) && 
                familyRepository.existsByFamilyName(request.getFamilyName())) {
                throw new RuntimeException("Family name already exists");
            }
            
            family.setFamilyName(request.getFamilyName());
            family.setAddress(request.getAddress());
            family.setDescription(request.getDescription());
            family.setTotalMemberCount(request.getMemberCount() + 1); // +1 for head
            familyRepository.save(family);

            // Handle member updates if provided
            if (request.getMembers() != null) {
                for (BasicMemberRequest memberRequest : request.getMembers()) {
                    // Skip if member already exists as verified user
                    if (userRepository.existsByMobile(memberRequest.getMobile())) {
                        continue;
                    }
                    
                    // Update or create pending member
                    PendingMember pendingMember = pendingMemberRepository
                        .findByMobileAndIsVerifiedFalse(memberRequest.getMobile())
                        .orElse(new PendingMember());
                    
                    pendingMember.setFamily(family);
                    pendingMember.setFirstName(memberRequest.getFirstName());
                    pendingMember.setMobile(memberRequest.getMobile());
                    pendingMember.setRelation(memberRequest.getRelation());
                    if (pendingMember.getVerificationCode() == null) {
                        pendingMember.setVerificationCode(generateVerificationCode());
                    }
                    pendingMemberRepository.save(pendingMember);
                }
            }

            userRepository.save(user);
            
            return FamilyResponse.builder()
                    .familyUid(family.getFamilyUid())
                    .familyName(family.getFamilyName())
                    .address(family.getAddress())
                    .description(family.getDescription())
                    .familyHead(FamilyResponse.UserResponse.builder()
                            .userUid(user.getUserUid())
                            .memberUid(user.getMemberUid())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .mobile(user.getMobile())
                            .nationalId(user.getNationalId())
                            .passportNumber(user.getPassportNumber())
                            .voterId(user.getVoterId())
                            .birthCertificateId(user.getBirthCertificateId())
                            .dateOfBirth(user.getDateOfBirth())
                            .occupation(user.getOccupation())
                            .education(user.getEducation())
                            .gender(user.getGender())
                            .maritalStatus(user.getMaritalStatus())
                            .address(user.getAddress())
                            .role(user.getRole())
                            .isVerified(user.isVerified())
                            .build())
                    .build();
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating profile: " + e.getMessage());
        }
    }

    @Transactional
    public FamilyResponse updateCompleteRegistration(String token, FamilyDetailsRequest request) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new InvalidTokenException("Invalid token format");
            }
            
            String jwtToken = token.substring(7);
            if (!jwtUtil.validateToken(jwtToken)) {
                throw new InvalidTokenException("Invalid or expired token");
            }

            String mobile = jwtUtil.getMobileFromToken(jwtToken);
            
            // Get user and verify they are a family head
            User user = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.isFamilyHead()) {
                throw new RuntimeException("Only family heads can update registration");
            }

            // Update user details
            UserDetailsRequest headDetails = request.getFamilyHead();
            user.setFirstName(headDetails.getFirstName());
            user.setLastName(headDetails.getLastName());
            user.setEmail(headDetails.getEmail());
            user.setDateOfBirth(LocalDate.parse(headDetails.getDateOfBirth()));
            user.setGender(Gender.valueOf(headDetails.getGender()));
            user.setMaritalStatus(MaritalStatus.valueOf(headDetails.getMaritalStatus()));
            user.setOccupation(headDetails.getOccupation());
            user.setEducation(headDetails.getEducation());
            
            // Update family details
            Family family = user.getFamily();
            
            // Check if another family is using the new name
            if (!family.getFamilyName().equals(request.getFamilyName()) && 
                familyRepository.existsByFamilyName(request.getFamilyName())) {
                throw new RuntimeException("Family name already exists");
            }
            
            family.setFamilyName(request.getFamilyName());
            family.setAddress(request.getAddress());
            family.setDescription(request.getDescription());
            family.setTotalMemberCount(request.getMemberCount() + 1); // +1 for head
            familyRepository.save(family);

            // Clear existing pending members
            pendingMemberRepository.deleteByFamily(family);

            // Add new pending members
            if (request.getMembers() != null) {
                for (BasicMemberRequest memberRequest : request.getMembers()) {
                    // Skip if member already exists as verified user
                    if (userRepository.existsByMobile(memberRequest.getMobile())) {
                        continue;
                    }
                    
                    PendingMember pendingMember = new PendingMember();
                    pendingMember.setFamily(family);
                    pendingMember.setFirstName(memberRequest.getFirstName());
                    pendingMember.setMobile(memberRequest.getMobile());
                    pendingMember.setRelation(memberRequest.getRelation());
                    pendingMember.setVerificationCode(generateVerificationCode());
                    pendingMemberRepository.save(pendingMember);
                }
            }

            userRepository.save(user);
            
            return FamilyResponse.builder()
                    .familyUid(family.getFamilyUid())
                    .familyName(family.getFamilyName())
                    .address(family.getAddress())
                    .description(family.getDescription())
                    .familyHead(FamilyResponse.UserResponse.builder()
                            .userUid(user.getUserUid())
                            .memberUid(user.getMemberUid())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .mobile(user.getMobile())
                            .nationalId(user.getNationalId())
                            .passportNumber(user.getPassportNumber())
                            .voterId(user.getVoterId())
                            .birthCertificateId(user.getBirthCertificateId())
                            .dateOfBirth(user.getDateOfBirth())
                            .occupation(user.getOccupation())
                            .education(user.getEducation())
                            .gender(user.getGender())
                            .maritalStatus(user.getMaritalStatus())
                            .address(user.getAddress())
                            .role(user.getRole())
                            .isVerified(user.isVerified())
                            .build())
                    .build();
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating registration: " + e.getMessage());
        }
    }

    @Transactional
    public PendingMemberResponse addPendingMember(String token, String familyUid, PendingMemberRequest request) {
        try {
            System.out.println("Starting addPendingMember process...");
            
            String headMobile = jwtUtil.getMobileFromToken(token.substring(7));
            System.out.println("Head mobile: " + headMobile);
            
            User head = userRepository.findByMobile(headMobile)
                .orElseThrow(() -> new RuntimeException("Head not found"));
            System.out.println("Found head: " + head.getFirstName());

            Family family = head.getFamily();
            if (family == null) {
                throw new RuntimeException("Head's family not found. Please complete family registration first.");
            }
            System.out.println("Found family: " + family.getFamilyName());

            PendingMember pendingMember = new PendingMember();
            pendingMember.setFirstName(request.getFirstName());
            pendingMember.setMobile(request.getMobile());
            pendingMember.setRelation(request.getRelation());
            pendingMember.setFamily(family);
            pendingMember.setMemberUid(UidGenerator.generateMemberId());
            pendingMember.setVerificationCode(UidGenerator.generateVerificationCode());

            System.out.println("Saving pending member...");
            pendingMember = pendingMemberRepository.save(pendingMember);
            System.out.println("Pending member saved with ID: " + pendingMember.getId());

            return PendingMemberResponse.builder()
                .firstName(pendingMember.getFirstName())
                .mobile(pendingMember.getMobile())
                .relation(pendingMember.getRelation())
                .verificationCode(pendingMember.getVerificationCode())
                .memberUid(pendingMember.getMemberUid())
                .familyUid(family.getFamilyUid())
                .build();
        } catch (Exception e) {
            System.out.println("Error in addPendingMember: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error adding pending member: " + e.getMessage());
        }
    }

    // Add helper method to map User to Response
    private FamilyResponse.UserResponse mapUserToResponse(User user) {
        return FamilyResponse.UserResponse.builder()
                .userUid(user.getUserUid())
                .memberUid(user.getMemberUid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .dateOfBirth(user.getDateOfBirth())
                .occupation(user.getOccupation())
                .education(user.getEducation())
                .gender(user.getGender())
                .maritalStatus(user.getMaritalStatus())
                .address(user.getAddress())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .build();
    }
} 