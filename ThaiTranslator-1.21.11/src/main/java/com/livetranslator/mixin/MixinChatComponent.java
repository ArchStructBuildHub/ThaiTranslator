package com.livetranslator.mixin;

import com.livetranslator.LiveTranslatorMod;
import com.livetranslator.config.TranslatorConfig;
import com.livetranslator.core.TranslationManager;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class MixinChatComponent {

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD")
    )
    private void onChatMessage(Component chatComponent, MessageSignature signature, GuiMessageTag tag, CallbackInfo ci) {
        if (!LiveTranslatorMod.isTranslationEnabled || !TranslatorConfig.BILINGUAL_CHAT.get()) {
            return;
        }

        String raw = chatComponent.getString();
        if (raw.startsWith("↳ [TH] ")) return;

        TranslationManager.translateAsync(raw).thenAccept(translated -> {
            if (translated != null && !translated.equals(raw)) {
                Minecraft.getInstance().execute(() -> {
                    Component translatedLine = Component.literal("§7↳ §b[TH] §f" + translated);
                    ((ChatComponent) (Object) this).addMessage(translatedLine);
                });
            }
        });
    }
}
