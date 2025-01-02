package com.famtree.famtree.dto;

import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import com.famtree.famtree.enums.UserRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private String userUid;
    private String memberUid;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private LocalDate dateOfBirth;
    private String occupation;
    private String education;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String address;
    private UserRole role;
    private boolean isVerified;
    private boolean isFamilyHead;
    private String familyUid;
    private String familyName;
} 