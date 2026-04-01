package org.group.jet2holiday.client;

public interface LlmAdviceClient {

    LlmAdviceResult generateAdvice(String prompt);

    String modelName();
}

