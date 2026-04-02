package org.group.jet2holiday.dto.ai;

import java.time.LocalDateTime;

public class MiniMaxChatResponse {

    private String answer;
    private boolean fallbackUsed;
    private String model;
    private LocalDateTime generatedAt;

    public MiniMaxChatResponse() {
    }

    public MiniMaxChatResponse(String answer, boolean fallbackUsed, String model, LocalDateTime generatedAt) {
        this.answer = answer;
        this.fallbackUsed = fallbackUsed;
        this.model = model;
        this.generatedAt = generatedAt;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
