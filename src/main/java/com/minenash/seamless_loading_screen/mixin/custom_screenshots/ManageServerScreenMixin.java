package com.minenash.seamless_loading_screen.mixin.custom_screenshots;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(ManageServerScreen.class)
public abstract class ManageServerScreenMixin extends Screen {
    // Room for the display mode button below the resource pack button, with the usual 4 pixel gap
    @Unique
    private static final int SHIFT = 6;

    @Shadow
    @Final
    private ServerData serverData;

    protected ManageServerScreenMixin(Component title) {
        super(title);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"), index = 1)
    private int moveDoneAndCancelDown(int y) {
        return y + SHIFT;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addDisplayModeButton(CallbackInfo info) {
        ServerInfoExtension server = (ServerInfoExtension) serverData;

        this.addRenderableWidget(
                CycleButton.<DisplayMode>builder(mode -> Component.translatable("seamless_loading_screen.config.displayMode." + mode.name().toLowerCase(Locale.ROOT)), server.getStoredDisplayMode())
                        .withValues(DisplayMode.values())
                        .create(this.width / 2 - 100, this.height / 4 + 96, 200, 20,
                                Component.translatable("seamless_loading_screen.config.displayMode"),
                                (button, mode) -> server.setDisplayMode(mode))
        );
    }
}
