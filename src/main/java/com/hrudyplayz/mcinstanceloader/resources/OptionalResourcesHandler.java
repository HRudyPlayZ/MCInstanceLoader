package com.hrudyplayz.mcinstanceloader.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.utils.FileHelper;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;
import org.apache.logging.log4j.Level;


public class OptionalResourcesHandler {
// This class represents a menu object, with the multiple choices of resources.

	// Dictionary of resource groups
	public static HashMap<String, ResourceObject> resourcesDict = new HashMap<>();

	public static ArrayList<ArrayList<MenuOption>> splitList(int number, ArrayList<MenuOption> list) {
		ArrayList<ArrayList<MenuOption>> result = new ArrayList<>();

		ArrayList<MenuOption> item = new ArrayList<>();
		for (int i = 0; i < list.size(); i += 1) {
			if (i % number == 0) {
				item = new ArrayList<>();
				result.add(item);
			}

			item.add(list.get(i));
		}

		return result;
	}

	public static class MenuOption {
	// This class represents a sigle option of the menu.

		public String name;
		public String description;
		public boolean isDefault;
		public String[] resources = new String[0];

		public void download() {
			ArrayList<ResourceObject> list = new ArrayList<>();
			for (String s : this.resources) {
				ResourceObject o = resourcesDict.get(s);
				if (o != null) list.add(o);
			}

			for (ResourceObject object : list) {
				LogHelper.appendToLog(Level.INFO, "", true);
				LogHelper.appendToLog(Level.INFO, "==================================================", true);
				object.appendToLog();

				LogHelper.verboseInfo("Attempting to download the optional resource " + object.name + "...");

				if (object.downloadFile()) {
					if (!object.checkHash()) Main.throwError("Could not verify the hash of " + object.name + ".");
					else if (!object.checkCache() && !Config.disableCache) {
						if (object.SHA512 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "SHA512-" + object.SHA512, true);
						else if (object.SHA256 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "SHA256-" + object.SHA256, true);
						else if (object.SHA1 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "SHA1-" + object.SHA1, true);
						else if (object.MD5 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "MD5-" + object.MD5, true);
						else if (object.CRC32 != null) FileHelper.copy(object.destination, "mcinstance-cache" + File.separator + "CRC32-" + object.CRC32, true);
					}
				}
				else Main.throwError("Error while downloading " + object.name + ".");

				LogHelper.appendToLog(Level.INFO, "==================================================", true);

				Main.errorContext = "";
			}
		}
	}

	public String name;
	public String title = "Please choose an option:";
	public int minimumCheckedAmount = 0;
	public int maximumCheckedAmount = 1;
	public ArrayList<MenuOption> list = new ArrayList<>();

}
