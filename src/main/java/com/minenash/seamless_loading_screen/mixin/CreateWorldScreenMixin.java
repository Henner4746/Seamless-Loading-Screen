package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Shadow
    @Final
    private WorldCreationUiState uiState;

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void setFilename(CallbackInfo ci) {
        ScreenshotLoader.displayMode = DisplayMode.ENABLED;
        ScreenshotLoader.setScreenshot(uiState.getTargetFolder());
    }
}
