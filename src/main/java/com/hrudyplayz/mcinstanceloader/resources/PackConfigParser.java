package com.hrudyplayz.mcinstanceloader.resources;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.utils.FileHelper;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;

import java.io.File;
import java.util.ArrayList;

public class PackConfigParser {
// This class acts as the .packconfig parser. Every separate use would be declared as a new method.

    public static ResourceObject[] parseResources (String file) {
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
            else{

                // Defines the collumn where text after it gets ignored, used for the comment system.
                int limit = trimmedLine.length();
                if (trimmedLine.contains("#")) limit = trimmedLine.indexOf("#");

                trimmedLine = trimmedLine.substring(0, limit); // The real line is everything before the #.

                // Grabs the property and value, if there's any.
                String property = "";
                String value = "";
                if (trimmedLine.contains("=")) {
                    property = trimmedLine.substring(0, trimmedLine.indexOf("=")).trim();
                    value =  trimmedLine.substring(trimmedLine.indexOf("=") + 1).trim();
                }

                // Defines the behavior to do on each object depending on which property gets modified (incorrect properties will get ignored).
                switch (property) {
                    case "type":
                        resource.type = value;
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

                    case "destination": // Replaces every separator with the system one and sets the download path to be in the temp folder (to ensure file safety).
                        value = value.replace("\\", File.separator);
                        value = value.replace("/", File.separator);
                        resource.destination = Config.configFolder + "temp" + File.separator + "overrides" + File.separator + value;
                        break;

                    case "side":
                        resource.side = value;
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

            cursor += 1; // Goes to the next line.
        }

        return result.toArray(new ResourceObject[0]); // After every line has been iterated over, it returns a converted array of the previous arraylist.
    }


}
