package net.ccbluex.liquidbounce;

import net.ccbluex.liquidbounce.utils.HttpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.ccbluex.liquidbounce.features.command.CommandManager;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui;
import net.ccbluex.liquidbounce.injection.TransformerManager;
import net.ccbluex.liquidbounce.features.module.ModuleManager;
import net.ccbluex.liquidbounce.ui.client.hud.DefaultHUD;
import net.ccbluex.liquidbounce.ui.client.hud.HUD;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.notifications.Notification;
import net.ccbluex.liquidbounce.utils.reflect.Mapping;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import java.net.URL;

public class LiquidBounce {

    public static LiquidBounce instance;

    public static final String CLIENT_NAME = "LiquidReflect";
    public static final int CLIENT_VERSION = 1;
    public static final String CLIENT_AUTHOR = "CCBlueX, cubk";
    public static boolean init = false;

    public final Logger LOGGER = LogManager.getLogger(CLIENT_NAME);

    public final ModuleManager moduleManager = new ModuleManager();
    public final EventManager eventManager = new EventManager();
    public final CommandManager commandManager = new CommandManager();
    public static final TransformerManager transformerManager = new TransformerManager();
    public HUD hud;
    public ClickGui clickGui;

    public LiquidBounce() {
        instance = this;
        Logger LOGGER = LogManager.getLogger("Bootstrap");
        try {
            LOGGER.info("Transforming...");
            transformerManager.registerTransformers();
        } catch (Exception e) {
            LOGGER.catching(e);
        }
    }

    public static void run() {
        new LiquidBounce();
    }

    public void startClient() {
        init = true;
        LOGGER.info("Initializing Client...");

        Fonts.init();

        LOGGER.info("Font Loaded.");

        hud = new DefaultHUD();
        commandManager.registerCommands();
        moduleManager.registerModules();
        eventManager.register(moduleManager);
        eventManager.register(commandManager);
        eventManager.register(new Mapping());
        eventManager.register(new RotationUtils());
        clickGui = new ClickGui();
        LOGGER.info("Client Loaded.");

        Display.setTitle(CLIENT_NAME + " b" + CLIENT_VERSION + " | DEVELOPMENT BUILD | " + Display.getTitle());

        try {
            HttpUtils.getFromURL(new URL("http://localhost:6666/api/close"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        LOGGER.info(message);
    }

    public void stopClient() {

    }
}