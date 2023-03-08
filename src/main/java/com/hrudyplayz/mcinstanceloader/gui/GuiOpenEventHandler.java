package com.hrudyplayz.mcinstanceloader.gui;

import java.util.Arrays;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.client.gui.GuiMainMenu;
import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;

@SuppressWarnings("unused")
public class GuiOpenEventHandler {
// This class is the hook used to determine whenever a GUI gets displayed.

    public static final GuiOpenEventHandler INSTANCE = new GuiOpenEventHandler(); // Creates the instance, so we can register it as an EVENT_BUS.

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void openMainMenu(GuiOpenEvent event) {
    // Checks if the main menu gets shown.
        if (!Main.hasUpdate && !Main.shouldDoSomething) return;

        // If the event and the GUI are valid and the displayed GUI is part of the given class list (for mod support), it shows the results screen.
        if (event != null && event.gui != null && event.gui.getClass().isAssignableFrom(GuiMainMenu.class)) {
            LogHelper.info("Displaying GUI.");

            if (Main.hasUpdate && Config.updateCheckerMode < 2) event.gui = new InfoGui(event.gui);
            else if (Main.shouldDoSomething) event.gui = new OptionalModsGui(event.gui);

            MinecraftForge.EVENT_BUS.unregister(INSTANCE);
        }
    }

}
