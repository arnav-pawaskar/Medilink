package com.example.appointementapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiHelper {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String apiKey;
    private final Handler mainHandler;
    private final Context context;

    public interface GeminiCallback {
        void onSuccess(String specialistRecommendation);
        void onError(String errorMessage);
    }

    public GeminiApiHelper(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.apiKey = loadGeminiApiKey();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private String loadGeminiApiKey() {
        try {
            return BuildConfig.GEMINI_API_KEY;
        } catch (Exception e) {
            return null;
        }
    }

    public void getSpecialistRecommendation(
            String currentProblem,
            String bloodGroup,
            String pastProblems,
            String familyHistory,
            GeminiCallback callback) {

        if (apiKey == null || apiKey.isEmpty()) {
            mainHandler.post(() -> callback.onError("API key not configured"));
            return;
        }

        String prompt = buildPrompt(currentProblem, bloodGroup, pastProblems, familyHistory);
        JSONObject requestBody = createRequestBody(prompt);
        String urlWithKey = GEMINI_API_URL + "?key=" + apiKey;

        Request request = new Request.Builder()
                .url(urlWithKey)
                .post(RequestBody.create(requestBody.toString(), JSON))
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        mainHandler.post(() -> callback.onError("API error " + response.code()));
                        return;
                    }

                    String responseBody = response.body().string();
                    String specialist = parseResponse(responseBody);

                    mainHandler.post(() -> {
                        if (specialist != null && !specialist.isEmpty()) {
                            callback.onSuccess(specialist);
                        } else {
                            callback.onError("Could not parse recommendation");
                        }
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
                } finally {
                    response.close();
                }
            }
        });
    }

    private String buildPrompt(String currentProblem, String bloodGroup,
                               String pastProblems, String familyHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a medical assistant. Recommend ONE specialist doctor from Mumbai India.\n\n");
        prompt.append("Patient Information:\n");
        prompt.append("- Current Problem: ").append(currentProblem).append("\n");
        prompt.append("- Blood Group: ").append(bloodGroup).append("\n");

        if (pastProblems != null && !pastProblems.isEmpty()) {
            prompt.append("- Past Problems: ").append(pastProblems).append("\n");
        }
        if (familyHistory != null && !familyHistory.isEmpty()) {
            prompt.append("- Family History: ").append(familyHistory).append("\n");
        }

        prompt.append("\nFormat: Dr. [Name], [Specialty]\n");
        prompt.append("Example: Dr. Sarah Johnson, Cardiologist\n");
        prompt.append("Respond ONLY with the doctor recommendation.");

        return prompt.toString();
    }

    private JSONObject createRequestBody(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentItem = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partItem = new JSONObject();

            partItem.put("text", prompt);
            partsArray.put(partItem);
            contentItem.put("parts", partsArray);
            contentsArray.put(contentItem);
            requestBody.put("contents", contentsArray);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 150);
            requestBody.put("generationConfig", generationConfig);

            return requestBody;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONArray parts = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts");
                if (parts.length() > 0) {
                    String text = parts.getJSONObject(0).getString("text").trim();
                    return cleanSpecialistName(text);
                }
            }
        } catch (JSONException e) {
            // Silent fail
        }
        return null;
    }

    private String cleanSpecialistName(String text) {
        text = text.trim().replaceAll("\\s+", " ");
        if (text.contains("\n")) {
            text = text.split("\n")[0].trim();
        }
        if (!text.startsWith("Dr.") && text.contains("Dr.")) {
            text = text.substring(text.indexOf("Dr.")).trim();
        }
        return text;
    }
}
