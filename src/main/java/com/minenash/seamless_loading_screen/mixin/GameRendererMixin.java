package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    public abstract RenderTarget mainRenderTarget();

    // Same spot vanilla uses for the world icon: the level is drawn, post effects and the GUI are not
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V"))
    private void attemptToTakeScreenshot(CallbackInfo ci) {
        OnLeaveHelper.onLevelRendered(mainRenderTarget());
    }
}
