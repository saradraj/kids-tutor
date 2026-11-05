package com.saradraj.kidstutor.session;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.saradraj.kidstutor.problem.ProblemGenerator;

import org.springframework.stereotype.Component;

/**
 * In-memory sessions for step 1. Swapped for Postgres in a later step.
 */
@Component
public class SessionStore {

    private final Map<String, TutorSession> sessions = new ConcurrentHashMap<>();
    private final ProblemGenerator problemGenerator;

    public SessionStore(ProblemGenerator problemGenerator) {
        this.problemGenerator = problemGenerator;
    }

    public TutorSession create(int level) {
        String id = UUID.randomUUID().toString();
        TutorSession session = new TutorSession(id, level, problemGenerator.generate(level));
        sessions.put(id, session);
        return session;
    }

    public Optional<TutorSession> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }
}
