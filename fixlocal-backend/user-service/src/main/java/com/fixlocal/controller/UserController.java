package com.fixlocal.controller;

import com.fixlocal.dto.ServiceOfferingRequest;
import com.fixlocal.dto.SkillTagsUpdateRequest;
import com.fixlocal.dto.UpdateUserRequest;
import com.fixlocal.dto.UserResponseDTO;
import com.fixlocal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDTO getMyProfile(Authentication authentication) {
        return userService.getMyProfile(authentication.getName());
    }

    @PutMapping("/me")
    public UserResponseDTO updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.updateMyProfile(authentication.getName(), request);
    }

    @PatchMapping("/me/availability")
    public UserResponseDTO toggleAvailability(
            Authentication authentication,
            @RequestParam boolean available) {

        return userService.toggleAvailability(authentication.getName(), available);
    }

    @PutMapping("/me/skill-tags")
    public UserResponseDTO updateSkillTags(
            Authentication authentication,
            @Valid @RequestBody SkillTagsUpdateRequest request) {

        return userService.updateSkillTags(authentication.getName(), request.getTags());
    }

    @PostMapping("/me/services")
    public UserResponseDTO addService(
            Authentication authentication,
            @Valid @RequestBody ServiceOfferingRequest request) {

        return userService.addServiceOffering(authentication.getName(), request);
    }

    @PutMapping("/me/services/{serviceId}")
    public UserResponseDTO updateService(
            Authentication authentication,
            @PathVariable String serviceId,
            @Valid @RequestBody ServiceOfferingRequest request) {

        return userService.updateServiceOffering(authentication.getName(), serviceId, request);
    }

    @DeleteMapping("/me/services/{serviceId}")
    public UserResponseDTO deleteService(
            Authentication authentication,
            @PathVariable String serviceId) {

        return userService.deleteServiceOffering(authentication.getName(), serviceId);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        userService.deleteMyAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}