package com.hrudyplayz.mcinstanceloader;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.hrudyplayz.mcinstanceloader.utils.LogHelper;

public class Config {
// This class will handle every setting and config the user can set to change the mod's behavior.

    public static String configFolder = "config" + File.separator + ModProperties.MODID + File.separator;

    public static int updateCheckerMode;
    public static boolean verboseMode;
    public static boolean skipFileDisabling;
    public static boolean deleteInsteadOfRenaming;
    public static boolean disableStopModRepostsCheck;
    public static boolean disableCache;
    public static boolean disableAutomaticZipCreation;
    public static int connectionTimeout;

    public static String curseforgeURL;
    public static String curseforgeAPIKey;

    public static String[] mainMenuClassPaths;
    public static int closeGameTimer;
    public static int amountOfDisplayedErrors;
    public static String[] successMessage;
    public static boolean allowSSLCertificateBypass;

    public static String CATEGORY_BEHAVIOR = "Behavior";
    public static String CATEGORY_GUI = "GUI";

    public static void createConfigFile() {
    // Creates the config file using Forge's system to allow to configure some stuff in the mod.

        LogHelper.info("Generating the config file...");

        File path = new File(configFolder + "config.cfg");
        Configuration config = new Configuration(path);
        config.load();

        updateCheckerMode = config.getInt("Update checker: mode", CATEGORY_BEHAVIOR, 0, 0, 3, "Defines the mode to use for the update checker. 0 = GUI with a continue button, 1 = GUI that cannot be skipped, 2 = Logs only, 3 = No update checker.");
        if (Main.side.equals("server") && updateCheckerMode < 2) updateCheckerMode = 2;

        verboseMode = config.getBoolean("Use verbose mode", CATEGORY_BEHAVIOR, false, "Enable this to make the mod log every action it does. Useful for debugging.");
        skipFileDisabling = config.getBoolean("Skip file disabling", CATEGORY_BEHAVIOR, false, "Whether to skip the step that disables the pack.mcinstance file and deletes the temp folder. Useful for pack devs.");
        deleteInsteadOfRenaming = config.getBoolean("Delete MCInstance directly", CATEGORY_BEHAVIOR, false, "Wheter to delete the pack.mcinstance file instead of renaming it.");
        disableStopModRepostsCheck = config.getBoolean("Disable StopModReposts check", CATEGORY_BEHAVIOR, false, "Whether to disable the StopModReposts check, used to prevent the use of malware sites. It's recommended to keep it enabled.");
        allowSSLCertificateBypass = config.getBoolean("Allow SSL Certificate bypass", CATEGORY_BEHAVIOR, false, "Whether to allow the bypass of SSL Certificates if this cause problems. This may fix download issues in some launchers.");
        disableCache = config.getBoolean("Disable the cache system", CATEGORY_BEHAVIOR, false, "Whether to disable the cache system, forcing every resource to be downloaded regardless of the cached value.");

        disableAutomaticZipCreation = config.getBoolean("Disable the automatic zipping system for the pack folder", CATEGORY_BEHAVIOR, false, "Whether to disable the automatic creation of the pack.mcinstance file from the pack folder.");
        if (deleteInsteadOfRenaming && !disableAutomaticZipCreation) disableAutomaticZipCreation = true;

        connectionTimeout = config.getInt("Web connection timeout", CATEGORY_BEHAVIOR, 100, 0, Integer.MAX_VALUE, "The amount of seconds the mod will wait to receive a response for downloads. Zero for no timeout at all (not recommended).");

        curseforgeURL = config.getString("CFCore proxy URL", CATEGORY_BEHAVIOR, "http://api-pocket.com/", "The URL to use for the CFCore API. Any proxy works. Leave blank to use the official CFCore one. Defaults to a simple proxy.");
        if (curseforgeURL.trim().length() == 0) curseforgeURL = "https://api.curseforge.com/";

        curseforgeAPIKey = config.getString("CFCore API key", CATEGORY_BEHAVIOR, "", "The API key to use if you use the official Curseforge API.");

        mainMenuClassPaths = config.getStringList("Menu class paths", CATEGORY_GUI, new String[]{"net.minecraft.client.gui.GuiMainMenu"}, "List of Java class paths for menus that will get interrupted by this mod's GUI. May be useful for mods that change the main menu.");
        closeGameTimer = config.getInt("Close game timer",CATEGORY_GUI, 10, 0, Integer.MAX_VALUE, "Delay before automatically closing the game on the success/fail screen. Set to 0 to disable.");
        amountOfDisplayedErrors = config.getInt("Maximum amount of displayed errors", CATEGORY_GUI, 5, 0, 64, "Maximum number of errors that can be displayed on the error screen at once.");
        successMessage = config.getStringList("Success message", CATEGORY_GUI, new String[]{"The modpack succcesfully installed.", "In order to see the applied changes, please restart your game."}, "Sentence displayed to the player after the pack finishes installing.");

        config.save();
    }
}
