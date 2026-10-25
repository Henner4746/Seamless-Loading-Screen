package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.FadeScreen;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// 26.x manages screens in Gui instead of Minecraft
@Mixin(value = Gui.class, priority = 900)
public abstract class GuiMixin {
    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private @Nullable Screen screen;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private @Nullable Screen fadeScreen(@Nullable Screen newScreen) {
        if (this.screen instanceof LevelLoadingScreen && ScreenshotLoader.inFade) {
            if (newScreen == null && minecraft.level != null && ScreenshotLoader.loaded) {
                return new FadeScreen(SeamlessLoadingScreenConfig.get().time, SeamlessLoadingScreenConfig.get().fade).then(forced -> {
                    if (!forced) setScreen(null);
                    ScreenshotLoader.inFade = false;
                });
            }

            if (newScreen != null && !(newScreen instanceof LevelLoadingScreen)) {
                LOGGER.warn("[SeamlessLoadingScreen] Fade screen has been skipped due to someone replacing the screen before we could add our own after {}", this.screen.getClass().getSimpleName());
                ScreenshotLoader.inFade = false;
                ScreenshotLoader.endBackgroundReplacment();
            }
        }

        // Joining failed or was cancelled: stop showing the screenshot in the menus
        if (newScreen instanceof TitleScreen || newScreen instanceof JoinMultiplayerScreen || newScreen instanceof SelectWorldScreen
                || newScreen instanceof DisconnectedScreen || newScreen instanceof RealmsMainScreen) {
            ScreenshotLoader.inFade = false;
            ScreenshotLoader.endBackgroundReplacment();
        }

        return newScreen;
    }
}
