package com.famtree.famtree.dto;

import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import com.famtree.famtree.enums.UserRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyResponse {
    private String familyUid;
    private String familyName;
    private String address;
    private String description;
    private UserResponse familyHead;
    private List<MemberResponse> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberResponse {
        private String firstName;
        private String mobile;
        private String relation;
        private String verificationCode;
        private String profilePicture;
        private List<String> photos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private String userUid;
        private String memberUid;
        private String firstName;
        private String lastName;
        private String email;
        private String mobile;
        private String nationalId;
        private String passportNumber;
        private String voterId;
        private String birthCertificateId;
        private LocalDate dateOfBirth;
        private String occupation;
        private String education;
        private Gender gender;
        private MaritalStatus maritalStatus;
        private String address;
        private UserRole role;
        private boolean isVerified;
        private boolean isFamilyHead;
        private String profilePicture;
        private List<String> photos;
    }
} 