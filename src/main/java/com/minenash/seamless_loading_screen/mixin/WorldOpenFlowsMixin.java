package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The world list, quick play and other mods all open singleplayer worlds through here
@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
    @Inject(method = "openWorld(Ljava/lang/String;Ljava/lang/Runnable;)V", at = @At("HEAD"))
    public void setFilename(String levelId, Runnable onCancel, CallbackInfo info) {
        ScreenshotLoader.displayMode = DisplayMode.ENABLED;
        ScreenshotLoader.setScreenshot(levelId);
    }
}
