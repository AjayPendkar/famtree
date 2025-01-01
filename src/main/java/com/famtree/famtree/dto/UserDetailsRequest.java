package com.famtree.famtree.dto;

import lombok.Data;

@Data
public class UserDetailsRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String occupation;
    private String education;
} 