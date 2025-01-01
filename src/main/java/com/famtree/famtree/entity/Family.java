package com.famtree.famtree.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "families")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String familyUid;
    
    @Column(unique = true)
    private String uniqueIdentifier;
    
    private String familyName;
    private String address;
    private String description;
    private Integer totalMemberCount;
    
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL)
    private List<User> members;
    
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL)
    private List<PendingMember> pendingMembers;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
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