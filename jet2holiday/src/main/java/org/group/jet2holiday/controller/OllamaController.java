package org.group.jet2holiday.controller;

import org.group.jet2holiday.client.OllamaClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class OllamaController {

    private final OllamaClient client;

    public OllamaController(OllamaClient client) {
        this.client = client;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = client.askModel(question);
        return Map.of("answer", answer);
    }
}