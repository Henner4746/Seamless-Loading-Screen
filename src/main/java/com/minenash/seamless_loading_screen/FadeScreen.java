package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Shown after the level finished loading: keeps the screenshot up while chunks load, then fades it into the world.
 */
public class FadeScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIDE_COLOR = 0x101010;

    private final int fadeTicks;
    private int ticksLeft;
    private Consumer<Boolean> callback;
    private boolean done;

    public FadeScreen(int totalTicks, int fadeTicks) {
        super(Component.translatable("seamless_loading_screen.screen.loading_chunks"));
        this.fadeTicks = Math.max(1, Math.min(totalTicks, fadeTicks));
        this.ticksLeft = totalTicks;
    }

    public FadeScreen then(Consumer<Boolean> callback) {
        this.callback = callback;

        return this;
    }

    @Override
    public void removed() {
        markDone(true);

        super.removed();
    }

    @Override
    protected void init() {
        if (SeamlessLoadingScreenConfig.get().playSoundEffect) {
            var id = Identifier.tryParse(SeamlessLoadingScreenConfig.get().soundEffect);

            if (id != null) {
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(SoundEvents.UI_TOAST_OUT);

                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, SeamlessLoadingScreenConfig.get().soundPitch, SeamlessLoadingScreenConfig.get().soundVolume));
            } else {
                LOGGER.error("[SeamlessLoadingScreen]: Unable to parse the SoundEffect due to it not being a valid Identifier. [Value: {}]", SeamlessLoadingScreenConfig.get().soundEffect);
            }
        }
    }

    @Override
    public void tick() {
        if (--ticksLeft <= 0) markDone(false);
    }

    private void markDone(boolean forceClosed) {
        if (this.done) return;

        this.done = true;

        this.ticksLeft = 0;

        if (this.callback != null) this.callback.accept(forceClosed);
        if (this.callback == null && !forceClosed && this.minecraft.gui.screen() == this)
            this.minecraft.gui.setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // The level behind must stay visible so the screenshot can fade into it
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (ticksLeft <= 0) return;

        float remaining = ticksLeft - partialTick;
        boolean doFade = remaining <= fadeTicks;
        float alpha = doFade ? Mth.clamp(remaining / fadeTicks, 0.0F, 1.0F) : 1.0F;

        if (ScreenshotLoader.loaded) {
            int w = (int) (ScreenshotLoader.imageRatio * height);
            int x = width / 2 - w / 2;

            if (w < width) {
                graphics.fill(0, 0, x, height, ARGB.color(alpha, SIDE_COLOR));
                graphics.fill(x + w, 0, width, height, ARGB.color(alpha, SIDE_COLOR));
            }

            ScreenshotLoader.drawScreenshot(graphics, width, height, ARGB.white(alpha));
        } else {
            graphics.fill(0, 0, width, height, ARGB.color(alpha, SIDE_COLOR));
        }

        if (!doFade && SeamlessLoadingScreenConfig.get().enableScreenshotBlur) {
            graphics.nextStratum();
            graphics.blurBeforeThisStratum();
        }

        ScreenshotLoader.drawTint(graphics, width, height, alpha);

        if (!doFade) {
            graphics.centeredText(this.font, this.title, width / 2, 70, 0xFFFFFFFF);
            String progress = String.valueOf(this.minecraft.levelExtractor.countRenderedSections());
            graphics.centeredText(this.font, progress, width / 2, 90, 0xFFFFFFFF);
        }
    }
}
