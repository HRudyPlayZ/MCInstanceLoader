package com.hrudyplayz.mcinstanceloader;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.hrudyplayz.mcinstanceloader.utils.LogHelper;

public class Config {
// This class will handle every setting and config the user can set to change the mod's behavior.

    public static String configFolder = "config" + File.separator + ModProperties.MODID + File.separator;

    public static boolean verboseMode;
    public static boolean skipFileDisabling;
    public static boolean deleteInsteadOfRenaming;
    public static int maxAmountOfWebRedirections;

    public static String[] mainMenuClassPaths;
    public static int closeGameTimer;
    public static int amountOfDisplayedErrors;
    public static String[] successMessage;

    public static void createConfigFile() {
    // Creates the config file using Forge's system to allow to configure some stuff in the mod.

        LogHelper.info("Generating the config file...");

        File path = new File(configFolder + "config.cfg");
        Configuration config = new Configuration(path);
        config.load();

        verboseMode = config.getBoolean("useVerboseMode", "behavior", false, "Enable this to make the mod log every action it does. Useful for debugging.");
        skipFileDisabling = config.getBoolean("skipFileDisabling", "behavior", false, "Whether to skip the step that disables the pack.mcinstance file and deletes the temp folder. Useful for pack devs.");
        deleteInsteadOfRenaming = config.getBoolean("deleteMcInstanceDirectly", "behavior", false, "Wheter to delete the pack.mcinstance file instead of renaming it.");
        maxAmountOfWebRedirections = config.getInt("maxAmountOfRedirections", "behavior", 10, 0, 255, "How many times can a web page redirect you to another address?");

        mainMenuClassPaths = config.getStringList("menuClassPaths", "gui", new String[]{"net.minecraft.client.gui.GuiMainMenu"}, "List of Java class paths for menus that will get interrupted by this mod's GUI. May be useful for mods that change the main menu.");
        closeGameTimer = config.getInt("closeGameTimer","gui", 10, 0, 32767, "Delay before automatically closing the game on the success/fail screen. Set to 0 to disable.");
        amountOfDisplayedErrors = config.getInt("amountOfDisplayedErrors", "gui", 5, 0, 255, "Maximum number of errors that can be displayed on the error screen at once.");
        successMessage = config.getStringList("successMessage", "gui", new String[]{"The modpack succcesfully installed.", "In order to see the applied changes, please restart your game."}, "Sentencce displayed to the player after the pack finishes installing.");

        config.save();
    }
}
