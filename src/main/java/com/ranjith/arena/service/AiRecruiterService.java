package com.ranjith.arena.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ranjith.arena.model.InterviewMessage;

@Service
public class AiRecruiterService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${AI_API_URL:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${AI_API_KEY:${OPENAI_API_KEY:}}")
    private String apiKey;

    @Value("${AI_MODEL:gpt-6-luna}")
    private String model;

    // =========================================================
    // AI CONFIGURATION CHECK
    // =========================================================

    public boolean enabled() {
        return valid(apiUrl)
                && valid(apiKey)
                && valid(model);
    }

    private boolean valid(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        String x = value.trim().toLowerCase(Locale.ROOT);

        return !x.contains("your_")
                && !x.contains("your-")
                && !x.equals("placeholder")
                && !x.equals("changeme");
    }

    // =========================================================
    // AI QUESTION RESPONSE
    // =========================================================

    public record AiQuestion(
            String round,
            String question,
            String reason
    ) {
    }

    // =========================================================
    // GENERATE NEXT INTERVIEW QUESTION
    // =========================================================

    public AiQuestion nextQuestion(
            String candidate,
            String role,
            String resume,
            List<InterviewMessage> history,
            int number
    ) {

        if (!enabled()) {
            throw new IllegalStateException(
                    "AI recruiter is not configured. " +
                    "Set AI_API_KEY or OPENAI_API_KEY. " +
                    "AI_API_URL defaults to OpenAI Chat Completions " +
                    "and AI_MODEL defaults to gpt-6-luna."
            );
        }

        String transcript = transcript(history);

        String system = """
                You are a senior human technical recruiter conducting
                a realistic live interview.

                Behave naturally like a real recruiter, not like a quiz bot.

                IMPORTANT RULES:

                1. The candidate resume below is the ONLY source for
                   claims about the candidate's skills, projects,
                   internships, certifications and education.

                2. Never invent a technology, project, certification,
                   internship or experience that is not present in the resume.

                3. Ask exactly ONE concise interview question at a time.

                4. Use the FULL conversation history.

                5. Analyze the candidate's previous answer for:
                   - correctness
                   - depth
                   - relevance
                   - clarity
                   - missing details
                   - weak explanations

                6. If the previous answer deserves probing,
                   ask a specific follow-up question.

                7. Do NOT repeatedly ask generic questions such as:
                   "Can you give an example?"

                8. Avoid repeating concepts that were already properly
                   discussed.

                9. Adapt difficulty according to the candidate's
                   demonstrated performance.

                10. Mix interview rounds naturally:
                    - HR
                    - introduction
                    - resume
                    - project
                    - technical
                    - Java
                    - OOP
                    - SQL
                    - Hibernate
                    - coding
                    - problem solving
                    - scenario
                    - behavioral

                11. Only ask technical questions that are reasonably
                    connected to the candidate's resume, target role,
                    or previous interview discussion.

                12. For project questions, test actual ownership.
                    Ask about architecture, implementation,
                    challenges, decisions, database, APIs,
                    testing and improvements when relevant.

                13. For coding questions, ask a concrete coding problem.
                    Later follow up on:
                    - approach
                    - edge cases
                    - time complexity
                    - space complexity
                    - code quality

                14. For HR questions, behave like a real recruiter.
                    Cover:
                    - motivation
                    - teamwork
                    - conflict
                    - strengths
                    - weaknesses
                    - learning
                    - career goals
                    - work style
                    - relocation when appropriate

                15. Use STAR-style follow-ups when a behavioral answer
                    is vague or incomplete.

                16. If an answer is wrong, do not immediately reveal
                    the correct solution. Probe once or move to a
                    related concept.

                17. Keep spoken questions normally under 45 words.

                18. Do not ask multiple questions in one response.

                19. Return ONLY valid JSON.

                Required JSON format:

                {
                  "round": "HR|TECHNICAL|PROJECT|CODING|SCENARIO",
                  "question": "single interview question",
                  "reason": "short internal reason"
                }
                """;

        String user =
                "Candidate: " + safe(candidate) +
                "\nTarget role: " + safe(role) +
                "\nQuestion number: " + number +
                "\n\nRESUME:\n" +
                limit(resume, 12000) +
                "\n\nINTERVIEW SO FAR:\n" +
                limit(transcript, 12000);

        try {

            JsonNode json = call(system, user);

            String round =
                    json.path("round")
                            .asText("TECHNICAL");

            String question =
                    json.path("question")
                            .asText();

            String reason =
                    json.path("reason")
                            .asText("Adaptive follow-up");

            if (question.isBlank()) {
                throw new RuntimeException(
                        "AI returned an empty interview question."
                );
            }

            return new AiQuestion(
                    round,
                    question,
                    reason
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "AI recruiter request failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // FINAL AI EVALUATION
    // =========================================================

    public JsonNode finalEvaluation(
            String candidate,
            String role,
            String resume,
            List<InterviewMessage> history
    ) {

        if (!enabled()) {
            throw new IllegalStateException(
                    "AI recruiter is not configured."
            );
        }

        String system = """
                You are an experienced technical interview assessor.

                Evaluate ONLY the evidence available in:

                1. The candidate resume
                2. The actual interview questions
                3. The candidate's actual answers

                Do not invent information.

                Do not award points merely because keywords appeared.

                Evaluate the candidate based on actual evidence.

                Analyze:

                - technical correctness
                - technical depth
                - communication
                - project ownership
                - project understanding
                - relevance of answers
                - problem solving
                - reasoning
                - clarity
                - practical understanding

                Distinguish between:

                - not assessed
                - weak evidence
                - adequate evidence
                - strong evidence

                Be evidence-based.

                If a skill was not tested, do not assume the candidate
                is weak at it.

                Return ONLY valid JSON.

                Use integer scores from 0 to 100.

                Required JSON:

                {
                  "overall": 0,
                  "technical": 0,
                  "communication": 0,
                  "project": 0,
                  "relevance": 0,
                  "problemSolving": 0,
                  "strengths": "specific evidence-based strengths",
                  "improvements": "specific evidence-based weaknesses or missing depth",
                  "recommendation": "specific practice plan"
                }

                Overall must be consistent with the component evidence.
                """;

        String user =
                "Candidate: " + safe(candidate) +
                "\nRole: " + safe(role) +
                "\n\nRESUME:\n" +
                limit(resume, 12000) +
                "\n\nFULL INTERVIEW TRANSCRIPT:\n" +
                limit(transcript(history), 18000);

        try {

            return call(system, user);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "AI evaluation failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // OPENAI API CALL
    // =========================================================

    private JsonNode call(
            String system,
            String user
    ) throws Exception {

        ObjectNode body =
                mapper.createObjectNode();

        // Model
        body.put(
                "model",
                model.trim()
        );

        /*
         * IMPORTANT:
         *
         * Do NOT send temperature here.
         *
         * The selected model only supports its default
         * temperature value.
         */

        body.put(
                "max_completion_tokens",
                900
        );

        // Messages
        ArrayNode messages =
                body.putArray("messages");

        ObjectNode systemMessage =
                messages.addObject();

        systemMessage.put(
                "role",
                "system"
        );

        systemMessage.put(
                "content",
                system
        );

        ObjectNode userMessage =
                messages.addObject();

        userMessage.put(
                "role",
                "user"
        );

        userMessage.put(
                "content",
                user
        );

        // Convert request to JSON
        String requestBody =
                mapper.writeValueAsString(body);

        // HTTP request
        HttpRequest request =
                HttpRequest.newBuilder(
                        URI.create(apiUrl)
                )
                .timeout(Duration.ofSeconds(60))
                .header(
                        "Content-Type",
                        "application/json"
                )
                .header(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .POST(
                        HttpRequest.BodyPublishers
                                .ofString(requestBody)
                )
                .build();

        // Send request
        HttpResponse<String> response =
                http.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        // =====================================================
        // ERROR HANDLING
        // =====================================================

        if (
                response.statusCode() < 200
                        || response.statusCode() >= 300
        ) {

            String responseBody =
                    limit(
                            response.body(),
                            1500
                    );

            if (
                    responseBody.contains("model_not_found")
                            || responseBody.contains("does not exist")
            ) {

                throw new RuntimeException(
                        "The configured model ["
                                + model
                                + "] is not available to this API key. "
                                + "Set AI_MODEL to a model your account can access."
                );
            }

            throw new RuntimeException(
                    "HTTP "
                            + response.statusCode()
                            + " - "
                            + responseBody
            );
        }

        // =====================================================
        // PARSE RESPONSE
        // =====================================================

        JsonNode root =
                mapper.readTree(
                        response.body()
                );

        String content =
                root.path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText();

        if (content == null || content.isBlank()) {

            throw new RuntimeException(
                    "Provider returned no message content."
            );
        }

        // =====================================================
        // EXTRACT JSON FROM AI RESPONSE
        // =====================================================

        int start =
                content.indexOf('{');

        int end =
                content.lastIndexOf('}');

        if (
                start < 0
                        || end < start
        ) {

            throw new RuntimeException(
                    "AI did not return valid JSON. "
                            + "AI response: "
                            + limit(content, 1000)
            );
        }

        String jsonText =
                content.substring(
                        start,
                        end + 1
                );

        return mapper.readTree(
                jsonText
        );
    }

    // =========================================================
    // BUILD TRANSCRIPT
    // =========================================================

    private String transcript(
            List<InterviewMessage> history
    ) {

        if (history == null || history.isEmpty()) {
            return "No previous interview conversation.";
        }

        StringBuilder result =
                new StringBuilder();

        for (
                InterviewMessage message
                : history
        ) {

            result.append(
                    message
                            .getSpeaker()
                            .toUpperCase()
            );

            result.append(" [");

            result.append(
                    message.getRoundType()
            );

            result.append("]: ");

            result.append(
                    message.getMessage()
            );

            result.append("\n");
        }

        return result.toString();
    }

    // =========================================================
    // LIMIT TEXT SIZE
    // =========================================================

    private String limit(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength
        );
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}