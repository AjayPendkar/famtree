package com.famtree.famtree.entity;

import com.famtree.famtree.enums.Gender;
import com.famtree.famtree.enums.MaritalStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pending_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String memberUid;
    
    private String firstName;
    private String lastName;
    private String mobile;
    private String relation;
    private String verificationCode;
    private boolean isVerified;
    
    // Additional fields
    private String email;
    private String dateOfBirth;
    private String occupation;
    private String education;
    private String address;
    private String description;
    
    // ID Documents
    private String nationalId;
    private String passportNumber;
    private String voterId;
    private String birthCertificateId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Image fields
    private String profileImageUrl;
    
    @ElementCollection
    @CollectionTable(name = "pending_member_document_images")
    private List<String> documentImageUrls;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;
    
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