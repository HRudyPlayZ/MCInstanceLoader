package com.hrudyplayz.mcinstanceloader.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.apache.logging.log4j.Level;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;
import com.hrudyplayz.mcinstanceloader.utils.FileHelper;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;
import com.hrudyplayz.mcinstanceloader.utils.WebHelper;


public class ResourceObject {
// This class represents a single resource object, with its own values defined.

    public String name;

    public String type = "url";
    public String url = "";
    public String[] follows = new String[0];

    public String projectId;
    public String versionId;
    public String fileId;
    public String sourceFileName;

    public String destination = "";
    public String side = "both";

    public String SHA512;
    public String SHA256;
    public String SHA1;
    public String MD5;
    public String CRC32;

    public void appendToLog() {
    // Adds the object's properties to the mod log, to allow for easier debugging.

        LogHelper.appendToLog(Level.INFO, "NAME:   " + this.name, false);
        LogHelper.appendToLog(Level.INFO, "DESTINATION:   " + this.destination, false);
        LogHelper.appendToLog(Level.INFO, "SIDE:   " + this.side, false);
        LogHelper.appendToLog(Level.INFO, "TYPE:   " + this.type, false);
        if (!this.type.equals("curseforge") && !this.type.equals("modrinth")) LogHelper.appendToLog(Level.INFO, "URL:   " + this.url, false);
        else {
            LogHelper.appendToLog(Level.INFO, "PROJECTID:   " + this.projectId, false);
            LogHelper.appendToLog(Level.INFO, "VERSIONID:   " + this.versionId, false);
            LogHelper.appendToLog(Level.INFO, "FILEID:   " + this.fileId, false);
            LogHelper.appendToLog(Level.INFO, "SOURCEFILENAME:   " + this.sourceFileName, false);
        }
        if (this.SHA512 != null) LogHelper.appendToLog(Level.INFO, "SHA-512:   " + this.SHA512, false);
        if (this.SHA256 != null) LogHelper.appendToLog(Level.INFO, "SHA-256:   " + this.SHA256, false);
        if (this.SHA1 != null) LogHelper.appendToLog(Level.INFO, "SHA-1:   " + this.SHA1, false);
        if (this.MD5 != null) LogHelper.appendToLog(Level.INFO, "MD5:   " + this.MD5, false);
        if (this.CRC32 != null) LogHelper.appendToLog(Level.INFO, "CRC32:   " + this.CRC32, false);
    }


    @SuppressWarnings("UnstableApiUsage") // Removes the useless warnings thrown by IDEs because Google marked the hash library as beta.
    public boolean checkHash() {
    // Checks the file hash against the specified ones.

        if (this.SHA512 != null) {
            try {
                HashCode hash = Files.hash(new File(this.destination), Hashing.sha512());

                if (!hash.toString().equalsIgnoreCase(this.SHA512)) {
                    Main.errorContext = "The SHA-512 hash does not match.";
                    return false;
                }
            }

            catch (IOException e) {
                Main.errorContext = "Error while checking the SHA-512 hash.";
                return false;
            }
        }

        if (this.SHA256 != null) {
            try {
                HashCode hash = Files.hash(new File(this.destination), Hashing.sha256());

                if (!hash.toString().equalsIgnoreCase(this.SHA256)) {
                    Main.errorContext = "The SHA-256 hash does not match.";
                    return false;
                }
            }

            catch (IOException e) {
                Main.errorContext = "Error while checking the SHA-256 hash.";
                return false;
            }
        }

        if (this.SHA1 != null) {
            try {
                HashCode hash = Files.hash(new File(this.destination), Hashing.sha1());

                if (!hash.toString().equalsIgnoreCase(this.SHA1)) {
                    Main.errorContext = "The SHA1 hash does not match.";
                    return false;
                }
            }

            catch (IOException e) {
                Main.errorContext = "Error while checking the SHA1 hash.";
                return false;
            }
        }

        if (this.MD5 != null) {
            try {
                HashCode hash = Files.hash(new File(this.destination), Hashing.md5());

                if (!hash.toString().equalsIgnoreCase(this.MD5)) {
                    Main.errorContext = "The MD5 hash does not match.";
                    return false;
                }
            }

            catch (IOException e) {
                Main.errorContext = "Error while checking the MD5 hash.";
                return false;
            }
        }

        if (this.CRC32 != null) { // Uses the little-endian format, which is more common than the big-endian one used by Google.common or md5sum in Unix.
            try {
                HashCode hash = Files.hash(new File(this.destination), Hashing.crc32());
                String libHash = hash.toString(); // Google.common's implementation of CRC32 somehow gives pairs in reversed order (big-endian, which isn't the norm).
                String realHash = ""; // So i have to do this so it ends up in the right order, thank goodness this is a fixed 8 characters length (so it is an even number).
                for (int i = 6; -1 < i; i -= 2) realHash += libHash.charAt(i) + libHash.substring(i + 1, i + 2);

                if (!realHash.equalsIgnoreCase(this.CRC32)) {
                    Main.errorContext = "The CRC32 hash does not match.";
                    return false;
                }
            }

            catch (IOException e) {
                Main.errorContext = "Error while checking the CRC32 hash.";
                return false;
            }
        }

        return true; // If there wasn't any check failing and returning false before, this probably means the hash is correct.
    }


