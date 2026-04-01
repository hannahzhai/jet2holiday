package org.group.jet2holiday.dto.ai;

import jakarta.validation.constraints.NotBlank;

public class MiniMaxChatRequest {

    @NotBlank(message = "question is required")
    private String question;

    private String range;

    public MiniMaxChatRequest() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
}
