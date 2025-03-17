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
            // Convert JSON string to FamilyDetailsRequest
            FamilyDetailsRequest request = objectMapper.readValue(requestJson, FamilyDetailsRequest.class);
            
            // Process head profile image if provided
            if (headProfileImage != null) {
                String imageUrl = imageService.uploadImage(headProfileImage);
                request.getFamilyHead().setProfilePicture(imageUrl);
            }
            
            // Process head additional photos if provided
            if (headPhotos != null && headPhotos.length > 0) {
                List<String> photoUrls = new ArrayList<>();
                for (MultipartFile photo : headPhotos) {
                    String photoUrl = imageService.uploadImage(photo);
                    photoUrls.add(photoUrl);
                }
                request.getFamilyHead().setPhotos(photoUrls);
            }
            
            // Process member photos if provided
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
        } catch (Exception e) {
            return ResponseEntity.badRequest()
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
            // For mobile requests, we accept the local file paths and handle them in the service
            // The mobile app will later upload these files separately
            
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

    @GetMapping("/heads")
    public ResponseEntity<ApiResponse<?>> getAllFamilyHeads() {
        List<FamilyHeadResponse> heads = familyService.getAllFamilyHeads();
        return ResponseEntity.ok(ApiResponse.success(heads, "Family heads retrieved successfully"));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<?>> updateFamilyProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.updateFamilyProfile(token, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Family profile updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
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
            return ResponseEntity.ok(ApiResponse.success(response, "Pending member added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/{familyUid}/photo")
    public ResponseEntity<?> updateFamilyPhoto(
            @PathVariable String familyUid,
            @RequestParam("photo") MultipartFile photo) {
        try {
            familyService.updateFamilyPhoto(familyUid, photo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
    public ResponseEntity<?> addMemberPhotos(
            @PathVariable String memberUid,
            @RequestParam("photos") MultipartFile[] photos) {
        try {
            List<String> photoUrls = new ArrayList<>();
            for (MultipartFile photo : photos) {
                String photoUrl = imageService.uploadImage(photo);
                photoUrls.add(photoUrl);
            }
            familyService.addMemberPhotos(memberUid, photoUrls);
            return ResponseEntity.ok(ApiResponse.success(photoUrls, "Member photos added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 