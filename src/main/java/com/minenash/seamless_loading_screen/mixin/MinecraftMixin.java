package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class MinecraftMixin {
    @Shadow
    public @Nullable ClientLevel level;

    @Unique
    private boolean seamless_loading_screen$leaving = false;
    @Unique
    private boolean seamless_loading_screen$stopping = false;

    @Shadow
    public abstract void disconnectFromWorld(Component message);

    @Shadow
    public abstract void stop();

    // Every way of quitting through the menus ends up here
    @Inject(method = "disconnectFromWorld", at = @At("HEAD"), cancellable = true)
    private void screenshotBeforeLeaving(Component message, CallbackInfo ci) {
        if (seamless_loading_screen$leaving || level == null) return;

        seamless_loading_screen$leaving = true;
        OnLeaveHelper.beginScreenshotTask(() -> {
            disconnectFromWorld(message);
            seamless_loading_screen$leaving = false;
        });
        ci.cancel();
    }

    @Inject(method = "stop", at = @At("HEAD"), cancellable = true)
    private void screenshotBeforeClosing(CallbackInfo ci) {
        if (!seamless_loading_screen$stopping && level != null) {
            seamless_loading_screen$stopping = true;
            OnLeaveHelper.beginScreenshotTask(() -> OnLeaveHelper.afterSaving(this::stop));
        }

        // Closing the window calls stop() every frame, so keep the game running until the screenshot is on disk
        if (OnLeaveHelper.isBusy()) ci.cancel();
    }
}
