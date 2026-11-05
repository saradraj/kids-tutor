# Math with Pip: a hint-first math tutor for kids

A Spring AI agent that coaches children (ages 6–10) through math problems with hints and questions instead of answers.

**Step 1 of 5:** math only. The agent, deterministic answer checking and a minimal kid UI.

## How it works

```
Kid UI ──▶ TutorController ──▶ TutorAgent (Spring AI ChatClient)
                                   │  hints-first system prompt
                                   │  per-session chat memory
                                   ▼
                              TutorTools
                         ┌─────────┼──────────┐
                getCurrentProblem  checkAnswer  nextProblem
                                   │
                              MathChecker (plain Java)
```

- **The LLM coaches but never grades.** Every answer goes through `checkAnswer`, which calls `MathChecker`. The model only learns `CORRECT`, `TOO_HIGH`, `TOO_LOW` or `NOT_A_NUMBER` and whether the answer is close, not the correct answer itself, so it can't leak it.
- **The tool controls how specific hints get.** `checkAnswer` returns a `hintLevel` (1–3) based on wrong attempts. A worked example that includes the answer is returned only after 3 wrong attempts.
- **The session is scoped by `ToolContext`.** The session id is passed in by the server, not chosen by the model, so a tool call can't reach another child's session.
- **No child PII.** Sessions hold only the current problem and attempt count. The prompt tells the agent never to ask for personal details.

## Run it

Requirements: Java 21, Maven 3.9+, an OpenAI API key.

```bash
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

Open http://localhost:8080, pick a level and type an answer.

Run the tests:

```bash
mvn test
```

### API

| Method | Path | Body | Returns |
|---|---|---|---|
| POST | `/api/sessions` | `{"level": 1}` | `{sessionId, problem}` |
| POST | `/api/sessions/{id}/messages` | `{"text": "12"}` | `{reply, problem, solved}` |

Levels: 1 = add/subtract within 10, 2 = within 20, 3 = times tables to 10.

## Versions

Spring Boot 4.1 and Spring AI 2.0.1. To use a different provider (for example Anthropic or Bedrock), swap the `spring-ai-starter-model-openai` dependency and its config in `application.yml`. The agent code doesn't change.

## Roadmap

1. ~~Math tutor: agent, `checkAnswer`, kid UI~~
2. Input/output safety guardrails and an audit log
3. Adaptive difficulty and a parent dashboard
4. Reading mode with `defineWord` and comprehension questions
5. Deploy, record a demo, add an architecture diagram
