package com.minenash.seamless_loading_screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class SeamlessLoadingScreenFabric implements ClientModInitializer {

    public static final KeyMapping OPEN_SETTINGS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "seamless_loading_screen.keybind.config",
//? if >=26.3 {
            // 26.3 moved keyboard input to SDL; the key type was renamed with it
            InputConstants.Type.KEYBOARD,
//?} else {
            /*InputConstants.Type.KEYSYM,
*///?}
            InputConstants.KEY_G,
            KeyMapping.Category.MISC));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_SETTINGS.consumeClick()) SeamlessLoadingScreen.openSettingsScreen(client);

            OnLeaveHelper.tick();
        });

        SeamlessLoadingScreen.onInitializeClient();
    }
}
