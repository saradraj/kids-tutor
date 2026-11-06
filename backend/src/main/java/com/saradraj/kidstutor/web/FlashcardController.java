package com.saradraj.kidstutor.web;

import com.saradraj.kidstutor.math.MathChecker;
import com.saradraj.kidstutor.problem.Problem;
import com.saradraj.kidstutor.problem.ProblemGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Flashcard practice without the LLM. Stateless: each card carries its own numbers,
 * so it keeps working after Cloud Run restarts. Answers are still checked on the server.
 */
@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    static final int MAX_CARDS = 20;
    static final int MAX_NUMBER = 100;

    private final ProblemGenerator problemGenerator;
    private final MathChecker mathChecker;

    public FlashcardController(ProblemGenerator problemGenerator, MathChecker mathChecker) {
        this.problemGenerator = problemGenerator;
        this.mathChecker = mathChecker;
    }

    public record Card(int left, Problem.Operation op, int right, String text) {
    }

    public record CheckRequest(int left, Problem.Operation op, int right, String answer){
    }

    public  record CheckResponse(boolean correct, int answer, MathChecker.Verdict verdict){
    }

    @GetMapping
    public List<Card> deck(@RequestParam(defaultValue = "1") int level,
                           @RequestParam(defaultValue = "10") int count) {
        int size = Math.clamp(count, 1, MAX_CARDS);
        Map<String, Problem> unique = new LinkedHashMap<>();
        for(int tries = 0; unique.size() < size && tries < size * 10; tries++) {
            Problem p = problemGenerator.generate(level);
            unique.putIfAbsent(p.text(), p);
        }

        return unique.values().stream()
                .map(p -> new Card(p.left(), p.op(), p.right(), p.text()))
                .toList();
    }

    @PostMapping("/check")
    public CheckResponse check(@RequestBody CheckRequest request) {
        if (request.op() == null || outOfRange(request.left()) || outOfRange(request.right())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card");
        }
        Problem problem = new Problem("card", request.left(), request.op(), request.right(), 0);
        MathChecker.CheckResult result = mathChecker.check(problem, request.answer());
        return new CheckResponse(result.verdict() == MathChecker.Verdict.CORRECT, problem.answer(), result.verdict());
    }

    private static boolean outOfRange(int n) {
        return n < 0 || n > MAX_NUMBER;
    }
}
