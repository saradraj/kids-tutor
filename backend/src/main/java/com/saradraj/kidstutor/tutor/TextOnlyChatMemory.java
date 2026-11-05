package com.saradraj.kidstutor.tutor;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Objects;

public class TextOnlyChatMemory implements ChatMemory {
    private final ChatMemory delegate;

    public TextOnlyChatMemory(ChatMemory delegate) {
        this.delegate = delegate;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        delegate.add(conversationId, messages.stream()
                .map(TextOnlyChatMemory::textOnly)
                .filter(Objects::nonNull)
                .toList());

    }

    private static Message textOnly(Message message) {
        if(!(message instanceof AssistantMessage assistant)) {
            return message;
        }
        boolean isThinking = assistant.getMetadata()
                .keySet().stream()
                .anyMatch(key -> key.toLowerCase().contains("signature"));
        String text = assistant.getText();
        if(isThinking || text == null || text.isBlank()) {
            return null;
        }
        return new AssistantMessage(text);
    }

    @Override
    public List<Message> get(String conversationId) {
        return delegate.get(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        delegate.clear(conversationId);
    }
}
