package com.fixlocal.controller;

import com.fixlocal.dto.InternalAdminChatStatsDTO;
import com.fixlocal.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/chat")
@RequiredArgsConstructor
public class InternalChatController {

    private final ChatService chatService;

    @GetMapping("/admin/stats")
    public InternalAdminChatStatsDTO getAdminStats() {
        return chatService.getAdminChatStats();
    }
}
