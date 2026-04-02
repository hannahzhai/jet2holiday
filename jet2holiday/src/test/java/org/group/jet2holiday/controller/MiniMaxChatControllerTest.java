package org.group.jet2holiday.controller;

import org.group.jet2holiday.dto.ai.MiniMaxChatRequest;
import org.group.jet2holiday.dto.ai.MiniMaxChatResponse;
import org.group.jet2holiday.service.MiniMaxChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiniMaxChatControllerTest {

    @Mock
    private MiniMaxChatService miniMaxChatService;

    private MiniMaxChatController miniMaxChatController;

    @BeforeEach
    void setUp() {
        miniMaxChatController = new MiniMaxChatController(miniMaxChatService);
    }

    @Test
    void ask_delegatesToService() {
        MiniMaxChatRequest request = new MiniMaxChatRequest();
        request.setQuestion("What should I rebalance?");
        request.setRange("1M");

        MiniMaxChatResponse expected = new MiniMaxChatResponse(
                "- Rebalance gradually.",
                false,
                "MiniMax-Text-01",
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );
        when(miniMaxChatService.askQuestion("What should I rebalance?", "1M")).thenReturn(expected);

        MiniMaxChatResponse response = miniMaxChatController.ask(request);

        assertEquals("MiniMax-Text-01", response.getModel());
        verify(miniMaxChatService).askQuestion("What should I rebalance?", "1M");
    }
}
