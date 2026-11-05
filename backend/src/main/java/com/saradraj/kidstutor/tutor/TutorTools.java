package com.saradraj.kidstutor.tutor;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.saradraj.kidstutor.math.MathChecker;
import com.saradraj.kidstutor.problem.Problem;
import com.saradraj.kidstutor.problem.ProblemGenerator;
import com.saradraj.kidstutor.session.SessionStore;
import com.saradraj.kidstutor.session.TutorSession;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools the tutor agent can call. The session id arrives through ToolContext,
 * so the model can never pick or guess another child's session.
 */
@Component
public class TutorTools {

    public static final String SESSION_ID = "sessionId";
    static final int ATTEMPTS_BEFORE_WORKED_EXAMPLE = 3;

    private final SessionStore sessionStore;
    private final MathChecker mathChecker;
    private final ProblemGenerator problemGenerator;

    public TutorTools(SessionStore sessionStore, MathChecker mathChecker, ProblemGenerator problemGenerator) {
        this.sessionStore = sessionStore;
        this.mathChecker = mathChecker;
        this.problemGenerator = problemGenerator;
    }

    public record CurrentProblem(String problem, int wrongAttempts, boolean solved) {
    }

    public record AnswerFeedback(
            MathChecker.Verdict verdict,
            MathChecker.Closeness closeness,
            int wrongAttempts,
            int hintLevel,
            String workedExample) {
    }

    @Tool(description = "Get the math problem the child is working on right now.")
    public CurrentProblem getCurrentProblem(ToolContext toolContext) {
        TutorSession session = session(toolContext);
        return new CurrentProblem(session.currentProblem().text(), session.wrongAttempts(), session.solved());
    }

    @Tool(description = """
            Check the child's answer to the current problem. Always call this before saying an answer \
            is right or wrong. It reports CORRECT, TOO_HIGH, TOO_LOW or NOT_A_NUMBER and how close the \
            answer is, but not the correct answer. hintLevel (1-3) says how specific your next hint may be. \
            workedExample is only filled in after several wrong attempts.""")
    public AnswerFeedback checkAnswer(
            @ToolParam(description = "The number the child answered, digits only, e.g. \"12\"") String childAnswer,
            ToolContext toolContext) {
        TutorSession session = session(toolContext);
        Problem problem = session.currentProblem();
        MathChecker.CheckResult result = mathChecker.check(problem, childAnswer);

        if (result.verdict() == MathChecker.Verdict.CORRECT) {
            session.markSolved();
        }
        else if (result.verdict() != MathChecker.Verdict.NOT_A_NUMBER) {
            session.recordWrongAttempt();
        }

        int attempts = session.wrongAttempts();
        String workedExample = attempts >= ATTEMPTS_BEFORE_WORKED_EXAMPLE && !session.solved()
                ? workedExample(problem) : null;
        int hintLevel = Math.clamp(attempts, 1, ATTEMPTS_BEFORE_WORKED_EXAMPLE);
        return new AnswerFeedback(result.verdict(), result.closeness(), attempts, hintLevel, workedExample);
    }

    @Tool(description = "Give the child a new problem. Only call this after the current problem is solved, or the child asks for a new one.")
    public CurrentProblem nextProblem(ToolContext toolContext) {
        TutorSession session = session(toolContext);
        session.startProblem(problemGenerator.generate(session.level()));
        return new CurrentProblem(session.currentProblem().text(), 0, false);
    }

    private TutorSession session(ToolContext toolContext) {
        Object id = toolContext.getContext().get(SESSION_ID);
        return sessionStore.find(String.valueOf(id))
                .orElseThrow(() -> new IllegalStateException("Unknown tutor session"));
    }

    static String workedExample(Problem problem) {
        int l = problem.left();
        int r = problem.right();
        return switch (problem.op()) {
            case ADD -> "Start at " + l + " and count up " + r + ": "
                    + countFrom(l, 1, r) + ". So " + problem.text() + " = " + problem.answer() + ".";
            case SUBTRACT -> "Start at " + l + " and count down " + r + ": "
                    + countFrom(l, -1, r) + ". So " + problem.text() + " = " + problem.answer() + ".";
            case MULTIPLY -> l + " groups of " + r + ": "
                    + IntStream.range(0, l).mapToObj(i -> String.valueOf(r)).collect(Collectors.joining(" + "))
                    + " = " + problem.answer() + ".";
        };
    }

    private static String countFrom(int start, int step, int count) {
        if (count == 0) {
            return "no steps needed";
        }
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> String.valueOf(start + i * step))
                .collect(Collectors.joining(", "));
    }
}
