package com.famtree.famtree.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String memberUid;
    private String userUid;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String occupation;
    private String education;
    private String address;
    private String description;
    
    // ID Documents
    private String nationalId;
    private String passportNumber;
    private String voterId;
    private String birthCertificateId;
    
    // Images
    private String profileImageUrl;
    private List<String> documentImageUrls;
    
    private String role;
    private String token;
    private String familyUid;
    private String familyName;
    private String message;
    private boolean isVerified;
    private boolean success;
} 