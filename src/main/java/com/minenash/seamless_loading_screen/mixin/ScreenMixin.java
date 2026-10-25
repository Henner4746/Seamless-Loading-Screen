package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The connecting and level loading screens draw panorama, blur and menu background in that order;
 * the screenshot takes the place of the panorama and the tint the place of the menu background.
 */
@Mixin(value = Screen.class, priority = 900)
public abstract class ScreenMixin {
    @Shadow
    public int width;
    @Shadow
    public int height;

    @Inject(method = "extractPanorama", at = @At("HEAD"), cancellable = true)
    private void drawScreenshotInsteadOfPanorama(GuiGraphicsExtractor graphics, float partialTick, CallbackInfo ci) {
        if (!ScreenshotLoader.shouldReplaceBackground()) return;

        graphics.fill(0, 0, width, height, 0xFF000000);
        ScreenshotLoader.drawScreenshot(graphics, width, height, 0xFFFFFFFF);
        // The blur only affects strata before the current one
        graphics.nextStratum();
        ci.cancel();
    }

    @Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void blurOnlyIfEnabled(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (ScreenshotLoader.shouldReplaceBackground() && !SeamlessLoadingScreenConfig.get().enableScreenshotBlur) ci.cancel();
    }

    @Inject(method = "extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true)
    private void drawTintInsteadOfMenuBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!ScreenshotLoader.shouldReplaceBackground()) return;

        ScreenshotLoader.drawTint(graphics, width, height, 1.0F);
        ci.cancel();
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void stopBackgroundReplacement(CallbackInfo ci) {
        if ((Object) this instanceof LevelLoadingScreen) ScreenshotLoader.endBackgroundReplacment();
    }
}
