package com.saradraj.kidstutor.math;

import com.saradraj.kidstutor.problem.Problem;

import org.springframework.stereotype.Component;

/**
 * Deterministic answer checking. The LLM coaches; this class grades.
 * The result says how close the child is without revealing the answer.
 */
@Component
public class MathChecker {

    public enum Verdict { CORRECT, TOO_HIGH, TOO_LOW, NOT_A_NUMBER }

    public enum Closeness { EXACT, CLOSE, FAR, UNKNOWN }

    public record CheckResult(Verdict verdict, Closeness closeness) {
    }

    public CheckResult check(Problem problem, String childAnswer) {
        Integer value = parse(childAnswer);
        if (value == null) {
            return new CheckResult(Verdict.NOT_A_NUMBER, Closeness.UNKNOWN);
        }
        int expected = problem.answer();
        if (value == expected) {
            return new CheckResult(Verdict.CORRECT, Closeness.EXACT);
        }
        Verdict verdict = value > expected ? Verdict.TOO_HIGH : Verdict.TOO_LOW;
        Closeness closeness = Math.abs(value - expected) <= closeThreshold(problem) ? Closeness.CLOSE : Closeness.FAR;
        return new CheckResult(verdict, closeness);
    }

    private int closeThreshold(Problem problem) {
        return problem.op() == Problem.Operation.MULTIPLY ? Math.max(problem.left(), problem.right()) : 2;
    }

    private Integer parse(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (!trimmed.matches("-?\\d{1,4}")) {
            return null;
        }
        return Integer.parseInt(trimmed);
    }
}
