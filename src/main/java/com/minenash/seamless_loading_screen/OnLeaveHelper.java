package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Takes the screenshot when leaving a world and only then lets the game continue leaving.
 */
public class OnLeaveHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Frames to wait after switching to first person so the change is visible in the screenshot
    private static final int FRAMES_BEFORE_CAPTURE = 2;
    // Leave anyway if no frame with the level was rendered in time
    private static final long CAPTURE_TIMEOUT_MS = 2000;

    private static boolean pending = false;
    private static int framesWaited = 0;
    private static long startTime = 0;
    private static Runnable onceFinished = () -> {};
    // The image only comes back from the GPU a few frames after the capture, closing the game has to wait for it
    private static CompletableFuture<Void> saving = CompletableFuture.completedFuture(null);

    /**
     * Whether this world session already has its screenshot, so later disconnect paths don't take another one.
     */
    public static boolean captured = false;

    /**
     * Starts the screenshot and runs the given task on the main thread once it was taken.
     */
    public static void beginScreenshotTask(Runnable runnable) {
        Minecraft client = Minecraft.getInstance();

        if (ScreenshotLoader.displayMode == DisplayMode.FREEZE || captured || client.level == null) {
            runnable.run();
            return;
        }

        if (pending) {
            Runnable previous = onceFinished;
            onceFinished = () -> {
                previous.run();
                runnable.run();
            };
            return;
        }

        pending = true;
        framesWaited = 0;
        startTime = Util.getMillis();
        onceFinished = runnable;

        client.options.setCameraType(CameraType.FIRST_PERSON);
    }

    /**
     * Called right after the level was rendered into the main target, before post effects and the GUI.
     */
    public static void onLevelRendered(RenderTarget target) {
        if (!pending || ++framesWaited < FRAMES_BEFORE_CAPTURE) return;

        Path file = ScreenshotLoader.getFile();
        Path iconFile = SeamlessLoadingScreenConfig.get().updateWorldIcon ? getWorldIconFile() : null;

        if (file != null) {
            CompletableFuture<Void> saved = new CompletableFuture<>();
            saving = saved.orTimeout(CAPTURE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            Screenshot.takeScreenshot(target, image -> Util.ioPool().execute(() -> {
                try {
                    save(image, file, iconFile);
                } finally {
                    saved.complete(null);
                }
            }));
        }

        finish();
    }

    public static void tick() {
        if (pending && Util.getMillis() - startTime > CAPTURE_TIMEOUT_MS) {
            LOGGER.warn("[SeamlessLoadingScreen]: No frame was rendered in time, leaving without a screenshot");
            finish();
        }
    }

    /**
     * Whether a screenshot is still being taken or written to disk.
     */
    public static boolean isBusy() {
        return pending || !saving.isDone();
    }

    /**
     * Runs the task on the main thread once the last screenshot is on disk, or failed to get there in time.
     */
    public static void afterSaving(Runnable task) {
        saving.whenComplete((result, error) -> Minecraft.getInstance().execute(task));
    }

    public static void resetForNewSession() {
        captured = false;
    }

    private static void finish() {
        pending = false;
        captured = true;

        Runnable task = onceFinished;
        onceFinished = () -> {};

        // Never leave the world in the middle of rendering a frame
        Minecraft.getInstance().execute(task);
    }

    private static Path getWorldIconFile() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        return server != null ? server.getWorldScreenshotFile().orElse(null) : null;
    }

    private static void save(NativeImage image, Path file, Path iconFile) {
        try (image) {
            Files.createDirectories(file.getParent());
            image.writeToFile(file);

            if (SeamlessLoadingScreenConfig.get().archiveScreenshots) {
                String name = file.getFileName().toString();
                String archiveName = name.substring(0, name.length() - 4) + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".png";
                Path archiveFile = file.getParent().getParent().resolve("archive").resolve(archiveName);

                Files.createDirectories(archiveFile.getParent());
                image.writeToFile(archiveFile);
            }

            if (iconFile != null) updateIcon(iconFile, image);
        } catch (IOException e) {
            LOGGER.error("[SeamlessLoadingScreen]: Unable to save the screenshot taken on leaving the world [Name: {}]", file, e);
        }
    }

    private static void updateIcon(Path iconFile, NativeImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int x = 0;
        int y = 0;
        if (width > height) {
            x = (width - height) / 2;
            width = height;
        } else {
            y = (height - width) / 2;
            height = width;
        }

        try (NativeImage icon = new NativeImage(64, 64, false)) {
            image.resizeSubRectTo(x, y, width, height, icon);
            icon.writeToFile(iconFile);
        }
    }
}
