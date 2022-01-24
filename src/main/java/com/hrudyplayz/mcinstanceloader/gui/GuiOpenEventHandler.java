package com.hrudyplayz.mcinstanceloader.gui;

import java.util.Arrays;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;


public class GuiOpenEventHandler {
// This class is the hook used to determine whenever a GUI gets displayed.

    public static int callCounter = 0;
    public static GuiOpenEventHandler instance = new GuiOpenEventHandler(); // Creates the instance, so we can register it as an EVENT_BUS.

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void openMainMenu(GuiOpenEvent event) {
    // Checks if the main menu gets shown.

        // If the event and the GUI are valid and the displayed GUI is part of the given class list (for mod support), it shows the results screen.
        if (event != null && event.gui != null && Arrays.asList(Config.mainMenuClassPaths).contains(event.gui.getClass().toString().substring(6)) && Main.shouldDoSomething) {
            LogHelper.info("Displaying GUI.");

            event.gui = new InfoGui(event.gui);
            MinecraftForge.EVENT_BUS.unregister(instance);
            return;
        }

        callCounter += 1; // Just a security to prevent this from getting fired too much.
        if (callCounter >= 20) MinecraftForge.EVENT_BUS.unregister(instance);
    }

}
