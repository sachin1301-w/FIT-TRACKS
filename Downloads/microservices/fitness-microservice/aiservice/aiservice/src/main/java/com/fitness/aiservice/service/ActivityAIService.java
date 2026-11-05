
package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityAIService {

    private final GroqService groqService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = groqService.getRecommendations(prompt);

        log.info("AI response is null: {}", aiResponse == null);
        log.info("AI response length: {}",
                aiResponse == null ? 0 : aiResponse.length());

        if (aiResponse == null || aiResponse.isBlank()) {
            log.error("Groq returned an empty response");
            return createDefaultRecommendation(activity);
        }

        log.info("========== GROQ AI RESPONSE ==========\n{}", aiResponse);
        log.info("======================================");

        return processAIResponse(activity, aiResponse);
    }


    private Recommendation processAIResponse(
            Activity activity, String aiResponse) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            // GroqService already returns the AI-generated content.
            // So parse aiResponse directly instead of looking for choices/message/content.
            String jsonContent = aiResponse.trim();

            log.debug("Generated JSON content: {}", jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);

            JsonNode analysisNode = analysisJson.path("analysis");

            if (!analysisNode.isObject()) {
                log.error("AI response is missing the analysis object");
                return createDefaultRecommendation(activity);
            }

            StringBuilder fullAnalysis = new StringBuilder();

            addAnalysisSection(
                    fullAnalysis, analysisNode, "overall", "Overall");

            addAnalysisSection(
                    fullAnalysis, analysisNode, "pace", "Pace");

            addAnalysisSection(
                    fullAnalysis, analysisNode, "heartRate", "Heart Rate");

            addAnalysisSection(
                    fullAnalysis, analysisNode,
                    "caloriesBurned", "Calories Burned");

            List<String> improvements =
                    extractImprovements(analysisJson.path("improvements"));

            List<String> suggestions =
                    extractSuggestions(analysisJson.path("suggestions"));

            List<String> safety =
                    extractSafetyGuidelines(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getType().toString())
                    .recommendation(fullAnalysis.toString())
                    .improvements(String.valueOf(improvements))
                    .safety(String.valueOf(safety))
                    .suggestions(suggestions)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AI response. Response length: {}",
                    aiResponse == null ? 0 : aiResponse.length(), e);

            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(
            Activity activity) {

        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType().toString())
                .recommendation(
                        "Unable to generate detailed analysis. Please try again.")
                .improvements(String.valueOf(
                        Collections.singletonList(
                                "Continue with your current routine")))
                .safety(String.valueOf(
                        Collections.singletonList(
                                "Warm up before exercising and stop if you feel unwell.")))
                .suggestions(Collections.singletonList(
                        "Easy workout, Start with a comfortable warm-up."))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();

        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }

        return safety.isEmpty()
                ? Collections.singletonList("No specific safety guidance")
                : safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();

        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description =
                        suggestion.path("description").asText();

                if (!workout.isBlank() || !description.isBlank()) {
                    suggestions.add(
                            String.format("%s, %s", workout, description));
                }
            });
        }

        return suggestions.isEmpty()
                ? Collections.singletonList("No specific workout suggestion")
                : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements = new ArrayList<>();

        if (improvementNode.isArray()) {
            improvementNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail =
                        improvement.path("recommendation").asText();

                if (!area.isBlank() || !detail.isBlank()) {
                    improvements.add(
                            String.format("%s: %s", area, detail));
                }
            });
        }

        return improvements.isEmpty()
                ? Collections.singletonList("No specific improvement available")
                : improvements;
    }

    private void addAnalysisSection(
            StringBuilder fullAnalysis,
            JsonNode analysisNode,
            String key,
            String prefix) {

        JsonNode value = analysisNode.path(key);

        if (!value.isMissingNode() && !value.isNull()
                && !value.asText().isBlank()) {

            fullAnalysis.append(prefix)
                    .append(": ")
                    .append(value.asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                Analyze this fitness activity.

                Return ONLY valid JSON. Do not use Markdown fences.
                Keep every value short: one or two sentences maximum.
                Do not add text before or after the JSON.
                Ensure every string, object, and array is fully closed.

                Required structure:
                {
                  "analysis": {
                    "overall": "Short overall analysis",
                    "pace": "Short pace analysis",
                    "heartRate": "Short heart rate analysis",
                    "caloriesBurned": "Short calorie analysis"
                  },
                  "improvements": [
                    {
                      "area": "Improvement area",
                      "recommendation": "Short actionable advice"
                    }
                  ],
                  "suggestions": [
                    {
                      "workout": "Workout name",
                      "description": "Short workout description"
                    }
                  ],
                  "safety": [
                    "Relevant safety advice"
                  ]
                }

                Activity details:
                Activity Type: %s
                Duration: %d minutes
                Calories Burned: %d
                Additional Metrics: %s

                Base your analysis only on the supplied metrics.
                Do not assume heart rate, pace, or distance if they
                are not provided. Avoid unsupported health claims.
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
