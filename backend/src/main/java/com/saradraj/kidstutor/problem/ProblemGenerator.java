package com.saradraj.kidstutor.problem;

import java.util.UUID;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Component;

/**
 * Generates problems for a fixed level. Adaptive levels come in a later step.
 * Level 1: add/subtract within 10. Level 2: add/subtract within 20. Level 3: times tables to 10.
 */
@Component
public class ProblemGenerator {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;

    private final RandomGenerator random;

    public ProblemGenerator() {
        this(new java.util.Random());
    }

    ProblemGenerator(RandomGenerator random) {
        this.random = random;
    }

    public Problem generate(int level) {
        int lvl = Math.clamp(level, MIN_LEVEL, MAX_LEVEL);
        String id = UUID.randomUUID().toString();
        return switch (lvl) {
            case 1 -> addOrSubtract(id, 10, lvl);
            case 2 -> addOrSubtract(id, 20, lvl);
            default -> new Problem(id, random.nextInt(2, 11), Problem.Operation.MULTIPLY, random.nextInt(2, 11), lvl);
        };
    }

    private Problem addOrSubtract(String id, int max, int level) {
        if (random.nextBoolean()) {
            int left = random.nextInt(0, max + 1);
            int right = random.nextInt(0, max - left + 1);
            return new Problem(id, left, Problem.Operation.ADD, right, level);
        }
        int left = random.nextInt(0, max + 1);
        int right = random.nextInt(0, left + 1);
        return new Problem(id, left, Problem.Operation.SUBTRACT, right, level);
    }
}
