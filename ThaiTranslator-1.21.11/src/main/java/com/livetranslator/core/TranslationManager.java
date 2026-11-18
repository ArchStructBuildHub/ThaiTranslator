package com.livetranslator.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.livetranslator.LiveTranslatorMod;
import com.livetranslator.config.TranslatorConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class TranslationManager {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Set<String> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private static final Pattern IGNORE_PATTERN = Pattern.compile("^[0-9\\s.,:/%+\\-xX*()\\[\\]<>|!?@#$^&=~`'\"]+$");
    private static final Pattern THAI_CHAR_PATTERN = Pattern.compile("[\\u0E00-\\u0E7F]");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    public static String getTranslatedOrQueue(String rawText) {
        if (!LiveTranslatorMod.isTranslationEnabled || rawText == null || rawText.isBlank()) {
            return rawText;
        }

        String cleanText = STRIP_COLOR_PATTERN.matcher(rawText).replaceAll("").trim();
        if (cleanText.length() < 2
                || IGNORE_PATTERN.matcher(cleanText).matches()
                || THAI_CHAR_PATTERN.matcher(cleanText).find()) {
            return rawText;
        }

        String cached = TranslationCache.get(cleanText);
        if (cached != null) {
            return cached;
        }

        if (IN_FLIGHT.add(cleanText)) {
            CompletableFuture.runAsync(() -> {
                try {
                    String result = fetchTranslation(cleanText);
                    if (result != null && !result.isBlank()) {
                        TranslationCache.put(cleanText, result);
                    }
                } catch (Exception e) {
                    LiveTranslatorMod.LOGGER.debug("Translation failed for: {}", cleanText);
                } finally {
                    IN_FLIGHT.remove(cleanText);
                }
            }, EXECUTOR);
        }

        return rawText;
    }

    public static CompletableFuture<String> translateAsync(String rawText) {
        String cleanText = STRIP_COLOR_PATTERN.matcher(rawText).replaceAll("").trim();
        if (cleanText.isBlank() || THAI_CHAR_PATTERN.matcher(cleanText).find()) {
            return CompletableFuture.completedFuture(rawText);
        }
        String cached = TranslationCache.get(cleanText);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() -> {
            String res = fetchTranslation(cleanText);
            if (res != null && !res.isBlank()) {
                TranslationCache.put(cleanText, res);
                return res;
            }
            return rawText;
        }, EXECUTOR);
    }

    private static String fetchTranslation(String text) {
        String provider = TranslatorConfig.PROVIDER.get().toUpperCase();
        if ("GEMINI".equals(provider)) {
            return callGeminiApi(text);
        } else {
            return callGoogleTranslateApi(text);
        }
    }

    private static String callGoogleTranslateApi(String text) {
        String apiKey = TranslatorConfig.GOOGLE_API_KEY.get().trim();
        if (apiKey.isEmpty()) return null;

        try {
            String url = "https://translation.googleapis.com/language/translate/v2?key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            JsonObject body = new JsonObject();
            body.addProperty("q", text);
            body.addProperty("target", "th");
            body.addProperty("format", "text");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                return json.getAsJsonObject("data")
                        .getAsJsonArray("translations")
                        .get(0).getAsJsonObject()
                        .get("translatedText").getAsString();
            }
        } catch (Exception e) {
            LiveTranslatorMod.LOGGER.error("Google Translate API error", e);
        }
        return null;
    }

    private static String callGeminiApi(String text) {
        String apiKey = TranslatorConfig.GEMINI_API_KEY.get().trim();
        String model = TranslatorConfig.GEMINI_MODEL.get().trim();
        if (apiKey.isEmpty()) return null;

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model
                    + ":generateContent?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String prompt = "Translate the following Minecraft game text into natural Thai. "
                    + "Keep formatting codes, numbers, and variables intact. "
                    + "Reply ONLY with the translated text, no explanation:\n" + text;

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonArray parts = new JsonArray();
            parts.add(textPart);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject body = new JsonObject();
            body.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                return json.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString().trim();
            }
        } catch (Exception e) {
            LiveTranslatorMod.LOGGER.error("Gemini API error", e);
        }
        return null;
    }
}
