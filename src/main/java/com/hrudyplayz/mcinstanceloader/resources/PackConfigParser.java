package com.hrudyplayz.mcinstanceloader.resources;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.utils.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class PackConfigParser {
// This class acts as the .packconfig parser. Every separate use would be declared as a new method.

    public static ResourceObject[] parseResources(String file) {
    // Parses the resources inside a resources.packconfig file, and returns a list of them.

        ArrayList<ResourceObject> result = new ArrayList<>(); // Creates the result array, that will get converted to a fixed length list at the end.
        String[] lines = FileHelper.listLines(file); // List of every line (as a string) in the entire file.

        ResourceObject resource = new ResourceObject(); // Defines the resource variable, aka the current object to create/modify.

        // Iterates over every line in the file.
        int cursor = 0;
        while (cursor < lines.length) {

            String trimmedLine = lines[cursor].trim(); // trims the current line of any starting/ending whitespaces.

            // If the line looks something like [this], it will create a new object and set its name.
            if (trimmedLine.startsWith("[") && trimmedLine.contains("]")) {
                resource = new ResourceObject();
                result.add(resource);

                resource.name = trimmedLine.substring(1, trimmedLine.indexOf("]")); // The name is everything between the brackets.
            }

            // Otherwise, it will define the properties of the current object.
            else {

                // Defines the collumn where text after it gets ignored, used for the comment system.
                int limit = trimmedLine.length();
                if (trimmedLine.contains("#")) limit = trimmedLine.indexOf("#");

                trimmedLine = trimmedLine.substring(0, limit); // The real line is everything before the #.

                // Grabs the property and value, if there's any.
                String property = "";
                String value = "";
                if (trimmedLine.contains("=")) {
                    property = trimmedLine.substring(0, trimmedLine.indexOf("=")).trim();
                    value = trimmedLine.substring(trimmedLine.indexOf("=") + 1).trim();
                }

                // Defines the behavior to do on each object depending on which property gets modified (incorrect properties will get ignored).
                if (value.length() > 0) {

                    switch (property) {
                        case "type":
                            resource.type = value.toLowerCase();
                            break;

                        case "url":
                            resource.url = value;
                            break;
                        case "follows":
                            // Grabs every object separated by a comma (,), allowing commas to be escaped using \,.
                            resource.follows = value.split("(?<!\\\\),");

                            // Removes the trailing spaces around the comma separators and replaces escaped commas with normal commas.
                            for (int i = 0; i < resource.follows.length; i += 1) resource.follows[i] = resource.follows[i].trim().replace("\\,", ",");
                            break;

                        case "projectId":
                            resource.projectId = value;
                            break;
                        case "versionId":
                            resource.versionId = value;
                            break;
                        case "fileId":
                            resource.fileId = value;
                            break;
                        case "sourceFileName":
                            resource.sourceFileName = value;
                            break;
                        case "optional":
                            if (value.equalsIgnoreCase("true")) {
                                HashMap<String, ResourceObject> dict = OptionalResourcesHandler.resourcesDict;
                                if (!dict.containsKey(resource.name)) dict.put(resource.name, resource);

                                result.remove(resource);
                            }
                            break;

                        case "destination": // Replaces every separator with the system one and sets the download path to be in the temp folder (to ensure file safety).
                            value = value.replace("\\", File.separator);
                            value = value.replace("/", File.separator);
                            resource.destination = Config.configFolder + "temp" + File.separator + "overrides" + File.separator + value;
                            break;

                        case "side":
                            resource.side = value.toLowerCase();

                            if (!Main.side.equals(resource.side) && !resource.side.equals("both") &&
                                (resource.side.equals("client") || resource.side.equals("server"))) {

                                HashMap<String, ResourceObject> dict = OptionalResourcesHandler.resourcesDict;
                                if (dict.containsKey(resource.name)) dict.remove(resource.name);

                                result.remove(resource);
                            }
                            else if (!resource.isOptional && !result.contains(resource)) result.add(resource);
                            else {
                                HashMap<String, ResourceObject> dict = OptionalResourcesHandler.resourcesDict;
                                if (!dict.containsKey(resource.name)) dict.put(resource.name, resource);
                            }

                            break;

                        case "SHA512": // Allows to type either SHA512 or SHA-512.
                        case "SHA-512":
                            resource.SHA512 = value;
                            break;

                        case "SHA256": // Allows to type either SHA256 or SHA-256.
                        case "SHA-256":
                            resource.SHA256 = value;
                            break;

                        case "SHA1": // Allows to type either SHA1 or SHA-1.
                        case "SHA-1":
                            resource.SHA1 = value;
                            break;

                        case "MD5":
                            resource.MD5 = value;
                            break;

                        case "CRC32":
                            resource.CRC32 = value;
                            break;
                    }
                }
            }

            cursor += 1; // Goes to the next line.
        }

        return result.toArray(new ResourceObject[0]); // After every line has been iterated over, it returns a converted array of the previous arraylist.
    }


    public static OptionalResourcesHandler[] parseMenus(String file) {
    // Parses the menus inside an optionals.packconfig file, and returns a list of them.

        ArrayList<OptionalResourcesHandler> result = new ArrayList<>(); // Creates the result array, that will get converted to a fixed length list at the end.
        String[] lines = FileHelper.listLines(file); // List of every line (as a string) in the entire file.

        OptionalResourcesHandler menu = new OptionalResourcesHandler(); // Defines the resource variable, aka the current object to create/modify.

        // Iterates over every line in the file.
        int cursor = 0;
        while (cursor < lines.length) {

            String trimmedLine = lines[cursor].trim(); // trims the current line of any starting/ending whitespaces.

            // If the line looks something like [this], it will create a new object and set its name.
            if (trimmedLine.startsWith("[") && trimmedLine.contains("]")) {
                menu = new OptionalResourcesHandler();
                result.add(menu);

                menu.name = trimmedLine.substring(1, trimmedLine.indexOf("]")); // The name is everything between the brackets.
            }

            // Otherwise, it will define the properties of the current object.
            else {

                // Defines the collumn where text after it gets ignored, used for the comment system.
                int limit = trimmedLine.length();
                if (trimmedLine.contains("#")) limit = trimmedLine.indexOf("#");

                trimmedLine = trimmedLine.substring(0, limit); // The real line is everything before the #.

                // Grabs the property and value, if there's any.
                String property = "";
                String value = "";
                if (trimmedLine.contains("=")) {
                    property = trimmedLine.substring(0, trimmedLine.indexOf("=")).trim();
                    value = trimmedLine.substring(trimmedLine.indexOf("=") + 1).trim();
                }

                // Defines the behavior to do on each object depending on which property gets modified (incorrect properties will get ignored).
                if (value.length() > 0) {
                    switch (property) {

                        case "title":
                            menu.title = value;

                            break;

                        case "minchoices":
                            try {
                                menu.minimumCheckedAmount = Integer.parseInt(value);
                                if (menu.minimumCheckedAmount < 0) menu.minimumCheckedAmount = 0;
                            }
                            catch (NumberFormatException ignore) {}

                            break;

                        case "maxchoices":
                            try {
                                menu.maximumCheckedAmount = Integer.parseInt(value);
                                if (menu.maximumCheckedAmount < 0) menu.maximumCheckedAmount = 0;
                            }
                            catch (NumberFormatException ignore) {}

                            break;

                        default:

                            if (!property.matches("option\\d+\\..*")) break;

                            int id = -1;
                            try {
                                id = Integer.parseInt(property.substring(6, property.indexOf(".")));
                            }
                            catch (NumberFormatException ignore) {}

                            boolean isName = property.matches("option\\d+\\.name");
                            boolean isDefault = property.matches("option\\d+\\.default");
                            boolean isDescription = property.matches("option\\d+\\.description");
                            boolean isResources = property.matches("option\\d+\\.resources");

                            if (isName || isDescription || isDefault || isResources) {
                                if (id == 0 || id > menu.list.size() + 1) {
                                    cursor += 1;
                                    continue;
                                }
                                if (id == menu.list.size() + 1) {
                                    id = menu.list.size() + 1;
                                    menu.list.add(new OptionalResourcesHandler.MenuOption());
                                }
                            }

                            if (isName) menu.list.get(id - 1).name = value;
                            if (isDescription) menu.list.get(id - 1).description = value;
                            if (isDefault) menu.list.get(id - 1).isDefault = value.equalsIgnoreCase("true");
                            if (isResources) {
                                // Grabs every object separated by a comma (,), allowing commas to be escaped using \,.
                                String[] resources = value.split("(?<!\\\\),");

                                // Removes the trailing spaces around the comma separators and replaces escaped commas with normal commas.
                                for (int i = 0; i < resources.length; i += 1) resources[i] = resources[i].trim().replace("\\,", ",");

                                menu.list.get(id - 1).resources = resources;
                            }

                            break;
                    }
                }
            }

            cursor += 1; // Goes to the next line.
        }

        return result.toArray(new OptionalResourcesHandler[0]); // After every line has been iterated over, it returns a converted array of the previous arraylist.
    }

}
