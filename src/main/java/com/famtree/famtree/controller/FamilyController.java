package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.FamilyResponse;
import com.famtree.famtree.dto.FamilyHeadResponse;
import com.famtree.famtree.dto.PendingMemberRequest;
import com.famtree.famtree.dto.PendingMemberResponse;
import com.famtree.famtree.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.famtree.famtree.dto.FamilyDetailsRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.multipart.MultipartFile;
import com.famtree.famtree.service.ImageService;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.famtree.famtree.dto.BasicMemberRequest;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    @PostMapping("/complete-registration")
    public ResponseEntity<ApiResponse<?>> completeFamilyRegistration(
            @RequestHeader("Authorization") String token,
            @RequestPart("data") String requestJson,
            @RequestPart(value = "headProfileImage", required = false) MultipartFile headProfileImage,
            @RequestPart(value = "headPhotos", required = false) MultipartFile[] headPhotos,
            @RequestPart(value = "memberPhotos", required = false) Map<String, MultipartFile[]> memberPhotos) {
        try {
            FamilyDetailsRequest request = objectMapper.readValue(requestJson, FamilyDetailsRequest.class);
            Map<String, Object> responseData = new HashMap<>();
            
            // Process head profile image
            if (headProfileImage != null) {
                String imageUrl = imageService.uploadImage(headProfileImage);
                request.getFamilyHead().setProfilePicture(imageUrl);
                responseData.put("headProfileImage", imageUrl);
            }
            
            // Process head photos
            if (headPhotos != null && headPhotos.length > 0) {
                List<String> photoUrls = new ArrayList<>();
                for (MultipartFile photo : headPhotos) {
                    String photoUrl = imageService.uploadImage(photo);
                    photoUrls.add(photoUrl);
                }
                request.getFamilyHead().setPhotos(photoUrls);
                responseData.put("headPhotos", photoUrls);
            }
            
            // Process member photos
            Map<String, List<String>> memberPhotoUrls = new HashMap<>();
            if (memberPhotos != null && !memberPhotos.isEmpty()) {
                for (int i = 0; i < request.getMembers().size(); i++) {
                    String key = "member" + i;
                    if (memberPhotos.containsKey(key)) {
                        MultipartFile[] photos = memberPhotos.get(key);
                        if (photos != null && photos.length > 0) {
                            List<String> photoUrls = new ArrayList<>();
                            for (MultipartFile photo : photos) {
                                String photoUrl = imageService.uploadImage(photo);
                                photoUrls.add(photoUrl);
                            }
                            request.getMembers().get(i).setPhotos(photoUrls);
                            memberPhotoUrls.put(key, photoUrls);
                        }
                    }
                }
                responseData.put("memberPhotos", memberPhotoUrls);
            }

            FamilyResponse response = familyService.completeFamilyRegistration(token, request);
            if (response.getFamilyHead() != null && response.getFamilyHead().isVerified() && response.getFamilyUid() == null) {
                return ResponseEntity.ok(ApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("User already registered as family head")
                    .data(response)
                    .build());
            }
            
            responseData.put("family", response);
            return ResponseEntity.ok(ApiResponse.success(responseData, "Family registration completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/complete-registration/json")
    public ResponseEntity<ApiResponse<?>> completeFamilyRegistrationJson(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            // Handle local file paths in the request
            if (request.getFamilyHead() != null && request.getFamilyHead().getProfilePicture() != null 
                    && request.getFamilyHead().getProfilePicture().startsWith("/data/")) {
                // This is a local file path, not a URL - we'll keep it as is for now
                // In a real implementation, you might want to handle this differently
            }
            
            // Handle member photos that are local file paths
            if (request.getMembers() != null) {
                for (BasicMemberRequest member : request.getMembers()) {
                    if (member.getProfilePicture() != null && member.getProfilePicture().startsWith("/data/")) {
                        // This is a local file path, not a URL - we'll keep it as is for now
                    }
                    
                    if (member.getPhotos() != null) {
                        for (String photo : member.getPhotos()) {
                            if (photo.startsWith("/data/")) {
                                // This is a local file path, not a URL - we'll keep it as is for now
                            }
                        }
                    }
                }
            }
            
            FamilyResponse response = familyService.completeFamilyRegistration(token, request);
            if (response.getFamilyHead() != null && response.getFamilyHead().isVerified() && response.getFamilyUid() == null) {
                return ResponseEntity.ok(ApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("User already registered as family head")
                    .data(null)
                    .build());
            }
            return ResponseEntity.ok(ApiResponse.success(response, "Family registration completed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    // Add a new endpoint for handling mobile app requests with local file paths
    @PostMapping("/complete-registration/mobile")
    public ResponseEntity<ApiResponse<?>> completeFamilyRegistrationMobile(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.completeFamilyRegistration(token, request);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("family", response);
            
            return ResponseEntity.ok(ApiResponse.success(responseData, "Family registration completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/heads")
    public ResponseEntity<ApiResponse<?>> getAllFamilyHeads() {
        try {
            List<FamilyHeadResponse> heads = familyService.getAllFamilyHeads();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("heads", heads);
            responseData.put("count", heads.size());
            
            return ResponseEntity.ok(ApiResponse.success(responseData, "Family heads retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<?>> updateFamilyProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.updateFamilyProfile(token, request);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("family", response);
            
            return ResponseEntity.ok(ApiResponse.success(responseData, "Family profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PutMapping("update/complete-registration")
    public ResponseEntity<ApiResponse<?>> updateCompleteRegistration(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.updateCompleteRegistration(token, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Family registration updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/members/pending")
    public ResponseEntity<ApiResponse<?>> addPendingMember(
            @RequestHeader("Authorization") String token,
            @RequestParam String familyUid,
            @RequestBody PendingMemberRequest request) {
        try {
            PendingMemberResponse response = familyService.addPendingMember(token, familyUid, request);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("member", response);
            responseData.put("familyUid", familyUid);
            
            return ResponseEntity.ok(ApiResponse.success(responseData, "Pending member added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/{familyUid}/photo")
    public ResponseEntity<ApiResponse<?>> updateFamilyPhoto(
            @PathVariable String familyUid,
            @RequestParam("photo") MultipartFile photo) {
        try {
            String photoUrl = imageService.uploadImage(photo);
            familyService.updateFamilyPhoto(familyUid, photo);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("photoUrl", photoUrl);
            responseData.put("familyUid", familyUid);
            
            return ResponseEntity.ok(ApiResponse.success(responseData, "Family photo updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/members/{memberUid}/photo")
    public ResponseEntity<?> updateMemberPhoto(
            @PathVariable String memberUid,
            @RequestParam("photo") MultipartFile photo) {
        try {
            familyService.updateMemberPhoto(memberUid, photo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/members/{memberUid}/photos")
    public ResponseEntity<ApiResponse<?>> addMemberPhotos(
            @PathVariable String memberUid,
            @RequestParam("photos") MultipartFile[] photos) {
        try {
            List<String> photoUrls = new ArrayList<>();
            for (MultipartFile photo : photos) {
                String photoUrl = imageService.uploadImage(photo);
                photoUrls.add(photoUrl);
            }
            familyService.addMemberPhotos(memberUid, photoUrls);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("photoUrls", photoUrls);
            responseData.put("memberUid", memberUid);
            responseData.put("count", photoUrls.size());
            
            return ResponseEntity.ok(ApiResponse.success(responseData, "Member photos added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 