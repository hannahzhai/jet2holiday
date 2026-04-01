package org.group.jet2holiday.controller;

import org.group.jet2holiday.dto.advice.AdviceGenerateRequest;
import org.group.jet2holiday.dto.advice.AdviceGenerateResponse;
import org.group.jet2holiday.service.AdviceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advice")
public class AdviceController {

    private final AdviceService adviceService;

    public AdviceController(AdviceService adviceService) {
        this.adviceService = adviceService;
    }

    @PostMapping("/generate")
    public AdviceGenerateResponse generateAdvice(@RequestBody(required = false) AdviceGenerateRequest request) {
        return adviceService.generateAdvice(request);
    }
}

