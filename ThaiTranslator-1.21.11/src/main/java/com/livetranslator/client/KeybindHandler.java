package com.livetranslator.client;

import com.livetranslator.LiveTranslatorMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.thaitranslator.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.thaitranslator"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_KEY.consumeClick()) {
            LiveTranslatorMod.isTranslationEnabled = !LiveTranslatorMod.isTranslationEnabled;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                String status = LiveTranslatorMod.isTranslationEnabled
                        ? "§a[Thai Translator] เปิดการแปลภาษาไทยแล้ว"
                        : "§c[Thai Translator] ปิดการแปลชั่วคราว (แสดงภาษาต้นฉบับ)";
                mc.player.displayClientMessage(Component.literal(status), true);
            }
        }
    }
}
