package com.hrudyplayz.mcinstanceloader.utils;

import com.hrudyplayz.mcinstanceloader.Config;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.FMLLog;
import com.hrudyplayz.mcinstanceloader.ModProperties;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class LogHelper {
// This class is used to handle both printing to the game's logs, and outputing to mod log file.

    public static void log(Level level, Object object) {
    // Prints a certain object (probably a string) both in game log and the mod log.

        appendToLog(level, object, false);
        FMLLog.log(ModProperties.NAME, level, String.valueOf(object));
    }

    public static void appendToLog(Level level, Object object, boolean skipFormat) {
    // Adds a certain object (probably a string) to the mod log, the skipFormat boolean disables the formating with time and level.

        String text = String.valueOf(object);
        LocalDateTime now = LocalDateTime.now();
        if (!skipFormat) text = "[" + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + "] " + level.toString() + ": " + text;
        FileHelper.appendFile(Config.configFolder + "details.log", new String[]{text});
    }

    public static void verboseInfo (Object object) {
    // Prints a certain object (probably a string) both in the game log if the verbose mode is enabled and in the mod log.

        appendToLog(Level.INFO, object, false);
        if (Config.verboseMode) LogHelper.info(object);
    }

    // Shortcuts of the log method, but without having to specify the level and import the class.
    public static void all(Object object)   { log(Level.ALL, object); }
    public static void debug(Object object) { log(Level.DEBUG, object); }
    public static void error(Object object) { log(Level.ERROR, object); }
    public static void fatal(Object object) { log(Level.FATAL, object); }
    public static void info(Object object)  { log(Level.INFO, object); }
    public static void off(Object object)   { log(Level.OFF, object); }
    public static void trace(Object object) { log(Level.TRACE, object); }
    public static void warn(Object object)  { log(Level.WARN, object); }
}
