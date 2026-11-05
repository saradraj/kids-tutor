package com.saradraj.kidstutor.tutor;

import java.util.Map;

import io.micrometer.common.util.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * The tutor agent: a ChatClient with a hints-first system prompt, per-session memory and the tutor tools.
 * Spring AI's ToolCallingAdvisor runs the model -> tool -> model loop.
 */
@Service
public class TutorAgent {

    private final ChatClient chatClient;

    public TutorAgent(ChatClient.Builder builder,
                      ChatMemory chatMemory,
                      TutorTools tutorTools,
                      @Value("classpath:prompts/tutor-system.st") Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultTools(tutorTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(new TextOnlyChatMemory(chatMemory)).build())
                .build();
    }

    public String reply(String sessionId, String childMessage) {
        String reply = ask(sessionId, childMessage);
        if (StringUtils.isBlank(reply)) {
            reply = ask(sessionId, "Please continue.");
        }
        return reply == null ? "" : reply.strip();
    }

    private String ask(String sessionId, String childMessage) {
        return chatClient.prompt()
                .user(childMessage)
                .toolContext(Map.of(TutorTools.SESSION_ID, sessionId))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}
