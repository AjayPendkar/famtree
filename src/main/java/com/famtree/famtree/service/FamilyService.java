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
import com.famtree.famtree.service.ImageService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PendingMemberRepository pendingMemberRepository;
    private final ImageService imageService;

    @Transactional
    public FamilyResponse completeFamilyRegistration(String token, FamilyDetailsRequest request) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid token format");
            }
            String jwtToken = token.substring(7);
            String mobile = jwtUtil.getMobileFromToken(jwtToken);
            
            User user = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create new family
            Family family = new Family();
            family.setFamilyUid("FAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
            family.setFamilyName(request.getFamilyName());
            family.setAddress(request.getAddress());
            family.setDescription(request.getDescription());
            family.setTotalMemberCount(request.getMemberCount());
            family = familyRepository.save(family);

            // Update user details
            UserDetailsRequest headDetails = request.getFamilyHead();
            user.setFirstName(headDetails.getFirstName());
            user.setLastName(headDetails.getLastName());
            user.setEmail(headDetails.getEmail());
            user.setDateOfBirth(LocalDate.parse(headDetails.getDateOfBirth()));
            user.setGender(Gender.valueOf(headDetails.getGender().toUpperCase()));
            user.setMaritalStatus(MaritalStatus.valueOf(headDetails.getMaritalStatus().toUpperCase()));
            user.setOccupation(headDetails.getOccupation());
            user.setEducation(headDetails.getEducation());
            user.setProfilePicture(headDetails.getProfilePicture());
            user.setPhotos(headDetails.getPhotos());
            user.setAddress(headDetails.getAddress());
            user.setFamily(family);
            user.setFamilyHead(true);
            user.setRole(UserRole.HEAD);
            user.setVerified(true);
            user = userRepository.save(user);

            // Handle members
            List<FamilyResponse.MemberResponse> memberResponses = new ArrayList<>();
            if (request.getMembers() != null) {
                for (BasicMemberRequest memberRequest : request.getMembers()) {
                    PendingMember pendingMember = new PendingMember();
                    pendingMember.setFamily(family);
                    pendingMember.setMemberUid(UidGenerator.generateMemberId());
                    pendingMember.setVerificationCode(UidGenerator.generateVerificationCode());
                    pendingMember.setFirstName(memberRequest.getFirstName());
                    pendingMember.setMobile(memberRequest.getMobile());
                    pendingMember.setRelation(memberRequest.getRelation());
                    pendingMember.setProfilePicture(memberRequest.getProfilePicture());
                    pendingMember.setPhotos(memberRequest.getPhotos());
                    pendingMember = pendingMemberRepository.save(pendingMember);
                    
                    memberResponses.add(FamilyResponse.MemberResponse.builder()
                        .firstName(pendingMember.getFirstName())
                        .mobile(pendingMember.getMobile())
                        .relation(pendingMember.getRelation())
                        .verificationCode(pendingMember.getVerificationCode())
                        .profilePicture(pendingMember.getProfilePicture())
                        .photos(pendingMember.getPhotos())
                        .build());
                }
            }

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
                        .dateOfBirth(user.getDateOfBirth())
                        .occupation(user.getOccupation())
                        .education(user.getEducation())
                        .gender(user.getGender())
                        .maritalStatus(user.getMaritalStatus())
                        .role(user.getRole())
                        .isVerified(user.isVerified())
                        .isFamilyHead(user.isFamilyHead())
                        .profilePicture(user.getProfilePicture())
                        .photos(user.getPhotos())
                        .address(user.getAddress())
                        .build())
                    .members(memberResponses.isEmpty() ? null : memberResponses)
                    .build();
        } catch (Exception e) {
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
            final Family savedFamily = familyRepository.save(family);

            // Handle member updates if provided
            List<FamilyResponse.MemberResponse> memberResponses = new ArrayList<>();
            if (request.getMembers() != null) {
                for (BasicMemberRequest memberRequest : request.getMembers()) {
                    // Skip if member already exists as verified user
                    if (userRepository.existsByMobile(memberRequest.getMobile())) {
                        continue;
                    }
                    
                    // Update or create pending member
                    PendingMember pendingMember = pendingMemberRepository
                        .findByMobileAndFamily(memberRequest.getMobile(), savedFamily)
                        .orElseGet(() -> {
                            PendingMember newMember = new PendingMember();
                            newMember.setFamily(savedFamily);
                            newMember.setMemberUid(UidGenerator.generateMemberId());
                            newMember.setVerificationCode(UidGenerator.generateVerificationCode());
                            return newMember;
                        });
                    
                    pendingMember.setFirstName(memberRequest.getFirstName());
                    pendingMember.setMobile(memberRequest.getMobile());
                    pendingMember.setRelation(memberRequest.getRelation());
                    pendingMember = pendingMemberRepository.save(pendingMember);
                    
                    memberResponses.add(FamilyResponse.MemberResponse.builder()
                        .firstName(pendingMember.getFirstName())
                        .mobile(pendingMember.getMobile())
                        .relation(pendingMember.getRelation())
                        .verificationCode(pendingMember.getVerificationCode())
                        .build());
                }
            }

            user = userRepository.save(user);
            
            // Build complete response
            return FamilyResponse.builder()
                    .familyUid(savedFamily.getFamilyUid())
                    .familyName(savedFamily.getFamilyName())
                    .address(savedFamily.getAddress())
                    .description(savedFamily.getDescription())
                    .familyHead(FamilyResponse.UserResponse.builder()
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
                        .role(user.getRole())
                        .isVerified(user.isVerified())
                        .build())
                    .members(memberResponses.isEmpty() ? null : memberResponses)
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
            // 1. Token validation
            if (!token.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid token format");
            }
            String jwtToken = token.substring(7);
            String mobile = jwtUtil.getMobileFromToken(jwtToken);
            
            System.out.println("Starting update for mobile: " + mobile);

            // 2. Get and validate user
            User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.isFamilyHead()) {
                throw new RuntimeException("Only family heads can update registration");
            }

            // 3. Update Family Details
            Family family = user.getFamily();
            if (family == null) {
                throw new RuntimeException("Family not found");
            }

            // Update ALL family fields
            family.setFamilyName(request.getFamilyName());
            family.setAddress(request.getAddress());
            family.setDescription(request.getDescription());
            family.setTotalMemberCount(request.getMemberCount());
            family.setUniqueIdentifier(family.getFamilyUid());
            family = familyRepository.save(family);

            // 4. Update ALL Family Head Details
            UserDetailsRequest headDetails = request.getFamilyHead();
            if (headDetails != null) {
                // Log before update
                System.out.println("Before Update - User Details:");
                System.out.println("FirstName: " + user.getFirstName());
                System.out.println("LastName: " + user.getLastName());
                System.out.println("Email: " + user.getEmail());
                System.out.println("DateOfBirth: " + user.getDateOfBirth());
                System.out.println("Gender: " + user.getGender());
                System.out.println("MaritalStatus: " + user.getMaritalStatus());
                System.out.println("Education: " + user.getEducation());
                System.out.println("Address: " + user.getAddress());
                System.out.println("ProfilePicture: " + user.getProfilePicture());

                // Set all fields
                user.setFirstName(headDetails.getFirstName());
                user.setLastName(headDetails.getLastName());
                user.setEmail(headDetails.getEmail());
                user.setDateOfBirth(LocalDate.parse(headDetails.getDateOfBirth()));
                user.setGender(Gender.valueOf(headDetails.getGender().toUpperCase()));
                user.setMaritalStatus(MaritalStatus.valueOf(headDetails.getMaritalStatus().toUpperCase()));
                user.setOccupation(headDetails.getOccupation());
                user.setEducation(headDetails.getEducation());
                user.setAddress(headDetails.getAddress());
                user.setProfilePicture(headDetails.getProfilePicture());
                user.setPhotos(headDetails.getPhotos());
                user.setFamilyHead(true);
                user.setVerified(true);
                user.setFamily(family);

                // Save user
                user = userRepository.save(user);

                // Log after update
                System.out.println("\nAfter Update - User Details:");
                System.out.println("FirstName: " + user.getFirstName());
                System.out.println("LastName: " + user.getLastName());
                System.out.println("Email: " + user.getEmail());
                System.out.println("DateOfBirth: " + user.getDateOfBirth());
                System.out.println("Gender: " + user.getGender());
                System.out.println("MaritalStatus: " + user.getMaritalStatus());
                System.out.println("Education: " + user.getEducation());
                System.out.println("Address: " + user.getAddress());
                System.out.println("ProfilePicture: " + user.getProfilePicture());

                // Verify from database
                User savedUser = userRepository.findById(user.getId()).get();
                System.out.println("\nVerified from DB - User Details:");
                System.out.println("FirstName: " + savedUser.getFirstName());
                System.out.println("LastName: " + savedUser.getLastName());
                System.out.println("Email: " + savedUser.getEmail());
                System.out.println("DateOfBirth: " + savedUser.getDateOfBirth());
                System.out.println("Gender: " + savedUser.getGender());
                System.out.println("MaritalStatus: " + savedUser.getMaritalStatus());
                System.out.println("Education: " + savedUser.getEducation());
                System.out.println("Address: " + savedUser.getAddress());
                System.out.println("ProfilePicture: " + savedUser.getProfilePicture());
            }
            
            // Save user with ALL updated fields
            user = userRepository.save(user);

            // 5. Handle Members
            List<FamilyResponse.MemberResponse> memberResponses = new ArrayList<>();
            if (request.getMembers() != null && !request.getMembers().isEmpty()) {
                // First clear existing pending members
                pendingMemberRepository.deleteByFamily(family);
                System.out.println("Cleared existing pending members");

                // Add new pending members
                for (BasicMemberRequest memberRequest : request.getMembers()) {
                    PendingMember pendingMember = new PendingMember();
                    pendingMember.setFamily(family);
                    pendingMember.setFirstName(memberRequest.getFirstName());
                    pendingMember.setMobile(memberRequest.getMobile());
                    pendingMember.setRelation(memberRequest.getRelation());
                    pendingMember.setMemberUid(UidGenerator.generateMemberId());
                    pendingMember.setVerificationCode(UidGenerator.generateVerificationCode());
                    pendingMember.setProfilePicture(memberRequest.getProfilePicture());
                    pendingMember.setPhotos(memberRequest.getPhotos());
                    pendingMember.setCreatedAt(LocalDateTime.now());
                    pendingMember.setUpdatedAt(LocalDateTime.now());

                    pendingMember = pendingMemberRepository.save(pendingMember);
                    System.out.println("Added pending member: " + pendingMember.getFirstName());

                    memberResponses.add(FamilyResponse.MemberResponse.builder()
                        .firstName(pendingMember.getFirstName())
                        .mobile(pendingMember.getMobile())
                        .relation(pendingMember.getRelation())
                        .verificationCode(pendingMember.getVerificationCode())
                        .profilePicture(pendingMember.getProfilePicture())
                        .photos(pendingMember.getPhotos())
                        .build());
                }
            }

            // Final save to ensure all data is persisted
            family = familyRepository.save(family);
            user = userRepository.save(user);

            // Return complete response with ALL fields
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
                        .dateOfBirth(user.getDateOfBirth())
                        .occupation(user.getOccupation())
                        .education(user.getEducation())
                        .gender(user.getGender())
                        .maritalStatus(user.getMaritalStatus())
                        .address(user.getAddress())
                        .role(user.getRole())
                        .isVerified(user.isVerified())
                        .isFamilyHead(user.isFamilyHead())
                        .profilePicture(user.getProfilePicture())
                        .photos(user.getPhotos())
                        .build())
                    .members(memberResponses.isEmpty() ? null : memberResponses)
                    .build();

        } catch (Exception e) {
            System.out.println("Error in updateCompleteRegistration: " + e.getMessage());
            e.printStackTrace();
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

    @Transactional
    public void deleteFamily(String familyUid) {
        Family family = familyRepository.findByFamilyUid(familyUid)
            .orElseThrow(() -> new RuntimeException("Family not found"));
        
        // Clear the family reference from all users
        for (User user : family.getMembers()) {
            user.setFamily(null);
            userRepository.save(user);
        }
        
        // Now delete the family
        familyRepository.delete(family);
    }

    public void updateFamilyPhoto(String familyUid, MultipartFile photo) throws IOException {
        Family family = familyRepository.findByFamilyUid(familyUid)
            .orElseThrow(() -> new RuntimeException("Family not found"));
            
        String photoUrl = imageService.uploadImage(photo);
        family.setFamilyPhoto(photoUrl);
        familyRepository.save(family);
    }

    public void updateMemberPhoto(String memberUid, MultipartFile photo) throws IOException {
        User member = userRepository.findByMemberUid(memberUid)
            .orElseThrow(() -> new RuntimeException("Member not found"));
            
        String photoUrl = imageService.uploadImage(photo);
        member.setProfilePicture(photoUrl);
        userRepository.save(member);
    }

    @Transactional
    public void addMemberPhotos(String memberUid, List<String> photoUrls) {
        User member = userRepository.findByMemberUid(memberUid)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Initialize photos list if null
        if (member.getPhotos() == null) {
            member.setPhotos(new ArrayList<>());
        }
        
        // Add new photos
        member.getPhotos().addAll(photoUrls);
        
        userRepository.save(member);
    }
} 