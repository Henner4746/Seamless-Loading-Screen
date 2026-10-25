package com.minenash.seamless_loading_screen.test;

import com.minenash.seamless_loading_screen.DisplayMode;
import com.minenash.seamless_loading_screen.FadeScreen;
import com.minenash.seamless_loading_screen.ScreenshotLoader;
import com.minenash.seamless_loading_screen.ServerInfoExtension;
import com.minenash.seamless_loading_screen.config.SeamlessLoadingScreenConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Leaves a world, checks that the screenshot was saved, rejoins it and checks the loading screen and fade.
 * Screenshots end up in build/run/clientGameTest/screenshots for a visual check.
 */
public class SeamlessLoadingScreenClientTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        testServerDisplayModeIsStored(context);
        testServerScreenLayout(context);
        testConfigScreenOpens(context);
        testLeaveAndRejoin(context);
    }

    private static void testServerDisplayModeIsStored(ClientGameTestContext context) {
        context.runOnClient(client -> {
            ServerData server = new ServerData("Test", "localhost", ServerData.Type.OTHER);
            check(((ServerInfoExtension) server).getStoredDisplayMode() == SeamlessLoadingScreenConfig.get().defaultServerMode, "a new server does not use the default display mode from the settings");
            ((ServerInfoExtension) server).setDisplayMode(DisplayMode.FREEZE);

            CompoundTag tag = server.write();
            ServerData read = ServerData.read(tag);

            check(((ServerInfoExtension) read).getDisplayMode() == DisplayMode.FREEZE, "display mode was not stored in servers.dat");
        });
    }

    private static void testServerScreenLayout(ClientGameTestContext context) {
        context.setScreen(() -> new ManageServerScreen(new TitleScreen(), Component.literal("Edit Server Info"), confirmed -> {}, new ServerData("Test", "localhost", ServerData.Type.OTHER)));
        context.waitTicks(2);
        context.takeScreenshot("sls-edit-server");
        context.setScreen(TitleScreen::new);
    }

    private static void testConfigScreenOpens(ClientGameTestContext context) {
        context.setScreen(() -> SeamlessLoadingScreenConfig.getInstance().generateScreen(new TitleScreen()));
        context.waitTicks(2);
        context.takeScreenshot("sls-config");
        context.setScreen(TitleScreen::new);
    }

    private static void testLeaveAndRejoin(ClientGameTestContext context) {
        TestSingleplayerContext singleplayer = context.worldBuilder().create();
        singleplayer.getConnection().waitForChunksRender();

        Path file = context.computeOnClient(client -> ScreenshotLoader.getFile());
        check(file != null, "creating a world did not choose a screenshot file");
        String levelId = singleplayer.getWorldSave().getSaveDirectory().getFileName().toString();
        check(file.endsWith(Path.of("screenshots", "worlds", "singleplayer", levelId + ".png")), "unexpected screenshot path " + file);
        context.takeScreenshot("sls-ingame");

        // Same call as the pause menu's quit button
        context.runOnClient(client -> client.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE));
        context.waitForScreen(TitleScreen.class);
        context.waitFor(client -> Files.isRegularFile(file));

        context.runOnClient(client -> client.createWorldOpenFlows().openWorld(levelId, () -> {}));
        context.waitFor(client -> client.gui.screen() instanceof LevelLoadingScreen || client.gui.screen() instanceof FadeScreen);
        check(context.computeOnClient(client -> ScreenshotLoader.loaded), "the saved screenshot was not loaded when rejoining");
        if (context.computeOnClient(client -> client.gui.screen() instanceof LevelLoadingScreen)) {
            context.takeScreenshot("sls-loading-screen");
        }

        context.waitForScreen(FadeScreen.class);
        context.waitTicks(10);
        context.takeScreenshot("sls-fade-waiting");

        int fade = SeamlessLoadingScreenConfig.get().fade;
        int time = SeamlessLoadingScreenConfig.get().time;
        context.waitTicks(time - fade / 2 - 10);
        context.takeScreenshot("sls-fade-half");

        context.waitFor(client -> client.gui.screen() == null);
        context.takeScreenshot("sls-after-fade");

        context.runOnClient(client -> client.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE));
        context.waitForScreen(TitleScreen.class);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
