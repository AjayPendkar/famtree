package com.famtree.famtree.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HomePageResponse {
    private UserInfo userInfo;
    private FamilyInfo familyInfo;
    private List<MemberInfo> members;
    private List<PendingMemberInfo> pendingMembers;
    private ConnectionInfo connectionInfo;
    private List<FeaturedFamily> featuredFamilies;
    private List<HeartWarmingStory> heartWarmingStories;

    @Data
    @Builder
    public static class UserInfo {
        private String userUid;
        private String name;
        private String role;
        private String profilePicture;
        private boolean isVerified;
        private boolean isFamilyHead;
    }

    @Data
    @Builder
    public static class FamilyInfo {
        private String familyUid;
        private String familyName;
        private String address;
        private String description;
        private int totalMembers;
        private int pendingMembers;
        private String familyPhoto;
    }

    @Data
    @Builder
    public static class MemberInfo {
        private String memberUid;
        private String name;
        private String relation;
        private String profilePicture;
        private boolean isVerified;
    }

    @Data
    @Builder
    public static class PendingMemberInfo {
        private String memberUid;
        private String name;
        private String relation;
        private String mobile;
        private String verificationCode;
    }

    @Data
    @Builder
    public static class ConnectionInfo {
        private int pendingRequests;
        private int totalConnections;
        private List<RecentConnection> recentConnections;

        @Data
        @Builder
        public static class RecentConnection {
            private String familyUid;
            private String familyName;
            private String familyPhoto;
            private String connectedDate;
        }
    }

    @Data
    @Builder
    public static class FeaturedFamily {
        private String familyUid;
        private String familyName;
        private String location;
        private String familyPhoto;
        private String description;
    }

    @Data
    @Builder
    public static class HeartWarmingStory {
        private String storyId;
        private String title;
        private String subtitle;
        private String image;
        private String description;
    }
} 