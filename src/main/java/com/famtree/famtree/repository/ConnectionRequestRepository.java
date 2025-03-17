package com.famtree.famtree.repository;

import com.famtree.famtree.entity.ConnectionRequest;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {
    List<ConnectionRequest> findByRequesterFamilyAndStatus(Family family, ConnectionStatus status);
    List<ConnectionRequest> findByReceiverFamilyAndStatus(Family family, ConnectionStatus status);
    long countByRequesterFamilyAndStatus(Family family, ConnectionStatus status);
    int countByReceiverFamilyAndStatus(Family family, ConnectionStatus status);
} 