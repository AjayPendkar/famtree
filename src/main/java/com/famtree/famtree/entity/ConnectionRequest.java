package com.famtree.famtree.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.famtree.famtree.enums.ConnectionStatus;

@Entity
@Table(name = "connection_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_family_id")
    private Family requesterFamily;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_family_id")
    private Family receiverFamily;

    private String message;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

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