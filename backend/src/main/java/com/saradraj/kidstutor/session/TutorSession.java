package com.saradraj.kidstutor.session;

import com.saradraj.kidstutor.problem.Problem;

/**
 * Per-child session state. Holds no personal information, only the current problem and attempts.
 */
public class TutorSession {

    private final String id;
    private final int level;
    private Problem currentProblem;
    private int wrongAttempts;
    private boolean solved;

    public TutorSession(String id, int level, Problem firstProblem) {
        this.id = id;
        this.level = level;
        this.currentProblem = firstProblem;
    }

    public synchronized void startProblem(Problem problem) {
        this.currentProblem = problem;
        this.wrongAttempts = 0;
        this.solved = false;
    }

    public synchronized void recordWrongAttempt() {
        wrongAttempts++;
    }

    public synchronized void markSolved() {
        solved = true;
    }

    public String id() {
        return id;
    }

    public int level() {
        return level;
    }

    public synchronized Problem currentProblem() {
        return currentProblem;
    }

    public synchronized int wrongAttempts() {
        return wrongAttempts;
    }

    public synchronized boolean solved() {
        return solved;
    }
}
