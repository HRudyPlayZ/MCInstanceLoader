package com.hrudyplayz.mcinstanceloader;

import java.io.File;

import net.minecraft.util.EnumChatFormatting;



public class ModProperties {
// This class will be used to define mod properties in order to access them from anywhere.

    // General values
    public static final String MODID = "mcinstanceloader";
    public static final String NAME = "MCInstance Loader";
    public static final String VERSION = "1.0";
    public static final String MC_VERSION = "1.7.10";
    public static final String URL = "";
    public static final String VERSION_CHECKER_URL = "";


    // Mod info page
    public static final String COLORED_NAME = EnumChatFormatting.DARK_GREEN + "MC" + "Instance" + EnumChatFormatting.GRAY + " Loader";

    public static final String COLORED_VERSION = EnumChatFormatting.GRAY + VERSION;
    public static final String COLORED_URL = EnumChatFormatting.GRAY + URL;

    public static final String CREDITS = EnumChatFormatting.GOLD + "AstroTibs" + EnumChatFormatting.GRAY + " for OptionsEnforcer along with " +
                                         EnumChatFormatting.BLUE + "Janrupf and HansWasser" + EnumChatFormatting.GRAY + " for ModDirector";

    public static final String[] AUTHORS = new String[] {
            EnumChatFormatting.RED + "HRudyPlayZ"
    };

    public static final String DESCRIPTION = EnumChatFormatting.GRAY + "A small mod that allows Forge to load a modpack in the " + EnumChatFormatting.DARK_GREEN + ".mcinstance" +
                                             EnumChatFormatting.GRAY + " format. \nIt also allows for easier pack updates while letting you keep your own mods. Learn more on the mod's page.";

    public static final String[] SPLASH_OF_THE_DAY = new String[] {
            "Darling, this is good!",
            "Why didn't i do this sooner?",
            "What am i doing with my life?",
            "Finally, someone did this!",
            "Why do i smell something burning?",
            "Only made possible by Notch's most realistic LEGO Simulator built so far.",
            "I love it.",
            "As stable as my bank account!",
            "I'm still learning Java, don't fight me :(",
            "Mitochondria is the powerhouse of the cell.",
            "And it's not made in MCreator!",
            "Creeper? Awww man.",
            "Also try ModDirector.",
            "Also try OptionsEnforcer.",
            "Also try it's the little things.",
            "Also try CraftPresence.",
            "The revolution in modding history.",
            "Jeff Bezos's well kept secret.",
            "Why were the dwarves digging a hole? To get to this sooner!",
            "Elon Musk's hidden fetish.",
            "if thisModWorks() then thatsAwesome() end",
            "Why did the chicken cross the road? Because this mod was waiting on the other side.",
            "I would like to first of all thank my two parents, without whom i wouldn't be here.",
            "And i think to myself, what a wonderful world.",
            "It's like crypto but actually stable!"
    };

    // Should be equal to null to disable it, otherwise it should just be the file name (ex: "logo.png").
    public static String LOGO = "assets" + File.separator + ModProperties.MODID + File.separator + "logo.png";
}
