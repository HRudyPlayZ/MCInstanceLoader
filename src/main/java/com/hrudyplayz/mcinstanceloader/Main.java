package com.hrudyplayz.mcinstanceloader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;

import net.minecraft.client.resources.I18n;
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


@SuppressWarnings("unused")
@Mod(modid = ModProperties.MODID, name = ModProperties.NAME, version = ModProperties.VERSION)
public class Main {
// This class defines the main mod class, and registers stuff like Events.

    //TODO: Cleanup the whole code base a little (add comments and such)
    //TODO: For the future, consider switching entirely to GUI buttons instead of preInit. Might make things better.
    //TODO: Add support for removing any mod through the zero-byte technique.

    public static boolean hasUpdate = false; // Used to check whether the mod has an update or not.
    public static String updateUrl = ""; // Direct link to the mod update.
    public static String updateFileName = ""; // Name to give to the update file.

    public static boolean shouldDoSomething = false; // Creates the shouldDoSomething variable used to check if the mod should display the end GUI.
    public static boolean hasErrorOccured = false; // Creates the hasErrorOccured boolean used to stop next steps from running and tell the GUI if it should be in error/success mode.
    public static String errorContext = ""; // Creates the public error context that any method can modify, used to tell more infos about a particular error.
    public static int errorCount = 0; // Number of errors encountered so far for the maximum count setting.

    public static String side; // Currently running side of the game, either "client" or "server".

    public static String[] blacklist; // List of strings that is later used to store the StopModReposts list.

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    // The preInit event, that gets called as soon as possible in Forge's loading. Basically the entire mod.
    // First phrase of the mod, gets ran at the game launch.

        // ===== STEP 0: The mod's initialisation. =====
        initMod(event);

        // ===== STEP 1: Cleanup phase =====
        cleanupFiles();
        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.

        // ===== STEP 2: Check for updates =====
        updateChecker();

