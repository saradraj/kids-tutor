package com.saradraj.kidstutor.problem;

/**
 * A single math problem. The answer is computed in code and never shown to the LLM.
 */
public record Problem(String id, int left, Operation op, int right, int level) {

    public enum Operation {
        ADD("+"), SUBTRACT("-"), MULTIPLY("×");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }
    }

    public int answer() {
        return switch (op) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
        };
    }

    /** Text safe to show the child and the LLM, e.g. "7 + 5". */
    public String text() {
        return left + " " + op.symbol() + " " + right;
    }
}
