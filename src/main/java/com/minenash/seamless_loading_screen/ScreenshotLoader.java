package com.minenash.seamless_loading_screen;

import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

public class ScreenshotLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern RESERVED_FILENAMES_PATTERN = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", Pattern.CASE_INSENSITIVE);
    public static final Identifier SCREENSHOT = Identifier.fromNamespaceAndPath(SeamlessLoadingScreen.MODID, "screenshot");
    public static double imageRatio = 1;
    public static boolean loaded = false;
    public static DisplayMode displayMode = DisplayMode.ENABLED;
    public static boolean inFade = false;
    public static boolean replacebg = false;
    private static Path file = null;

    public static void endBackgroundReplacment() {
        replacebg = false;
    }

    public static boolean shouldReplaceBackground() {
        return replacebg && loaded;
    }

    /**
     * Where the screenshot of the world currently being joined is stored, or null if no world was joined through a hooked path.
     */
    public static Path getFile() {
        return file;
    }

    private static void setFileName(String subPath) {
        User user = Minecraft.getInstance().getUser();

        UUID offlineUUID = UUIDUtil.createOfflinePlayerUUID(user.getName());
        UUID sessionUUID = user.getProfileId();

        String baseDir = "screenshots/worlds/";

        if (SeamlessLoadingScreenConfig.get().saveScreenshotsByUsername) {
            String userDir = (sessionUUID != null && !sessionUUID.equals(offlineUUID))
                    ? cleanFileName(user.getName())
                    : "offline";

            baseDir = "screenshots/" + userDir + "/worlds/";
        }

        Path gameDir = PlatformFunctions.getGameDir();
        file = gameDir.resolve(baseDir + subPath);
        migrateLegacyFile(gameDir.resolve(baseDir + "screenshots/worlds/" + subPath));
        setScreenshot();
    }

    /**
     * Versions up to 2.2.1 prefixed the directory twice and stored screenshots under screenshots/worlds/screenshots/worlds/.
     */
    private static void migrateLegacyFile(Path legacyFile) {
        if (Files.exists(file) || !Files.isRegularFile(legacyFile)) return;

        try {
            Files.createDirectories(file.getParent());
            Files.move(legacyFile, file);
        } catch (IOException e) {
            LOGGER.warn("[SeamlessLoadingScreen]: Unable to move the screenshot from {} to {}", legacyFile, file, e);
        }
    }

    public static void setScreenshot(String address, int port) {
        setFileName("servers/" + cleanFileName(address) + "_" + port + ".png");
    }

    public static void setScreenshot(String worldName) {
        setFileName("singleplayer/" + worldName + ".png");
    }

    public static void setRealmScreenshot(String realmName) {
        setFileName("realms/" + cleanFileName(realmName) + ".png");
    }

    private static void setScreenshot() {
        loaded = false;

        if (displayMode == DisplayMode.DISABLED) return;

        try (InputStream in = Files.newInputStream(file)) {
            if (PlatformFunctions.isDevEnv()) {
                LOGGER.info("[SeamlessLoadingScreen]: Loading screenshot {}", file);
            }

            DynamicTexture texture = new DynamicTexture(() -> "Seamless Loading Screen screenshot", NativeImage.read(in));
            Minecraft.getInstance().getTextureManager().register(SCREENSHOT, texture);
            imageRatio = texture.getPixels().getWidth() / (double) texture.getPixels().getHeight();
            loaded = true;
            replacebg = true;
        } catch (NoSuchFileException ignore) {
        } catch (IOException e) {
            LOGGER.error("[SeamlessLoadingScreen]: An Issue has occurred when attempting to set a Screenshot: [name: {}]", file, e);
        }
    }

    private static String cleanFileName(String fileName) {
        for (char c : SharedConstants.ILLEGAL_FILE_CHARACTERS) fileName = fileName.replace(c, '_');

        if (RESERVED_FILENAMES_PATTERN.matcher(fileName).matches()) fileName = "_" + fileName + "_";

        if (fileName.length() > 255 - 4) fileName = fileName.substring(0, 255 - 4);

        return fileName;
    }

    /**
     * Draws the screenshot centered at full height, keeping its aspect ratio.
     */
    public static void drawScreenshot(GuiGraphicsExtractor graphics, int width, int height, int color) {
        int w = (int) (imageRatio * height);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCREENSHOT, width / 2 - w / 2, 0, 0.0F, 0.0F, w, height, w, height, color);
    }

    public static void drawTint(GuiGraphicsExtractor graphics, int width, int height, float fadeValue) {
        Color color = SeamlessLoadingScreenConfig.get().tintColor;
        int alpha = Math.round(255 * (SeamlessLoadingScreenConfig.get().tintStrength * fadeValue));

        graphics.fill(0, 0, width, height, ARGB.color(alpha, color.getRed(), color.getGreen(), color.getBlue()));
    }
}
