package com.famtree.famtree.service;

import com.famtree.famtree.dto.ConnectionRequestDTO;
import com.famtree.famtree.entity.ConnectionRequest;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.Subscription;
import com.famtree.famtree.enums.ConnectionStatus;
import com.famtree.famtree.enums.SubscriptionPlan;
import com.famtree.famtree.repository.ConnectionRequestRepository;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionRequestRepository connectionRequestRepository;
    private final FamilyRepository familyRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void sendConnectionRequest(String requesterFamilyId, String receiverFamilyId, String message) {
        Family requesterFamily = familyRepository.findByFamilyUid(requesterFamilyId)
            .orElseThrow(() -> new RuntimeException("Requester family not found"));
        
        Family receiverFamily = familyRepository.findByFamilyUid(receiverFamilyId)
            .orElseThrow(() -> new RuntimeException("Receiver family not found"));

        // Check remaining requests
        Subscription subscription = subscriptionRepository.findByFamily(requesterFamily)
            .orElse(Subscription.builder().plan(SubscriptionPlan.FREE).build());

        if (!subscription.getPlan().hasUnlimitedRequests()) {
            long sentRequests = connectionRequestRepository.countByRequesterFamilyAndStatus(
                requesterFamily, ConnectionStatus.PENDING);
            
            if (sentRequests >= subscription.getPlan().getMaxRequests()) {
                throw new RuntimeException("REQUEST_LIMIT_EXCEEDED");
            }
        }

        ConnectionRequest request = ConnectionRequest.builder()
            .requesterFamily(requesterFamily)
            .receiverFamily(receiverFamily)
            .message(message)
            .status(ConnectionStatus.PENDING)
            .build();

        connectionRequestRepository.save(request);
    }

    public List<ConnectionRequestDTO> getConnectionRequests(String familyId, String type, String status) {
        Family family = familyRepository.findByFamilyUid(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found"));

        ConnectionStatus requestStatus = ConnectionStatus.valueOf(status.toUpperCase());
        List<ConnectionRequest> requests;

        if ("incoming".equals(type)) {
            requests = connectionRequestRepository.findByReceiverFamilyAndStatus(family, requestStatus);
        } else {
            requests = connectionRequestRepository.findByRequesterFamilyAndStatus(family, requestStatus);
        }

        return requests.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateRequestStatus(Long requestId, String status) {
        ConnectionRequest request = connectionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(ConnectionStatus.valueOf(status.toUpperCase()));
        connectionRequestRepository.save(request);
    }

    private ConnectionRequestDTO mapToDTO(ConnectionRequest request) {
        return ConnectionRequestDTO.builder()
            .requestId(request.getId())
            .requesterFamily(ConnectionRequestDTO.FamilyBasicInfo.builder()
                .familyUid(request.getRequesterFamily().getFamilyUid())
                .familyName(request.getRequesterFamily().getFamilyName())
                .build())
            .receiverFamily(ConnectionRequestDTO.FamilyBasicInfo.builder()
                .familyUid(request.getReceiverFamily().getFamilyUid())
                .familyName(request.getReceiverFamily().getFamilyName())
                .build())
            .status(request.getStatus().toString())
            .createdAt(request.getCreatedAt())
            .message(request.getMessage())
            .build();
    }
} 