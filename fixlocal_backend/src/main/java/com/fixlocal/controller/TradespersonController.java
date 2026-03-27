package com.fixlocal.controller;

import com.fixlocal.dto.TradespersonDTO;
import com.fixlocal.service.TradespersonService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/tradespersons")
@RequiredArgsConstructor
public class TradespersonController {

    private final TradespersonService tradespersonService;

    // ======================================================
    // SEARCH TRADESPERSONS
    // ======================================================

    @GetMapping("/search")
    public ResponseEntity<Page<TradespersonDTO>> searchTradespersons(
            @RequestParam String city,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                tradespersonService.searchTradespersons(
                        city,
                        occupation,
                        minRating,
                        tag,
                        latitude,
                        longitude,
                        radiusKm,
                        page,
                        size
                )
        );
    }

    // ======================================================
    // GET TRADESPERSON PROFILE
    // ======================================================

    @GetMapping("/{id}")
    public ResponseEntity<TradespersonDTO> getTradesperson(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                tradespersonService.getTradespersonById(id)
        );
    }
}