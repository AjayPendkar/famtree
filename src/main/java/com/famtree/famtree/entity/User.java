package com.famtree.famtree.entity;

import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import com.famtree.famtree.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
    @Index(name = "idx_mobile", columnList = "mobile"),
    @Index(name = "idx_current_token", columnList = "currentToken")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userUid;
    
    @Column(unique = true)
    private String memberUid;
    
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    
    private String mobile;
    private LocalDate dateOfBirth;
    private String occupation;
    private String education;
    private String address;
    private String description;
    
    // ID Documents
    private String nationalId;
    private String passportNumber;
    private String voterId;
    private String birthCertificateId;
    
    // Authentication
    private String otp;
    private LocalDateTime expiryTime;
    private boolean isVerified;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private boolean isFamilyHead;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;
    
    // Token storage
    @Column(name = "current_token", columnDefinition = "TEXT")
    private String currentToken;
    
    @ElementCollection
    @CollectionTable(name = "user_device_tokens", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "device_token", columnDefinition = "TEXT")
    private List<String> deviceTokens = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private String profilePicture;  // URL or path to profile picture
    
    @ElementCollection
    @CollectionTable(name = "user_photos", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "photo_url")
    private List<String> photos;  // List of photo URLs/paths
    
    private String relation;  // Add this field for storing relationship to family head
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 