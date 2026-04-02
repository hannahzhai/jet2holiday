package org.group.jet2holiday.dto.ai;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniMaxChatRequestValidationTest {

    @Test
    void question_isRequired() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            MiniMaxChatRequest request = new MiniMaxChatRequest();
            request.setQuestion("   ");

            var violations = validator.validate(request);
            assertFalse(violations.isEmpty());
        }
    }

    @Test
    void validQuestion_passesValidation() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            MiniMaxChatRequest request = new MiniMaxChatRequest();
            request.setQuestion("What is my biggest risk?");
            request.setRange("1M");

            var violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }
    }
}
