package com.famtree.famtree.service;

import com.famtree.famtree.dto.HomePageResponse;
import com.famtree.famtree.entity.User;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.PendingMember;
import com.famtree.famtree.repository.UserRepository;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.PendingMemberRepository;
import com.famtree.famtree.repository.ConnectionRequestRepository;
import com.famtree.famtree.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.famtree.famtree.entity.ConnectionRequest;
import com.famtree.famtree.enums.ConnectionStatus;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PendingMemberRepository pendingMemberRepository;
    private final ConnectionRequestRepository connectionRequestRepository;

    public HomePageResponse getHomePageData(String token) {
        String mobile = jwtUtil.getMobileFromToken(token.substring(7));
        User user = userRepository.findByMobile(mobile)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Family family = user.getFamily();
        if (family == null) {
            throw new RuntimeException("Family not found");
        }

        return HomePageResponse.builder()
            .userInfo(getUserInfo(user))
            .familyInfo(getFamilyInfo(family))
            .members(getMembers(family))
            .pendingMembers(getPendingMembers(family))
            .connectionInfo(getConnectionInfo(family))
            .featuredFamilies(getFeaturedFamilies())
            .heartWarmingStories(getHeartWarmingStories())
            .build();
    }

    private HomePageResponse.UserInfo getUserInfo(User user) {
        // Add null checks and default values for name
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        
        // If name is still empty after trimming, set a default
        if (fullName.isEmpty()) {
            fullName = "User-" + user.getUserUid();
        }

        return HomePageResponse.UserInfo.builder()
            .userUid(user.getUserUid())
            .name(fullName)
            .role(user.getRole().toString())
            .profilePicture(user.getProfilePicture())
            .isVerified(user.isVerified())
            .isFamilyHead(user.isFamilyHead())
            .build();
    }

    private HomePageResponse.FamilyInfo getFamilyInfo(Family family) {
        int pendingCount = pendingMemberRepository.countByFamily(family);
        
        // Handle null totalMemberCount
        Integer totalMembers = family.getTotalMemberCount();
        if (totalMembers == null) {
            totalMembers = 0;
        }
        
        return HomePageResponse.FamilyInfo.builder()
            .familyUid(family.getFamilyUid())
            .familyName(family.getFamilyName())
            .address(family.getAddress())
            .description(family.getDescription())
            .totalMembers(totalMembers)
            .pendingMembers(pendingCount)
            .familyPhoto(family.getFamilyPhoto())
            .build();
    }

    private List<HomePageResponse.MemberInfo> getMembers(Family family) {
        return family.getMembers().stream()
            .map(member -> {
                String firstName = member.getFirstName() != null ? member.getFirstName() : "";
                String lastName = member.getLastName() != null ? member.getLastName() : "";
                String fullName = (firstName + " " + lastName).trim();
                
                if (fullName.isEmpty()) {
                    fullName = "Member-" + member.getMemberUid();
                }

                return HomePageResponse.MemberInfo.builder()
                    .memberUid(member.getMemberUid())
                    .name(fullName)
                    .relation("Member") // TODO: Implement relation logic
                    .profilePicture(member.getProfilePicture())
                    .isVerified(member.isVerified())
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<HomePageResponse.PendingMemberInfo> getPendingMembers(Family family) {
        return pendingMemberRepository.findByFamily(family).stream()
            .map(member -> HomePageResponse.PendingMemberInfo.builder()
                .memberUid(member.getMemberUid())
                .name(member.getFirstName())
                .relation(member.getRelation())
                .mobile(member.getMobile())
                .verificationCode(member.getVerificationCode())
                .build())
            .collect(Collectors.toList());
    }

    private HomePageResponse.ConnectionInfo getConnectionInfo(Family family) {
        // Get pending requests count
        int pendingCount = connectionRequestRepository.countByReceiverFamilyAndStatus(family, ConnectionStatus.PENDING);
        
        // Get accepted connections
        List<ConnectionRequest> acceptedConnections = connectionRequestRepository
            .findByReceiverFamilyAndStatus(family, ConnectionStatus.ACCEPTED);
        
        // Get recent connections with explicit type parameters
        List<HomePageResponse.ConnectionInfo.RecentConnection> recentConnections = acceptedConnections.stream()
            .limit(5)
            .<HomePageResponse.ConnectionInfo.RecentConnection>map(conn -> {
                return HomePageResponse.ConnectionInfo.RecentConnection.builder()
                    .familyUid(conn.getRequesterFamily().getFamilyUid())
                    .familyName(conn.getRequesterFamily().getFamilyName())
                    .familyPhoto(conn.getRequesterFamily().getFamilyPhoto())
                    .connectedDate(conn.getCreatedAt().toString())
                    .build();
            })
            .collect(Collectors.toList());

        return HomePageResponse.ConnectionInfo.builder()
            .pendingRequests(pendingCount)
            .totalConnections(acceptedConnections.size())
            .recentConnections(recentConnections)
            .build();
    }

    private List<HomePageResponse.FeaturedFamily> getFeaturedFamilies() {
        return familyRepository.findTop4ByOrderByCreatedAtDesc()
            .stream()
            .map(family -> HomePageResponse.FeaturedFamily.builder()
                .familyUid(family.getFamilyUid())
                .familyName(family.getFamilyName())
                .location(family.getAddress())
                .familyPhoto(family.getFamilyPhoto())
                .description(family.getDescription())
                .build())
            .collect(Collectors.toList());
    }

    private List<HomePageResponse.HeartWarmingStory> getHeartWarmingStories() {
        // For now returning static data, later can be fetched from database
        List<HomePageResponse.HeartWarmingStory> stories = new ArrayList<>();
        
        stories.add(HomePageResponse.HeartWarmingStory.builder()
            .storyId("STORY1")
            .title("Raj & Priya")
            .subtitle("Found joy through...")
            .image("/images/raj-priya.jpg")
            .description("Found joy through connecting with their long-lost family members")
            .build());

        stories.add(HomePageResponse.HeartWarmingStory.builder()
            .storyId("STORY2")
            .title("John & Emily")
            .subtitle("Reconnected with...")
            .image("/images/john-emily.jpg")
            .description("Reconnected with their childhood friends after 20 years")
            .build());

        return stories;
    }
} 