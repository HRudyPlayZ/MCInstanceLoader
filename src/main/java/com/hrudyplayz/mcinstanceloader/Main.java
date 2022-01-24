package com.hrudyplayz.mcinstanceloader;

import java.io.File;
import java.util.Collections;
import java.util.Random;

import org.apache.logging.log4j.Level;

import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.common.ProgressManager;

import com.hrudyplayz.mcinstanceloader.utils.LogHelper;
import com.hrudyplayz.mcinstanceloader.utils.WebHelper;
import com.hrudyplayz.mcinstanceloader.utils.FileHelper;
import com.hrudyplayz.mcinstanceloader.utils.ZipHelper;
import com.hrudyplayz.mcinstanceloader.resources.PackConfigParser;
import com.hrudyplayz.mcinstanceloader.resources.ResourceObject;
import com.hrudyplayz.mcinstanceloader.gui.GuiOpenEventHandler;
import com.hrudyplayz.mcinstanceloader.gui.InfoGui;




@Mod(modid = ModProperties.MODID, name = ModProperties.NAME, version = ModProperties.VERSION)
public class Main {
// This class defines the main mod class, and registers stuff like Events.

    public static boolean shouldDoSomething = false; // Creates the shouldDoSomething variable used to check if the mod should display the end GUI.
    public static boolean hasErrorOccured = false; // Creates the hasErrorOccured boolean used to stop next steps from running and tell the GUI if it should be in error/success mode.
    public static String errorContext = ""; // Creates the public error context that any method can modify, used to tell more infos about a particular error.
    public static int errorCount = 0; // Number of errors encountered so far for the maximum count setting.

    public static String side; // Currently running side of the game, either "client" or "server".

    public static String[] blacklist; // List of strings that is later used to store the StopModReposts list.

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    // The preInit event, that gets called as soon as possible in Forge's loading. Basically the entire mod.


        // ===== STEP 0: Creating the config file, the menu information and the carryover folder =====
        FileHelper.overwriteFile( Config.configFolder + "details.log", new String[0]); // Clears the log file, so it can be reused.

        LogHelper.verboseInfo("The mod correctly entered the preInit stage.");
        makeFancyModInfo(event);
        Config.createConfigFile();

