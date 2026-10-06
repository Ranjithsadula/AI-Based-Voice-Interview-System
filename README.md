# AI Interview Arena v6 — Genuine Adaptive AI Recruiter

This version replaces the keyword/rule-based question generator and fake keyword scoring used in v5 with an actual LLM-driven interview loop.

## What changed
- Resume PDF is extracted and supplied to the AI on every interview turn.
- Resume grounding rule: the AI must not invent candidate skills/projects not present in the uploaded resume.
- Every new question receives the full interview transcript, so follow-ups depend on the candidate's actual previous answers.
- Human-style round selection: HR, technical, project deep-dive, coding, scenarios.
- Coding questions can probe approach, edge cases and complexity.
- Final report is produced by the AI from question/answer evidence, not keyword counting.
- 20 minutes minimum; selectable up to 60 minutes.
- New clean MySQL DB: `ai_interview_arena_v6`.

## Important: configure a real LLM
A genuine adaptive interviewer cannot work without an LLM. v6 deliberately reports `AI not configured` instead of pretending that fixed rules are AI.

Set these PowerShell environment variables to the values supplied by your chosen OpenAI-compatible LLM provider:

```powershell
$env:AI_API_URL="YOUR_CHAT_COMPLETIONS_ENDPOINT"
$env:AI_API_KEY="YOUR_API_KEY"
$env:AI_MODEL="YOUR_MODEL_NAME"
mvn spring-boot:run
```

Do not commit your real API key to `application.properties` or GitHub.

## Run
```powershell
mvn clean package
mvn spring-boot:run
```
Then in a second terminal:
```powershell
cd frontend
python -m http.server 9000
```
Open http://localhost:9000

## Database
MySQL database: `ai_interview_arena_v6`
Local username/password are currently configured in `application.properties` for the user's development machine.


## OpenAI quick setup
If you are using OpenAI directly, you only need to set your API key. V6.1 defaults to the OpenAI Chat Completions endpoint and `gpt-6-luna`. You can override the model with `AI_MODEL`. Do not use placeholder values such as `YOUR_MODEL_NAME`.

PowerShell:
```powershell
$env:OPENAI_API_KEY="YOUR_REAL_API_KEY"
mvn clean package
mvn spring-boot:run
```
Or explicitly:
```powershell
$env:AI_API_URL="https://api.openai.com/v1/chat/completions"
$env:AI_API_KEY="YOUR_REAL_API_KEY"
$env:AI_MODEL="gpt-6-luna"
mvn spring-boot:run
```
