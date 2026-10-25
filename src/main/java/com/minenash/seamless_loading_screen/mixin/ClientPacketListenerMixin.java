package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.OnLeaveHelper;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    // Only joining a world fades in, respawns and dimension changes keep the vanilla loading screen
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void startFadeOnJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        OnLeaveHelper.resetForNewSession();
        if (ScreenshotLoader.loaded) ScreenshotLoader.inFade = true;
    }
}