        secondPhase();
    }

    public static void secondPhase() {
    // Second phase of the mod, used for the continue option in the update GUI.

        // ===== STEP 3: MCInstance extraction =====
        extractMCInstance();
        errorContext = ""; // Resets the errorContext, so it can be reused for the next step.
        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.

        // ===== STEP 4: Zip the pack folder =====
        if (!Config.disableAutomaticZipCreation && !FileHelper.exists(Config.configFolder + "pack.mcinstance") && !FileHelper.exists(Config.configFolder + "pack.mcinstance.disabled")) {
            LogHelper.info("No pack.instance file found, created one from the pack folder.");
            ZipHelper.zip(Config.configFolder + "pack", Config.configFolder + "pack.mcinstance", false, true);
        }

        // ===== STEP 5: Resources download =====
        downloadResources();
        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.

        // Does the third phase on servers, as they never have a GUI displayed with the optional mods.
        if (side.equals("server")) thirdPhase();
    }

    public static void thirdPhase() {
    // Third phrase of the mod, used after downloading the optional mods.

        // ===== STEP 6: Overrides copy =====
        copyOverrides();
        copyLocalizedOverrides();
        LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.

        // ===== STEP 7: Carryover copy =====
        copyCarryover();

        // ===== STEP 8: Final setup =====
        finalSetup();
    }


    public static void makeFancyModInfo(FMLPreInitializationEvent event) {
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


    public static void initMod(FMLPreInitializationEvent event) {
    // Mod Initialisation: Creating the config file, the menu information and the carryover folder. Also gets the current side the mod is running on.

        FileHelper.overwriteFile( Config.configFolder + "details.log", new String[0]); // Clears the log file, so it can be reused.

        LogHelper.info("Current mod version is " + ModProperties.VERSION); // Displays the mod version

        LogHelper.verboseInfo("The mod correctly entered the preInit stage.");

        makeFancyModInfo(event); // Creates the nice-looking mod info page.

        // Registers the GuiOpenEventHandler when on client side, and sets the variable to easily check which side is running.
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            side = "client";
            MinecraftForge.EVENT_BUS.register(GuiOpenEventHandler.instance);
        }
        else side = "server";

        LogHelper.verboseInfo("The current side is " + side + " ."); // Prints the current side.

        Config.createConfigFile();

        // Deletes the empty files in the mods folder, prior to an installation
        String[] list = FileHelper.listDirectory("mods", true);
        LogHelper.verboseInfo("Detecting files scheduled for deletion");

        for (String s : list) {
            long size = -1;
            try {
                size = Files.size(Paths.get("mods" + File.separator + s));
            }
            catch (Exception ignore) {}

            //Ignore folders
            if (FileHelper.isDirectory("mods" + File.separator + s))
                return;

            LogHelper.verboseInfo("name: " + s + " | size: " + size);

            if (size == 0) {
                FileHelper.delete("mods" + File.separator + s);
                LogHelper.verboseInfo("Deleting file " + s);
            }
        }
        // If the carryover folder doesn't exist, it creates it with empty mods and config folders inside.
        if (!FileHelper.exists("carryover")) {
            LogHelper.info("The carryover folder didn't exist, created a blank one.");
            FileHelper.createDirectory("carryover");
        }

        // If the cache folder doesn't exist, it creates it.
        if (!FileHelper.exists("mcinstance-cache")) {
            LogHelper.info("The cache folder didn't exist, created it.");
            FileHelper.createDirectory("mcinstance-cache");
        }

        // Creates the pack folder with an example layout if it doesn't exist.
        String path = Config.configFolder + "pack";
        if (!FileHelper.isDirectory(path)) {
            LogHelper.info("The pack folder was a file instead, removed it.");
            FileHelper.delete(path);
        }
        if (!FileHelper.exists(path)) {
            LogHelper.info("The pack folder didn't exist, created it.");

            // The pack directory
            FileHelper.createDirectory(path);

            // The main overrides directory
            FileHelper.createDirectory(path + File.separator + "overrides");
            FileHelper.overwriteFile(path + File.separator + "overrides" + File.separator + "example.txt", new String[]{"Example file that would be at root. Applies to both client-side and server-side.", "Created my MCInstance Loader."});
            FileHelper.createDirectory(path + File.separator + "overrides" + File.separator + "mods");
            FileHelper.overwriteFile(path + File.separator + "overrides" + File.separator + "mods" + File.separator + "example2.txt", new String[]{"Example file that would be in the mods folder. Applies to both client-side and server-side.", "Created by MCInstance Loader."});

            // The client overrides directory
            FileHelper.createDirectory(path + File.separator + "client-overrides");
            FileHelper.overwriteFile(path + File.separator + "client-overrides" + File.separator + "example.txt", new String[]{"Example file that would be at root. Client-side only", "Created my MCInstance Loader."});
            FileHelper.createDirectory(path + File.separator + "client-overrides" + File.separator + "mods");
            FileHelper.overwriteFile(path + File.separator + "client-overrides" + File.separator + "mods" + File.separator + "example2.txt", new String[]{"Example file that would be in the mods folder. Client-side only", "Created by MCInstance Loader."});

            // The server overrides directory
            FileHelper.createDirectory(path + File.separator + "server-overrides");
            FileHelper.overwriteFile(path + File.separator + "server-overrides" + File.separator + "example.txt", new String[]{"Example file that would be at root. Server-side only", "Created my MCInstance Loader."});
            FileHelper.createDirectory(path + File.separator + "server-overrides" + File.separator + "mods");
            FileHelper.overwriteFile(path + File.separator + "server-overrides" + File.separator + "mods" + File.separator + "example2.txt", new String[]{"Example file that would be in the mods folder. Server-side only", "Created by MCInstance Loader."});


            // The metadata.packconfig file.
            FileHelper.overwriteFile(path + File.separator + "metadata.packconfig", new String[]{
                    "# Example metadata.packconfig file",
                    "# It isn't used by the mod itself but may be used by some third party tools at some point.",
                    "# Setting them is highly recommended.",
                    "",
                    "[file]",
                    "formatVersion = 1 # Don't change this for now, only do if there's a significant format update in the future.",
                    "",
                    "[modloader]",
                    "type = forge",
                    "version = [Your forge version, ex: 1614]",
                    "minecraftVersion = [Your Minecraft version, ex: 1.7.10]",
                    "",
                    "[pack]",
                    "name = [Your pack name]",
                    "author = [Your name]",
                    "description = [A short description, only on one line]",
                    "version = [Your pack version]"
            });

            // The resources.packconfig file.
            FileHelper.overwriteFile(path + File.separator + "resources.packconfig", new String[]{
                    "# Example resources.packconfig file",
                    "# Comments start with an # and will affect the rest of the line.",
                    "# Incorrect lines are also ignored so non-existant properties, spaces etc aren't an issue.",
                    "",
                    "# Please check out the wiki for more information: https://github.com/HRudyPlayZ/MCInstanceLoader/wiki",
                    "",
                    "# The file is separated into multiple resources, each created with a line starting with square brackets ([]).",
                    "",
                    "[Example URL file]    # The value inside here is the resource's name, it is what will be displayed to the user.",
                    "type = url   # This is the value type, currently it can either be \"url\", \"modrinth\", or \"curseforge\". URL is the default in case of an incorrect or missing value.",
                    "destination = [Where to save the file, relative to the .minecraft folder]   # Don't forget to add the file name with the extension at the end.",
                    "",
                    "url = [The web address of the file you want to download]",
                    "",
                    "# It is also possible to add a list of buttons to click in order using the follows property.",
                    "# It is just a list of texts to \"click\" on, separated by commas and ignoring any heading or ending spaces.",
                    "# If for some reasons you need to click on a button that has a comma in its text, you can write \"\\,\" to escape it.",
                    "# The text to click on is accesed through the HTML page like this: <div href=\"[Your target URL]\"> [Your text] </div>",
                    "follows = [Your first button to click on], [The second button to click afterwards], [...]",
                    "",
                    "# It is possible to set any resource to only download when either on the client or server side. Both is the default in case of an incorrect or missing value.",
                    "side = both | client | server",
                    "",
                    "# It is also possible to mark a resource as optional, meaning that it won't be automatically downloaded this way.",
                    "optional = true",
                    "",
                    "# The following are hashes, they're not mandatory, but setting at least one of them is recommended. Only the following formats are supported for now.",
                    "# Any file that doesn't match one of the given hashes will throw an error. Hashes are required if you want to use the mod's cache feature.",
                    "SHA-512 = [Your SHA-512 hash]   #Any of the SHA properties can also be written without the dash. (like SHA512)",
                    "SHA-256 = [Your SHA-256 hash]",
                    "SHA-1 = [Your SHA-1 hash]",
                    "MD5 = [Your MD5 hash]",
                    "CRC32 = [Your CRC32 hash]   # Uses the big-endian order and not little-endian. So the format commonly given by websites, WinRAR, OpenHashTab and others as opposed to Unix's md5sum.",
                    "",
                    "",
                    "[Example Modrinth file]",
                    "type = modrinth   # Modrinth files use the \"modrinth\" type.",
                    "destination = [Where to save the file, relative to the .minecraft folder]",
                    "versionId = [Your file's version ID]   # It can be obtained by going on the website and looking around on the download page of the file you want.",
                    "sourceFileName = [The name of the file hosted on Modrinth's servers]   # Mandatory if there's multiple files on the version you're looking for. Optional otherwise.",
                    "",
                    "# Hashes can also be given here. If no hashes are given, the mod will grab them from the API's response in order to still allow for caching if possible.",
                    "",
                    "",
                    "[Example Curseforge file]",
                    "type = curseforge   # Curse files use the \"curseforge\" type.",
                    "destination = [Where to save the file, relative to the .minecraft folder]",
                    "",
                    "# The mod has multiple ways of downloading from Curseforge. You can either set the projectId and fileId or the fileId and sourceFileName to change the way a file gets downloaded.",
                    "# Please note that the API may break at some point. And it is recommended to give all three of those values just in case.",
                    "projectId = [Your file project's ID]   # It can be obtained by going on the website and looking on the right side of the project page.",
                    "fileId = [Your file's ID]   # It can be obtained in the URL of the download page for a specific file.",
                    "sourceFileName = [The name of the file hosted on Curseforge's servers]",
                    "",
                    "# Hashes can also be given here. If no hashes are given, the mod will grab them from the API's response if possible, in order to still allow for caching.",
                    "# Note that the mod will only use the API if at least the projectId and fileId properties are given, otherwise it will try to generate the URL."
            });

            // The optionals.packconfig file.
            FileHelper.overwriteFile(path + File.separator + "optionals.packconfig", new String[]{
                    "# Example optionals.packconfig file",
                    "# Comments start with an # and will affect the rest of the line.",
                    "",
                    "# Please check out the wiki for more information: https://github.com/HRudyPlayZ/MCInstanceLoader/wiki",
                    "",
                    "# The file is separated into multiple \"choice menus\", each created with a line starting with square brackets ([]).",
                    "# Each of them is separate from the other and will be displayed one at a time.",
                    "[Menu 1]    # The value inside here is the internal menu name, it may be used at some point, so make sure to give a unique name nonetheless.",
                    "",
                    "title = [Your title]    # This value defines the title of the menu, make it say something like \"Choose one of the following:\".",
                    "",
                    "# Those two properties define how many items you can select in the menu.",
                    "# Everything selected by the user will be downloaded once they click the \"Confirm and continue\" button.",
                    "# If the maximum amount of choices is above the minimum, there is no restriction on how many choices you can make.",
                    "# If there is less selected items than the minimum, the \"Confirm and continue\" button will be grayed out.",
                    "# If there is more than the maximum amount of items checked, the first one will automatically get unchecked.",
                    "",
                    "maxchoices = 1     # A number between 0 and X (any incorrect value will get ignored or adapted to the range), defaults to 1 if no value is set, 0 means there is no maximum to the amount you can check.",
                    "minchoices = 0     # A number between 0 and X (any incorrect value will get ignored or adapted to the range). defaults to 0 if no value is set, which means the user is allowed to not check anything.",
                    "",
                    "# The optional resources system uses a list of \"option\" objects, this allows for grouping resources.",
                    "# This list uses the format of optionX.property, X starting at 1.",
                    "# It needs to be ordered, any option where the id (X) is separated by a hole will be ignored.",
                    "# Below are how they're defined.",
                    "",
                    "option1.name = [Your option name]    # The name of the option, displayed next to the checkbox to the user.",
                    "option1.description = [Your option's description]    # The description of the option, used to indicate more precisely to the user what this choice does.",
                    "option1.default = true    # Whether the option should be checked by default or not, defaults to false if no value is set. Keep in mind that this still respects the value of maxchoices and might get unchecked if later options also get set to default and exceed the limit.",
                    "",
                    "# The list of resources to download by choosing this specific option, separated by commas.",
                    "# Commas can be included inside a resource name if needed using \"\\,\".",
                    "# Those names are defined inside the resources.packconfig file inside brackets.",
                    "# NOTE: Only resources that were set to be optional would be downloaded.",
                    "# Any undefined resources would be ignored.",
                    "option1.resources = [Your first resource name], [Your second resource name], [...]",
                    "",
                    "option2.name = [Your second option name]",
                    "option2.description = [Your second option's description]",
                    "option2.default = true    # Setting this to true means that Option 1 will get unchecked as there is a maximum of one checked option for this menu.",
                    "option2.resources = [Resource A], [Resource B], [...]",
                    "",
                    "# NOTE: You can put as much options as you want, note that they will get splitted into pages of 3 in order to be displayed in the GUI.",
                    "# The page buttons and indicators only appear if there is more than one page. If there is more than 2, a first page and a last page button also appear.",
                    "",
                    "",
                    "# Completely unrelated menu with its own settings.",
                    "[Menu 2]",
                    "title = [Your second title]",
                    "",
                    "option1.name = [Your first option name]",
                    "option1.desription = [Your first option description]",
                    "option1.default = true",
                    "option1.resources = [Resource A], [...]"
            });
        }
    }


    public static void cleanupFiles() {
    // Cleanup phase: Deletes the potential remnants of a failed installation.

        if (FileHelper.exists(Config.configFolder + "temp")) { // Deletes the temp folder, if the mod couldn't do it before for some reasons.
            LogHelper.info("Found a leftover temp directory, removed it.");
            FileHelper.delete(Config.configFolder + "temp");
        }
    }


    public static void updateChecker() {
    // Checks if there is an update to the mod or not.

        // If the update checker is disabled entirely, it doesn't do anything.
        if (Config.updateCheckerMode == 3) return;

        // Otherwise, it tries to download the version data. If it fails, it just throws an error in the logs.
        if (!WebHelper.downloadFile("https://raw.githubusercontent.com/HRudyPlayZ/MCInstanceLoader/1.7.10/versionData.txt", Config.configFolder + "temp" +  File.separator + "versionData.txt")) {
            LogHelper.error("Update checker: Failed to download the update data.");
            return;
        }

        String[] list = FileHelper.listLines(Config.configFolder + "temp" +  File.separator + "versionData.txt");
        String version = "";

        for (String s : list) {
            if (s.startsWith("version:")) version = s.substring(8).trim();
            if (s.startsWith("url:")) updateUrl = s.substring(4).trim();
            if (s.startsWith("fileName:")) updateFileName = s.substring(9).trim();
        }

        if (!ModProperties.VERSION.equalsIgnoreCase(version)) {
            LogHelper.info("Update checker: There is a new version available. (" + version + ")");
            LogHelper.info("You can get it manually at the following address: " + updateUrl);

            if (Config.updateCheckerMode < 2) {
                hasUpdate = true;
                throwUpdateScreen();
            }
        }
        else LogHelper.info("Update checker: The mod is properly updated.");
    }


    public static void extractMCInstance() {
    // MCInstance extraction: Extracts the mcinstance file to the temp folder.

        if (hasUpdate) return;

        if (FileHelper.exists(Config.configFolder + "pack.mcinstance")) {
            LogHelper.info("Found pack.instance, starting the installation process.");

            shouldDoSomething = true; // The file exists and the results GUI should displayed (otherwise the mod won't do anything).

            if (!ZipHelper.extract(Config.configFolder + "pack.mcinstance", Config.configFolder + "temp" + File.separator)) throwError("Error while extracting the pack.mcinstance file.");
        }
        else LogHelper.info("Missing pack.mcinstance, skipping.");
    }


    public static void downloadResources() {
    // Resources download: Downloads the resources added in the resources.packconfig file and saves them to their appropriate location in the temp/overrides folder.

        if (!hasErrorOccured && !hasUpdate && FileHelper.exists(Config.configFolder + "temp" + File.separator + "resources.packconfig")) {
            LogHelper.info("Found a resources.packconfig file, starting the download process.");

            ResourceObject[] list = PackConfigParser.parseResources(Config.configFolder + "temp" + File.separator + "resources.packconfig");

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Downloading resource", list.length, true);
            int progressCount = 0;

            // Grabs the current list of blacklisted sites from StopModReposts.
            if (!Config.disableStopModRepostsCheck) {
                LogHelper.verboseInfo("Getting the blacklist from StopModReposts.org...");
                if (WebHelper.downloadFile("https://api.stopmodreposts.org/sites.txt", Config.configFolder + "temp" + File.separator + "stopmodreposts.txt")) blacklist = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "stopmodreposts.txt");
                else blacklist = new String[0];
            }
            else blacklist = new String[0];

            // For each object present in the resources list, it tries to download and check their hash, and displays the resource in a fancy format in the mod log.
            for (ResourceObject object : list) {
                progress.step(object.name);

                LogHelper.appendToLog(Level.INFO, "", true);
                LogHelper.appendToLog(Level.INFO, "==================================================", true);
                object.appendToLog();

                progressCount++;

                if (!Config.verboseMode && side.equals("server")) {
                    LogHelper.info("Downloading " + object.name + "... (" + progressCount + "/" + list.length + " resources)");
                }

                LogHelper.verboseInfo("Attempting to download the resource " + object.name + "... (" + progressCount + "/" + list.length + " resources)");

                if (object.downloadFile()) {
                    if (!object.checkHash()) throwError("Could not verify the hash of " + object.name + ".");
                    else if (!object.checkCache() && !Config.disableCache) {
                        if (object.SHA512 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "SHA512-" + object.SHA512);
                        else if (object.SHA256 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "SHA256-" + object.SHA256);
                        else if (object.SHA1 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "SHA1-" + object.SHA1);
                        else if (object.MD5 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "MD5-" + object.MD5);
                        else if (object.CRC32 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "CRC32-" + object.CRC32);
                    }
                }
                else throwError("Error while downloading " + object.name + ".");

                LogHelper.appendToLog(Level.INFO, "==================================================", true);

                errorContext = ""; // Resets the errorContext, so it can be reused for the next resource or the next step.
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }
    }


    public static void copyOverrides() {
    // Overrides copy: Replaces the minecraft files with the ones in the overrides folder.

        String path = Config.configFolder + "temp" + File.separator + "overrides";

        // If there wasn't any error that occured on the previous steps.
        if (!hasErrorOccured && !hasUpdate && FileHelper.exists(path) && FileHelper.isDirectory(path)) {
            LogHelper.info("Moving the files from the overrides folder.");

            // Creates the recursive file list, to merge from.
            String[] fileList = FileHelper.listDirectory(Config.configFolder + "temp" + File.separator + "overrides", true);

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Merging overrides", fileList.length, true);

            // For every file/folder in the overrides folder, we move them to the root .minecraft folder.
            for (String s : fileList) {
                progress.step(s);

                LogHelper.verboseInfo("Merging " + s + " with the original folder.");

                // Tries to move (merge) the file, if it fails it throws an error.
                if (!FileHelper.copy(Config.configFolder + "temp" + File.separator + "overrides" + File.separator + s, s)) throwError("Error while merging the file " + s + " from the overrides folder.");

                errorContext = ""; // Resets the errorContext, so it can be reused for the next resource or the next step.
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }
    }

    public static void copyLocalizedOverrides() {
        // Overrides copy: Replaces the minecraft files with the ones in the localized overrides folder.
        String overrideType = (side.equals("client")) ? "client-overrides" : "server-overrides";

        String path = Config.configFolder + "temp" + File.separator + overrideType;

        // If there wasn't any error that occured on the previous steps.
        if (!hasErrorOccured && !hasUpdate && FileHelper.exists(path) && FileHelper.isDirectory(path)) {
            LogHelper.info("Moving the files from the " + overrideType + " folder.");

            // Creates the recursive file list, to merge from.
            String[] fileList = FileHelper.listDirectory(Config.configFolder + "temp" + File.separator + overrideType, true);

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Merging " + overrideType + " folder", fileList.length, true);

            // For every file/folder in the localized overrides folder, we move them to the root .minecraft folder.
            for (String s : fileList) {
                progress.step(s);

                LogHelper.verboseInfo("Merging " + s + " with the original folder.");

                // Tries to move (merge) the file, if it fails it throws an error.
                if (!FileHelper.copy(Config.configFolder + "temp" + File.separator + overrideType + File.separator + s, s)) throwError("Error while merging the file " + s + " from the " + overrideType + " folder.");

                errorContext = ""; // Resets the errorContext, so it can be reused for the next resource or the next step.
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }
    }


    public static void copyCarryover() {
    // Carryover copy: Replace the files with the ones placed inside the carryover folder by the player.

        String path = "carryover";

        // If there wasn't any error that occured on the previous steps.
        if (!hasErrorOccured && !hasUpdate && shouldDoSomething && FileHelper.exists(path) && FileHelper.isDirectory(path)) {
            LogHelper.info("Copying the files from the carryover folder.");

            // Creates the recursive file list, to merge from.
            String[] fileList = FileHelper.listDirectory("carryover", true);

            // Creates the forge progressbar for the current step, so it can be displayed on the loading screen.
            ProgressManager.ProgressBar progress = ProgressManager.push("MCInstance: Merging carryover", fileList.length, true);

            // For every file/folder in the carryover folder, we copy them to the root .minecraft folder. We don't have to worry about cleaning files for now.
            for (String s : fileList) {
                progress.step(s);

                LogHelper.verboseInfo("Merging " + s + " from carryover to the root folder.");

                // Tries to copy (merge) the file, if it fails it throws an error.
                if (!FileHelper.copy("carryover" + File.separator + s, s)) throwError("Error while merging the file " + s + " from the carryover folder.");

                errorContext = ""; // Resets the errorContext, so it can be reused for the next resource or the next step.
            }

            ProgressManager.pop(progress); // Deletes the progressbar, as it doesn't need to be shown anymore (all files have been done).
        }
    }


    public static void finalSetup() {
    // Final setup: Finalising setup, throwing the success or error screen, disabling/deleting the mcinstance file, deleting or not the temp folder.

        String path;

        // If there wasn't any error, it will throw the success screen and disable the mcinstance file, so the game will boot fine on the next launch.
        if (!hasErrorOccured && shouldDoSomething) {
            if (side.equals("server")) throwSuccess("");
            else {
                for (int i = 0; i < Config.successMessage.length; i += 1) throwSuccess(Config.successMessage[i]);
            }

            path = Config.configFolder + "pack.mcinstance";
            if (!Config.skipFileDisabling && FileHelper.exists(path)) { // If the config to skip the file disabling wasn't set, it will disable or remove it.
                if (Config.deleteInsteadOfRenaming) FileHelper.delete(path);
                else FileHelper.move(path, path + ".disabled");
            }
        }

        // Will delete the temp folder, unless the skip file disabling config is set, to allow for in-depth debugging after the game closes.
        path = Config.configFolder + "temp";
        if (!Config.skipFileDisabling && FileHelper.exists(path)) FileHelper.delete(path);
    }


    public static void throwError(String text) {
    // Sets the final results screen to be an error screen. Adds an error any time it gets called.
    // If the amount of errors displayed is above the maximum limit, it adds to the more counter instead.

        // Sets the hasErrorOccured value to true, so the game will display an error screen and not overwrite any file after the error.
        hasErrorOccured = true;

        if (errorContext.length() > 0) text += " (" + errorContext + ")"; // Adds the error context, if there's any set.

        LogHelper.error(text); // Adds the error to the general game log and the mod log.

        if (side.equals("server")) {
            LogHelper.info("Failed to install the mcinstance file!");
            MinecraftServer.getServer().initiateShutdown();
            return;
        };

        text = "- " + text; // Formats the text to look like an error list.

        if (InfoGui.textList.size() <= 0) { // If this function gets called for the first time, it adds the error message and configures the GUI.
            InfoGui.textList.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.RED + I18n.format("gui.mcinstanceloader.error"));
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

    public static void throwSuccess(String text) {
    // Sets the final results screen to be the success screen, and adds the success message in it.

        if (side.equals("server")) {
            LogHelper.info("Succesfully installed the mcinstance file!");
            LogHelper.info("The server will soon restart to apply its changes.");

            MinecraftServer.getServer().initiateShutdown();
            return;
        }

        // If the function gets called for the first time, it adds the success message and configures the GUI.
        // Basically a useless check as the function only gets called once, but it might come useful in the future, we never know.
        if (InfoGui.textList.size() <= 0) {
            InfoGui.textList.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.DARK_GREEN + I18n.format("gui.mcinstanceloader.success"));
            InfoGui.buttonAmount = 1;

            LogHelper.info("Succesfully installed the mcinstance file!");
        }

        InfoGui.textList.add(text);
    }


    public static void throwUpdateScreen() {
    // Sets the final results screen to be the mod update screen.

        if (side.equals("server")) return;

        if (Config.updateCheckerMode == 1) InfoGui.buttonAmount = 1;
        else InfoGui.buttonAmount = 2;

        InfoGui.textList.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.BLUE + I18n.format("gui.mcinstanceloader.updatefound"));

        InfoGui.textList.addAll(Arrays.asList(I18n.format("gui.mcinstanceloader.updatemessage").split("\\\\n")));
    }
}
