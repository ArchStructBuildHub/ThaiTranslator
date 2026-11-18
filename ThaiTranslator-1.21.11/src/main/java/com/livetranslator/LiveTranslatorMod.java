package com.livetranslator;

import com.livetranslator.client.KeybindHandler;
import com.livetranslator.config.TranslatorConfig;
import com.livetranslator.core.TranslationCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = LiveTranslatorMod.MOD_ID, dist = Dist.CLIENT)
public class LiveTranslatorMod {
    public static final String MOD_ID = "thaitranslator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean isTranslationEnabled = true;

    public LiveTranslatorMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, TranslatorConfig.SPEC);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(KeybindHandler::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(KeybindHandler::onClientTick);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        TranslationCache.load();
        Runtime.getRuntime().addShutdownHook(new Thread(TranslationCache::save));
        LOGGER.info("Thai Translator by NCchaNon initialized! Press F8 to toggle translation.");
    }
}
