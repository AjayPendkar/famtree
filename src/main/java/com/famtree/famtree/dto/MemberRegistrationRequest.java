package com.famtree.famtree.dto;

import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegistrationRequest {
    private String mobile;
    private String memberUid;
    private String familyUid;
    private String familyName;
    private String headName;
    private String firstName;
    private String lastName;
    private String email;
    private String dateOfBirth;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String occupation;
    private String education;
    private String address;
    private String description;
    private String relation;
    
    // ID Documents
    private String nationalId;
    private String passportNumber;
    private String voterId;
    private String birthCertificateId;
    
    // Images
    private String profileImageUrl;
    private List<String> documentImageUrls;
} 