package com.minenash.seamless_loading_screen.mixin;

import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {
    // Chunks that have not started loading are drawn see-through, so the screenshot behind the map stays visible
    @ModifyArg(method = "extractChunksForRendering", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"), index = 4)
    private static int seeThroughEmptyChunks(int color) {
        return color == 0xFF000000 ? 0xAA000000 : color;
    }
}
