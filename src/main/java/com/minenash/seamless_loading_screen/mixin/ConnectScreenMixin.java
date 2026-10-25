package com.minenash.seamless_loading_screen.mixin;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void getImage(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean isQuickPlay, @Nullable TransferState transferState, CallbackInfo ci) {
        ScreenshotLoader.displayMode = data != null ? ((ServerInfoExtension) data).getDisplayMode() : DisplayMode.ENABLED;
        ScreenshotLoader.setScreenshot(address.getHost(), address.getPort());
    }
}
