package com.livetranslator.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.livetranslator.LiveTranslatorMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TranslationCache {
    private static final Path CACHE_FILE = FMLPaths.CONFIGDIR.get().resolve("thaitranslator_cache.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger UNSAVED_COUNT = new AtomicInteger(0);

    public static String get(String original) {
        return CACHE.get(original);
    }

    public static void put(String original, String translated) {
        CACHE.put(original, translated);
        if (UNSAVED_COUNT.incrementAndGet() >= 20) {
            UNSAVED_COUNT.set(0);
            saveAsync();
        }
    }

    public static void load() {
        if (!Files.exists(CACHE_FILE)) return;
        try {
            String json = Files.readString(CACHE_FILE, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = GSON.fromJson(json, type);
            if (loaded != null) {
                CACHE.putAll(loaded);
                LiveTranslatorMod.LOGGER.info("Loaded {} cached translations.", CACHE.size());
            }
        } catch (IOException e) {
            LiveTranslatorMod.LOGGER.error("Failed to load translation cache", e);
        }
    }

    public static void saveAsync() {
        new Thread(TranslationCache::save, "ThaiTranslator-CacheSaver").start();
    }

    public static synchronized void save() {
        try {
            String json = GSON.toJson(CACHE);
            Files.writeString(CACHE_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LiveTranslatorMod.LOGGER.error("Failed to save translation cache", e);
        }
    }
}
