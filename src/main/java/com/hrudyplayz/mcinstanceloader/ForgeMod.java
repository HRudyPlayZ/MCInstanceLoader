package com.hrudyplayz.mcinstanceloader;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;
import org.apache.logging.log4j.Level;


@Mod(modid = ModProperties.MODID, name = ModProperties.NAME, version = ModProperties.VERSION)
public class ForgeMod {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// The preInit event, that gets called as soon as possible in Forge's loading. Basically the entire mod.
		// First phrase of the mod, gets ran at the game launch.

		// ===== STEP 0: The mod's initialisation. =====
		Main.initMod(event);

		// ===== STEP 1: Cleanup phase =====
		Main.cleanupFiles();
		LogHelper.appendToLog(Level.INFO, "", true); // Adds an empty line to the log file, to make it more readable.

		// ===== STEP 2: Check for updates =====
		Main.updateChecker();

		Main.secondPhase();
	}
}
