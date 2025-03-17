package com.famtree.famtree.controller;

import com.famtree.famtree.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "Image management APIs")
public class ImageController {
    
    private final ImageService imageService;

    @Operation(
        summary = "Upload an image",
        description = "Upload an image file (JPEG, PNG) with size up to 10MB. Returns the URL of the uploaded image."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image uploaded successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.famtree.famtree.dto.ApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input (file too large, unsupported format)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.famtree.famtree.dto.ApiResponse.class))
        )
    })
    @PostMapping("/upload")
    public ResponseEntity<com.famtree.famtree.dto.ApiResponse<?>> uploadImage(
            @Parameter(description = "Image file to upload (max 10MB, JPEG/PNG)", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadImage(file);
            return ResponseEntity.ok(com.famtree.famtree.dto.ApiResponse.success(imageUrl, "Image uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(com.famtree.famtree.dto.ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @Operation(
        summary = "Get an image by filename",
        description = "Retrieve an image by its filename. Returns the image binary data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image found and returned",
            content = @Content(mediaType = "image/*", schema = @Schema(type = "string", format = "binary"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Image not found"
        )
    })
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "Name of the image file to retrieve", required = true)
            @PathVariable String filename) {
        try {
            byte[] image = imageService.getImage(filename);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 