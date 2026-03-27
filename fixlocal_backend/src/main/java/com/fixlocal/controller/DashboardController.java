package com.fixlocal.controller;

import com.fixlocal.dto.TradespersonDashboardDTO;
import com.fixlocal.dto.UserDashboardDTO;
import com.fixlocal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public UserDashboardDTO getUserDashboard(Authentication authentication) {
        return dashboardService.getUserDashboard(authentication.getName());
    }

    @GetMapping("/tradesperson")
    @PreAuthorize("hasRole('TRADESPERSON')")
    public TradespersonDashboardDTO getTradespersonDashboard(Authentication authentication) {
        return dashboardService.getTradespersonDashboard(authentication.getName());
    }
}