        // Registers the GuiOpenEventHandler when on client side, and sets the variable to easily check which side is running.
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            side = "client";
            MinecraftForge.EVENT_BUS.register(GuiOpenEventHandler.instance);
        }
        else side = "server";
        LogHelper.verboseInfo("The current side is " + side + " .");
        
        // If the carryover folder doesn't exist, it creates it with empty mods and config folders inside.
        if (!FileHelper.exists("carryover")) {
            LogHelper.info("The carryover folder didn't exist, created a blank one.");
            
            FileHelper.createDirectory("carryover");
            FileHelper.createDirectory("carryover" + File.separator + "mods");
            FileHelper.createDirectory("carryover" + File.separator + "config");
        }


        // ===== STEP 1: Delete the potential remnants of a failed installation =====
        if (FileHelper.exists(Config.configFolder + "temp")) { // Deletes the temp folder, if the mod couldn't do it before for some reasons.
            LogHelper.info("Found a leftover temp directory, removed it.");
            FileHelper.delete(Config.configFolder + "temp");
        }

        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.


        // ===== STEP 2: Extract the mcinstance file to the temp folder =====
        if (FileHelper.exists(Config.configFolder + "pack.mcinstance")) {
            LogHelper.info("Found pack.instance, starting the installation process.");

            shouldDoSomething = true; // The file exists and the results GUI should displayed (otherwise the mod won't do anything).

            if (!ZipHelper.extract(Config.configFolder + "pack.mcinstance", Config.configFolder + "temp" + File.separator)) throwError("Error while extracting the pack.mcinstance file.");
        }
        else LogHelper.info("Missing pack.mcinstance, skipping.");

        errorContext = ""; // Resets the errorContext, so it can be reused for the next step.
        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.


        // ===== STEP 3: Download resources from file =====
        if (!hasErrorOccured && FileHelper.exists(Config.configFolder + "temp" + File.separator + "resources.packconfig")) {
            LogHelper.info("Found a resources.packconfig file, starting the download process.");
            
            ResourceObject[] list = PackConfigParser.parseResources(Config.configFolder + "temp" + File.separator + "resources.packconfig");

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Downloading resource", list.length, true);

            // Grabs the current list of blacklisted sites from StopModReposts.
            LogHelper.verboseInfo("Getting the blacklist from StopModReposts.org...");
            if (WebHelper.downloadFile("https://api.stopmodreposts.org/sites.txt", Config.configFolder + "temp" + File.separator + "stopmodreposts.txt")) blacklist = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "stopmodreposts.txt");
            else throwError("Error while getting the blacklist from StopModReposts.");

            // For each object present in the resources list, it tries to download and check their hash, and displays the resource in a fancy format in the mod log.
            for (ResourceObject object : list) {
                progress.step(object.name);

                LogHelper.appendToLog(Level.INFO, "", true);
                LogHelper.appendToLog(Level.INFO, "==================================================", true);
                object.appendToLog();

                LogHelper.verboseInfo("Attempting to download the resource " + object.name + "...");

                if (object.downloadFile()) {
                    if (!object.checkHash()) throwError("Could not verify the hash of " + object.name + ".");
                }
                else throwError("Error while downloading " + object.name + ".");

                LogHelper.appendToLog(Level.INFO, "==================================================", true);

                errorContext = ""; // Resets the errorContext, so it can be reused for the next resource or the next step.
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }

        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.


        // ===== STEP 4: Replace the source files with the ones in the overrides folder =====
        String path = Config.configFolder + "temp" + File.separator + "overrides";

        // If there wasn't any error that occured on the previous steps.
        if (!hasErrorOccured && FileHelper.exists(path) && FileHelper.isDirectory(path)) {
            LogHelper.info("Moving the files from the overrides folder.");
            String[] fileList = FileHelper.listDirectory(Config.configFolder + "temp" + File.separator + "overrides", false);

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Moving overrides", fileList.length, true);

            // For every file/folder in the overrides folder, we move them to the root .minecraft folder.
            for (String s : fileList) {
                progress.step(s);

                // Protects the user saves, config and the carryover folder. So they can't accidentally get replaced by an empty directory.
                // Every other folder will get replaced, in order to allow packs to cleanup some scripts folder, or logs or whatever.
                if (!s.equals("carryover") && !s.equals("saves") && !s.equals("config") && !s.equals("mods")) {
                    LogHelper.verboseInfo("Replacing " + s + " from the root folder.");

                    if (!FileHelper.move(Config.configFolder + "temp" + File.separator + "overrides" + File.separator + s, s, true)) throwError("Error while moving the file " + s + " from the overrides folder.");
                }

                else {
                    LogHelper.verboseInfo("Merging " + s + " with the original folder.");

                    if (!FileHelper.move(Config.configFolder + "temp" + File.separator + "overrides" + File.separator + s, s, false)) throwError("Error while merging the file " + s + " from the overrides folder.");
                }

                errorContext = ""; // Resets the errorContext, so it can be reused for the next resource or the next step.
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }

        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.


        // ===== STEP 5: Replace the files with the ones from the carryover folder =====
        path = "carryover";
        if (!hasErrorOccured && shouldDoSomething && FileHelper.exists(path) && FileHelper.isDirectory(path)) {
            LogHelper.info("Copying the files from the carryover folder.");

            String[] fileList = FileHelper.listDirectory("carryover", false);

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Moving from carryover", fileList.length, true);

            // For every file/folder in the carryover folder, we copy them to the root .minecraft folder. We don't have to worry about cleaning files for now.
            for (String s : fileList) {
                progress.step(s);
                LogHelper.verboseInfo("Moving the " + s + " folder from carryover to the root folder.");

                if (!FileHelper.copy("carryover" + File.separator + s, s, false)) throwError("Error while copying the file " + s + " from the carryover folder.");

                errorContext = "";
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }


        // ===== STEP 6: Finalising setup (deleting files, throwing the success screen) =====

        // If there wasn't any error, it will throw the success screen and disable the mcinstance file, so the game will boot fine on the next launch.
        if (!hasErrorOccured) {
            for (int i = 0; i < Config.successMessage.length; i += 1) throwSuccess(Config.successMessage[i]);

            path = Config.configFolder + "pack.mcinstance";
            if (!Config.skipFileDisabling && FileHelper.exists(path)) { // If the config to skip the file disabling wasn't set, it will disable or remove it.
                if (Config.deleteInsteadOfRenaming) FileHelper.delete(path);
                else FileHelper.move(path, path + ".disabled", true);
            }
        }

        // Will delete the temp folder, unless the skip file disabling config is set, to allow for in-depth debugging after the game closes.
        path = Config.configFolder + "temp";
        if (!Config.skipFileDisabling && FileHelper.exists(path)) FileHelper.delete(path);
    }


    public void throwError (String text) {
    // Sets the final results screen to be an error screen. Adds an error any time it gets called.
    // If the amount of errors displayed is above the maximum limit, it adds to the more counter instead.

        // Sets the hasErrorOccured value to true, so the game will display an error screen and not overwrite any file after the error.
        hasErrorOccured = true;

        if (errorContext.length() > 0) text += " (" + errorContext + ")"; // Adds the error context, if there's any set.

        LogHelper.error(text); // Adds the error to the general game log and the mod log.

        text = "- " + text; // Formats the text to look like an error list.

        if (InfoGui.textList.size() <= 0) { // If this function gets called for the first time, it adds the error message and configures the GUI.
            InfoGui.textList.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.RED + "There was an issue processing the files.");
            InfoGui.buttonAmount = 2;
        }

        // Handles the maximum amount of displayed error limit, made so errors cannot overflow past the GUI screen.
        // If the maximum amount is reached, any error afterwards will just edit the errorCount value instead.
        if (InfoGui.textList.size() < Config.amountOfDisplayedErrors + 1) InfoGui.textList.add(text);
        else {
            if (InfoGui.textList.size() == Config.amountOfDisplayedErrors + 1) InfoGui.textList.add("");
            errorCount += 1;
            InfoGui.textList.set(Config.amountOfDisplayedErrors + 1, "... (" + errorCount + " more)");
        }
    }


    public void throwSuccess (String text) {
    // Sets the final results screen to be the success screen, and adds the success message in it.

        // If the function gets called for the first time, it adds the success message and configures the GUI.
        // Basically a useless check as the function only gets called once, but it might come useful in the future, we never know.
        if (InfoGui.textList.size() <= 0) {
            InfoGui.textList.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.DARK_GREEN + "Succesfully processed the files!");
            InfoGui.buttonAmount = 1;

            LogHelper.info("Succesfully installed the mcinstance file!");
        }

        InfoGui.textList.add(text);
    }


    public void makeFancyModInfo(FMLPreInitializationEvent event) {
    // The following will overwrite the mcmod.info file so the info page looks good.
    // Adapted from Jabelar's Magic Beans and AstroTibs's OptionsEnforcer.

        LogHelper.info("Generating the mod info page...");

        // This will stop Forge from complaining about missing mcmod.info (just in case i forget it).
        event.getModMetadata().autogenerated = false;

        event.getModMetadata().name = ModProperties.COLORED_NAME; // Mod name
        event.getModMetadata().version = ModProperties.COLORED_VERSION; // Mod version
        event.getModMetadata().credits = ModProperties.CREDITS; // Mod credits

        // Author list
        event.getModMetadata().authorList.clear();
        Collections.addAll(event.getModMetadata().authorList, ModProperties.AUTHORS);

        event.getModMetadata().url = ModProperties.COLORED_URL; // Mod URL

        // Mod description
        event.getModMetadata().description = ModProperties.DESCRIPTION + "\n\n" + EnumChatFormatting.DARK_GRAY + EnumChatFormatting.ITALIC  +
                                             ModProperties.SPLASH_OF_THE_DAY[(new Random()).nextInt(ModProperties.SPLASH_OF_THE_DAY.length)] ;

        if (ModProperties.LOGO != null) event.getModMetadata().logoFile = ModProperties.LOGO; // Mod logo
    }

}
