package com.minenash.seamless_loading_screen.mixin.custom_screenshots;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerData.class)
public abstract class ServerDataMixin implements ServerInfoExtension {
    @Shadow
    public String ip;

    // Servers without a stored mode follow the default from the settings
    @Unique
    private DisplayMode seamless_loading_screen$displayMode = SeamlessLoadingScreenConfig.get().defaultServerMode;

    @Inject(method = "read", at = @At("RETURN"))
    private static void deserialize(CompoundTag tag, CallbackInfoReturnable<ServerData> callback) {
        tag.getString("screenshotDisplayMode").ifPresent(mode -> {
            try {
                ((ServerDataMixin) (Object) callback.getReturnValue()).seamless_loading_screen$displayMode = DisplayMode.valueOf(mode);
            } catch (IllegalArgumentException ignored) {
            }
        });
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void serialize(CallbackInfoReturnable<CompoundTag> callback) {
        callback.getReturnValue().putString("screenshotDisplayMode", seamless_loading_screen$displayMode.toString());
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyFrom(ServerData info, CallbackInfo callback) {
        seamless_loading_screen$displayMode = ((ServerDataMixin) (Object) info).seamless_loading_screen$displayMode;
    }

    @Override
    public DisplayMode getDisplayMode() {
        for (String blacklistedAddress : SeamlessLoadingScreenConfig.get().blacklistedAddresses) {
            if (!blacklistedAddress.isBlank() && this.ip.contains(blacklistedAddress)) {
                return DisplayMode.DISABLED;
            }
        }

        return seamless_loading_screen$displayMode;
    }

    @Override
    public DisplayMode getStoredDisplayMode() {
        return seamless_loading_screen$displayMode;
    }

    @Override
    public void setDisplayMode(DisplayMode mode) {
        seamless_loading_screen$displayMode = mode;
    }
}