    public boolean downloadFile() {
    // Downloads the file based on the given parameters and saves it to the destination path.
    // Also checks the URL against the StopModReposts list.

        // Checks the URL against the StopModReposts list.
        for (int i = 0; i < Main.blacklist.length; i += 1) {
            if (this.url.contains(Main.blacklist[i])) {
                Main.errorContext = "URL is from a repost website.";
                return false;
            }
        }

        // If the specified side is the current one or if the side property is incorrect/unchanged.
        if (Main.side.equals(this.side) || (this.side.equals("both")) ||
           (!this.side.equals("client") && !this.side.equals("server"))) {

            // Support for curseforge's API.
            // Might need to be changed if Overwolf really messes with the API at some point.
            if (this.type.equals("curseforge")) {

                // If the file name hosted on curseforge's servers has been specified, it will try to download the file using the direct link from Curseforge
                // and return true if it worked.
                if (this.sourceFileName != null && this.fileId != null) {
                    this.url = "https://media.forgecdn.net/files/" + this.fileId.substring(0, 4) + "/" + this.fileId.substring(4) + "/" + this.sourceFileName;
                    if (WebHelper.downloadFile(this.url, this.destination)) return true;
                }

                // If it fails or the source file name wasn't specified, it will use Curseforge's API to try and get the file instead.
                if (!WebHelper.downloadFile("https://addons-ecs.forgesvc.net/api/v2/addon/" + this.projectId + "/file/" + this.fileId, Config.configFolder + "temp" + File.separator + "moddata.json")) {
                    Main.errorContext = "Error while getting the file from Curseforge.";
                    return false;
                }

                // Grabs the JSON response from the server.
                String data = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "moddata.json")[0];

                Pattern pattern = Pattern.compile("(?<=\"downloadUrl\":\").*?(?=\")"); // Grabs the download URL without requiring an entire JSON parser.
                Matcher matcher = pattern.matcher(data);
                if (matcher.find()) this.url = matcher.group();
                else this.url = ""; // If somehow no URL was found, it gives it an empty one.

                this.follows = new String[0]; // Sets the follows list to be empty, so the WebHelper class doesn't try to follow it if the user specified it.
            }

            // Support for Modrinth's API.
            // It might need to get updated once they switch to their API v2.
            // Adding a direct download option like curseforge's would require to introduce a new versionName field,
            // or somehow detect if versionId isn't a valid versionId. Not planned for now but maybe later.
            else if (this.type.equals("modrinth")) {

                this.url = ""; // Sets the URL to be empty if nothing worked.

                if (!WebHelper.downloadFile("https://api.modrinth.com/api/v1/version/" + this.versionId, Config.configFolder + "temp" + File.separator + "moddata.json")) {
                    Main.errorContext = "Error while getting the file from Modrinth.";
                    return false;
                }

                // Grabs the JSON response from the server.
                String data = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "moddata.json")[0];
                data = data.substring(data.indexOf("\"files\""), data.indexOf(",\"dependencies\""));

                ArrayList<String> fileUrls = new ArrayList<>();
                Pattern pattern = Pattern.compile("(?<=\"url\":\").*?(?=\")"); // Grabs the entire list of links between "files" and "dependencies", without a JSON parser.
                Matcher matcher = pattern.matcher(data);
                while (matcher.find()) fileUrls.add(matcher.group());

                for (String s : fileUrls) {
                    if (s.substring(s.lastIndexOf("/") + 1).equals(sourceFileName)) this.url = s; // if the URL's file name is equals to the given source file name, it assumes it is the wanted file.
                }

                this.follows = new String[0]; // Sets the follows list to be empty, so the WebHelper class doesn't try to follow it if the user specified it.
            }

            // If it didn't download it from before (so either it's not of curseforge type, the resource didn't have a sourceFileName specified, or the direct download failed),
            // it will download it here. Anything other than the curseforge or modrinth type will directly go here.

            boolean result;
            if (this.follows.length <= 0) result = WebHelper.downloadFile(this.url, this.destination);
            else result = WebHelper.downloadFile(this.url, this.destination, this.follows);

            return result;
        }

        return true; // If the side doesn't correspond, it succesfully skipped it.
    }

}