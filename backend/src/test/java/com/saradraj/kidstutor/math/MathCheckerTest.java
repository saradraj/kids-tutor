package com.saradraj.kidstutor.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.saradraj.kidstutor.math.MathChecker.Closeness;
import com.saradraj.kidstutor.math.MathChecker.Verdict;
import com.saradraj.kidstutor.problem.Problem;
import com.saradraj.kidstutor.problem.Problem.Operation;

import org.junit.jupiter.api.Test;

class MathCheckerTest {

    private final MathChecker checker = new MathChecker();
    private final Problem sevenPlusFive = new Problem("p1", 7, Operation.ADD, 5, 2);
    private final Problem sixTimesSeven = new Problem("p2", 6, Operation.MULTIPLY, 7, 3);

    @Test
    void correctAnswer() {
        assertEquals(new MathChecker.CheckResult(Verdict.CORRECT, Closeness.EXACT), checker.check(sevenPlusFive, "12"));
    }

    @Test
    void toleratesWhitespace() {
        assertEquals(Verdict.CORRECT, checker.check(sevenPlusFive, "  12 ").verdict());
    }

    @Test
    void closeAndTooHigh() {
        assertEquals(new MathChecker.CheckResult(Verdict.TOO_HIGH, Closeness.CLOSE), checker.check(sevenPlusFive, "13"));
    }

    @Test
    void farAndTooLow() {
        assertEquals(new MathChecker.CheckResult(Verdict.TOO_LOW, Closeness.FAR), checker.check(sevenPlusFive, "2"));
    }

    @Test
    void multiplicationOffByOneGroupIsClose() {
        assertEquals(new MathChecker.CheckResult(Verdict.TOO_LOW, Closeness.CLOSE), checker.check(sixTimesSeven, "35"));
    }

    @Test
    void wordsAreNotNumbers() {
        assertEquals(Verdict.NOT_A_NUMBER, checker.check(sevenPlusFive, "twelve").verdict());
        assertEquals(Verdict.NOT_A_NUMBER, checker.check(sevenPlusFive, null).verdict());
    }
}
