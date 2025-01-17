package com.famtree.famtree.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String mobile;
    private String role;
    private String familyUid;
    private String familyName;
    private UserDetails userDetails;
    private List<FamilyMemberDetails> familyMembers;

    @Data
    @Builder
    public static class UserDetails {
        private String userUid;
        private String firstName;
        private String lastName;
        private String email;
        private String profilePicture;
        private List<String> photos;
        private boolean isFamilyHead;
    }

    @Data
    @Builder
    public static class FamilyMemberDetails {
        private String memberUid;
        private String firstName;
        private String lastName;
        private String mobile;
        private String relation;
        private String profilePicture;
        private List<String> photos;
    }
} 