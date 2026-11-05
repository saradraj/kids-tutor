package com.saradraj.kidstutor.web;

import com.saradraj.kidstutor.session.SessionStore;
import com.saradraj.kidstutor.session.TutorSession;
import com.saradraj.kidstutor.tutor.TutorAgent;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sessions")
public class TutorController {

    static final int MAX_MESSAGE_LENGTH = 200;

    private final SessionStore sessionStore;
    private final TutorAgent tutorAgent;

    public TutorController(SessionStore sessionStore, TutorAgent tutorAgent) {
        this.sessionStore = sessionStore;
        this.tutorAgent = tutorAgent;
    }

    public record StartRequest(int level) {
    }

    public record StartResponse(String sessionId, String problem) {
    }

    public record MessageRequest(String text) {
    }

    public record MessageResponse(String reply, String problem, boolean solved) {
    }

    @PostMapping
    public StartResponse start(@RequestBody StartRequest request) {
        TutorSession session = sessionStore.create(request.level());
        return new StartResponse(session.id(), session.currentProblem().text());
    }

    @PostMapping("/{sessionId}/messages")
    public MessageResponse message(@PathVariable String sessionId, @RequestBody MessageRequest request) {
        TutorSession session = sessionStore.find(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        String text = request.text() == null ? "" : request.text().strip();
        if (text.isEmpty() || text.length() > MAX_MESSAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message must be 1-" + MAX_MESSAGE_LENGTH + " characters");
        }
        String reply = tutorAgent.reply(sessionId, text);
        return new MessageResponse(reply, session.currentProblem().text(), session.solved());
    }
}
