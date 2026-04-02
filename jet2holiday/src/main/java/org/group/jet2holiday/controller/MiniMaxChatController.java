package org.group.jet2holiday.controller;

import jakarta.validation.Valid;
import org.group.jet2holiday.dto.ai.MiniMaxChatRequest;
import org.group.jet2holiday.dto.ai.MiniMaxChatResponse;
import org.group.jet2holiday.service.MiniMaxChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class MiniMaxChatController {

    private final MiniMaxChatService miniMaxChatService;

    public MiniMaxChatController(MiniMaxChatService miniMaxChatService) {
        this.miniMaxChatService = miniMaxChatService;
    }

    @PostMapping("/minimax-chat")
    public MiniMaxChatResponse ask(@Valid @RequestBody MiniMaxChatRequest request) {
        return miniMaxChatService.askQuestion(request.getQuestion(), request.getRange());
    }
}
