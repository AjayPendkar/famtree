package com.famtree.famtree.service;

import com.famtree.famtree.dto.UserResponse;
import com.famtree.famtree.dto.FamilyResponse;
import com.famtree.famtree.entity.User;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.enums.UserRole;
import com.famtree.famtree.repository.UserRepository;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.PendingMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PendingMemberRepository pendingMemberRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapUserToResponse)
            .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(String role) {
        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == userRole)
            .map(this::mapUserToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(String mobile) {
        User user = userRepository.findByMobile(mobile)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // If user is family head, delete the entire family
        if (user.isFamilyHead() && user.getFamily() != null) {
            deleteFamily(user.getFamily().getFamilyUid());
        } else {
            userRepository.delete(user);
        }
    }

    @Transactional
    public UserResponse updateUserRole(String mobile, String newRole) {
        User user = userRepository.findByMobile(mobile)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserRole role = UserRole.valueOf(newRole.toUpperCase());
        user.setRole(role);
        
        // If changing to HEAD role, ensure user is not already part of a family
        if (role == UserRole.HEAD && user.getFamily() != null && !user.isFamilyHead()) {
            throw new RuntimeException("User is already part of a family and cannot be made head");
        }
        
        user = userRepository.save(user);
        return mapUserToResponse(user);
    }

    public List<FamilyResponse> getAllFamilies() {
        return familyRepository.findAll().stream()
            .map(family -> {
                User head = userRepository.findByFamilyAndIsFamilyHeadIsTrue(family)
                    .orElse(null);
                
                return FamilyResponse.builder()
                    .familyUid(family.getFamilyUid())
                    .familyName(family.getFamilyName())
                    .address(family.getAddress())
                    .description(family.getDescription())
                    .familyHead(head != null ? mapUserToFamilyResponse(head) : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFamily(String familyUid) {
        Family family = familyRepository.findByFamilyUid(familyUid)
            .orElseThrow(() -> new RuntimeException("Family not found"));
        
        // Delete all pending members
        pendingMemberRepository.deleteByFamily(family);
        
        // Delete the family (this will cascade to members due to JPA relationship)
        familyRepository.delete(family);
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
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
            .familyUid(user.getFamily() != null ? user.getFamily().getFamilyUid() : null)
            .familyName(user.getFamily() != null ? user.getFamily().getFamilyName() : null)
            .build();
    }

    private FamilyResponse.UserResponse mapUserToFamilyResponse(User user) {
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
            .role(user.getRole())
            .isVerified(user.isVerified())
            .build();
    }
} 