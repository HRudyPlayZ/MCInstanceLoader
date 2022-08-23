package com.hrudyplayz.mcinstanceloader.utils;

import com.hrudyplayz.mcinstanceloader.Config;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.FMLLog;
import com.hrudyplayz.mcinstanceloader.ModProperties;

import java.time.LocalDateTime;

/**
An helper class to print stuff to logs, also handles the mod's specific log file.

@author HRudyPlayZ
*/
@SuppressWarnings("unused")
public class LogHelper {

    /**
    Prints a certain object (probably a {@link String}), both in the game logs and the mod logs.

     @param level The {@link Level}'s level of importance.
     @param object The object to print.
     */
    public static void log(Level level, Object object) {
        appendToLog(level, object, false);
        FMLLog.log(ModProperties.NAME, level, String.valueOf(object));
    }


    /**
    Prints a certain object (probably a {@link String}), to the mod's log file.

    @param level The {@link Level}'s level of importance.
    @param object The object to print.
    @param skipFormat Whether to disable the formating with time and level or not.
    */
    public static void appendToLog(Level level, Object object, boolean skipFormat) {
        String text = String.valueOf(object);
        LocalDateTime now = LocalDateTime.now();

        if (!skipFormat) text = "[" + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + "] " + level.toString() + ": " + text;

        if (FileHelper.exists(Config.configFolder + "details.log")) FileHelper.appendFile(Config.configFolder + "details.log", new String[]{text});
    }


    /**
    Prints a certain object (probably a {@link String}) both to the mod's log and, if verbose mode is enabled, in the game's logs.
    The importance level will be sent to {@link Level#INFO}.

    @param object
    */
    public static void verboseInfo (Object object) {
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
