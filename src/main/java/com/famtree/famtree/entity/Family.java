package com.famtree.famtree.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "families")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String familyUid;
    private String familyName;
    private String address;
    private String description;
    @Column(name = "total_member_count")
    private Integer totalMemberCount = 0;
    private String uniqueIdentifier;
    
    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;
    
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> members;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String familyPhoto;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isBlocked = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 