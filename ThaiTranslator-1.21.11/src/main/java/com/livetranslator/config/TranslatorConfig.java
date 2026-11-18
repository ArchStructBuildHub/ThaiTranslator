package com.livetranslator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TranslatorConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> PROVIDER;
    public static final ModConfigSpec.ConfigValue<String> GOOGLE_API_KEY;
    public static final ModConfigSpec.ConfigValue<String> GEMINI_API_KEY;
    public static final ModConfigSpec.ConfigValue<String> GEMINI_MODEL;
    public static final ModConfigSpec.BooleanValue BILINGUAL_CHAT;

    static {
        BUILDER.push("General Settings");

        PROVIDER = BUILDER
                .comment("เลือกผู้ให้บริการแปลภาษา: 'GOOGLE' หรือ 'GEMINI'")
                .define("provider", "GOOGLE");

        GOOGLE_API_KEY = BUILDER
                .comment("ใส่ Google Cloud Translation API Key ของคุณที่นี่")
                .define("googleApiKey", "");

        GEMINI_API_KEY = BUILDER
                .comment("ใส่ Google Gemini API Key ของคุณที่นี่")
                .define("geminiApiKey", "");

        GEMINI_MODEL = BUILDER
                .comment("รุ่นโมเดล Gemini ที่ใช้แปล (แนะนำ gemini-2.5-flash เพื่อความเร็ว)")
                .define("geminiModel", "gemini-2.5-flash");

        BILINGUAL_CHAT = BUILDER
                .comment("แสดงข้อความต้นฉบับคู่กับคำแปลไทยในช่องแชท (true = แสดงคู่กัน, false = แทนที่ทั้งหมด)")
                .define("bilingualChat", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
