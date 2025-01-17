package com.famtree.famtree.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDetailsRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String occupation;
    private String education;
    private boolean isVerified;
    private String profilePicture;
    private List<String> photos;
    private String address;
} 