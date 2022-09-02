package com.hrudyplayz.mcinstanceloader.resources;

import net.minecraft.client.Minecraft;
import org.lwjgl.Sys;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

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
    public boolean isOptional;

    public String destination = "";
    public String side = "both";

    private boolean hasTriedHashChecks;

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
        LogHelper.appendToLog(Level.INFO, "OPTIONAL:   " + this.isOptional, false);

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
                if (!this.hasTriedHashChecks) {
                    this.hasTriedHashChecks = true;
                    LogHelper.info("An error occured while checking the SHA-512 hash, trying again...");
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    catch (InterruptedException ignore) {}

                    return this.checkHash();
                }
                else {
                    e.printStackTrace();
                    Main.errorContext = "System error while checking the SHA-512 hash.";

                    return false;
                }
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
                if (!this.hasTriedHashChecks) {
                    this.hasTriedHashChecks = true;
                    LogHelper.info("An error occured while checking the SHA-256 hash, trying again...");
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    catch (InterruptedException ignore) {}

                    return this.checkHash();
                }
                else {
                    e.printStackTrace();
                    Main.errorContext = "System error while checking the SHA-256 hash.";

                    return false;
                }
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
                if (!this.hasTriedHashChecks) {
                    this.hasTriedHashChecks = true;
                    LogHelper.info("An error occured while checking the SHA1 hash, trying again...");
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    catch (InterruptedException ignore) {}

                    return this.checkHash();
                }
                else {
                    e.printStackTrace();
                    Main.errorContext = "System error while checking the SHA1 hash.";

                    return false;
                }
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
                if (!this.hasTriedHashChecks) {
                    this.hasTriedHashChecks = true;
                    LogHelper.info("An error occured while checking the MD5 hash, trying again...");
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    catch (InterruptedException ignore) {}

                    return this.checkHash();
                }
                else {
                    e.printStackTrace();
                    Main.errorContext = "System error while checking the MD5 hash.";

                    return false;
                }
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
                if (!this.hasTriedHashChecks) {
                    this.hasTriedHashChecks = true;
                    LogHelper.info("An error occured while checking the CRC32 hash, trying again...");
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    catch (InterruptedException ignore) {}

                    return this.checkHash();
                }
                else {
                    e.printStackTrace();
                    Main.errorContext = "System error while checking the CRC32 hash.";

                    return false;
                }
            }
        }

        return true; // If there wasn't any check failing and returning false before, this probably means the hash is correct.
    }

    public boolean hasHash() {
    // Returns whether the resource has any configured hash attached or not.

        return this.CRC32 != null || this.MD5 != null || this.SHA1 != null || this.SHA256 != null || this.SHA512 != null;
    }


    public boolean checkCache() {
    // Checks the cache folder to not have to redownload mods everytime.
    // If the mod is already cached, it will just copy the file to the given destination instead.

        if (Config.disableCache || !hasHash()) return false;

        String[] files = FileHelper.listDirectory("mcinstance-cache", false);
        for (String s : files) {
            if (this.SHA512 != null && s.equalsIgnoreCase("SHA512-" + this.SHA512)) return FileHelper.copy("mcinstance-cache" + File.separator + s, this.destination);
            if (this.SHA256 != null && s.equalsIgnoreCase("SHA256-" + this.SHA256)) return FileHelper.copy("mcinstance-cache" + File.separator + s, this.destination);
            if (this.SHA1 != null && s.equalsIgnoreCase("SHA1-" + this.SHA1)) return FileHelper.copy("mcinstance-cache" + File.separator + s, this.destination);
            if (this.MD5 != null && s.equalsIgnoreCase("MD5-" + this.MD5)) return FileHelper.copy("mcinstance-cache" + File.separator + s, this.destination);
            if (this.CRC32 != null && s.equalsIgnoreCase("CRC32-" + this.CRC32)) return FileHelper.copy("mcinstance-cache" + File.separator + s, this.destination);
        }

        return false;
    }


    private boolean genCurseforgeUrl() {
    // Tries to generate the curseforge URL in case the resource's API usage is disabled or if the sourceFilename and fileId are provided.
    // Returns true if the download was succesful.

        if (this.sourceFileName != null && this.fileId != null) {
            try {
                this.url = "https://media.forgecdn.net/files/" + Integer.parseInt(this.fileId.substring(0, 4)) + "/" + Integer.parseInt(this.fileId.substring(4)) + "/" + URLEncoder.encode(this.sourceFileName, "UTF-8");
            }
            catch (Exception ignore) {}

            return WebHelper.downloadFile(this.url, this.destination);
        }

        return false;
    }


    private boolean getCurseforgeData() {
    // Grabs some properties for Curseforge downloads.
    // This is using an unofficial API proxy to allow for any kind of files to work. Might have to switch again later on.
    // This project cannot rely on the official CFCore by default as there's both the needs to hide the API key and to download "opted-out" files and Overwolf doesn't propose any solution for those issues.

        String apiUrl = Config.curseforgeURL + "/v1/mods/" + (this.projectId != null? this.projectId : "") + "/files/" + (this.fileId != null? this.fileId : "");

        if (WebHelper.downloadFile(apiUrl, Config.configFolder + "temp" + File.separator + "curseforgeData.json")) {
            String[] lines = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "curseforgeData.json");
            String file = "";
            if (lines.length > 0) file = lines[0];

            String[] splitted;

            // Download URL
            if (file.contains("downloadUrl")) {
                splitted = file.split("\"downloadUrl\":\"");
                String url = "";
                if (splitted.length >= 2) {
                    url = splitted[1];
                    url = url.substring(0, url.indexOf("\""));
                }
                if (url.length() >= 1) this.url = url;
            }

            // Source file name
            if (this.sourceFileName == null && file.contains("fileName")) {
                splitted = file.split("\"fileName\":\"");
                String fileName = "";
                if (splitted.length >= 2) {
                    fileName = splitted[1];
                    fileName = fileName.substring(0, fileName.indexOf("\""));
                }
                if (fileName.length() >= 1) this.sourceFileName = fileName;
            }

            // SHA1 hash
            if (this.SHA1 == null) {
                splitted = file.split("\",\"algo\":1");
                String sha1 = "";
                if (splitted.length >= 2) {
                    sha1 = splitted[0].substring(splitted[0].lastIndexOf("\"") + 1);
                }
                if (sha1.length() >= 1) this.SHA1 = sha1;
            }

            // MD5 hash
            if (this.MD5 == null) {
                splitted = file.split("\",\"algo\":2");
                String md5 = "";
                if (splitted.length >= 2) {
                    md5 = splitted[0].substring(splitted[0].lastIndexOf("\"") + 1);
                }
                if (md5.length() >= 1) this.MD5 = md5;
            }

            return this.url.length() > 0 || this.sourceFileName != null;
        }

        return false;
    }


    public boolean getModrinthData() {
    // Grabs some properties for Modrinth downloads.
    // Uses the v2 API. Might be subject to change.

        String apiUrl = "https://api.modrinth.com/v2/version/" + (this.versionId != null? this.versionId : "");
        if (WebHelper.downloadFile(apiUrl, Config.configFolder + "temp" + File.separator + "modrinthData.json")) {
            String[] lines = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "modrinthData.json");
            String file = "";
            if (lines.length > 0) file = lines[0];

            if (file.contains("\"files\":")) {
                file = file.split("\"files\":\\[\\{")[1];
                file = file.substring(0, file.indexOf("]"));

                String[] files = file.split("},\\{");

                file = "";
                for (String s : files) { // Grabs the file that matches the given file name.
                    if (s.contains("\"filename\":\"" + this.sourceFileName + "\"") || files.length == 1) {
                        file = s;
                        break;
                    }
                }
                if (file.length() < 1) return false;

                String[] splitted;

                // Download URL
                if (file.contains("url")) {
                    splitted = file.split("\"url\":\"");
                    String url = "";
                    if (splitted.length >= 2) {
                        url = splitted[1];
                        url = url.substring(0, url.indexOf("\""));
                    }
                    if (url.length() >= 1) this.url = url;
                }

                // SHA1 hash
                if (this.SHA1 == null && file.contains("\"sha1\":\"")) {
                    splitted = file.split("\"sha1\":\"");
                    String sha1 = "";
                    if (splitted.length >= 2) {
                        sha1 = splitted[1];
                        sha1 = sha1.substring(0, sha1.indexOf("\""));
                    }
                    if (sha1.length() >= 1) this.SHA1 = sha1;
                }

                // SHA512 hash
                if (this.SHA512 == null && file.contains("\"sha512\":\"")) {
                    splitted = file.split("\"sha512\":\"");
                    String sha512 = "";
                    if (splitted.length >= 2) {
                        sha512 = splitted[1];
                        sha512 = sha512.substring(0, sha512.indexOf("\""));
                    }
                    if (sha512.length() >= 1) this.SHA512 = sha512;
                }

                return this.url.length() > 0;
            }
        }

        return false;
    }


    public boolean downloadFile() {
    // Downloads the file based on the given parameters and saves it to the destination path.
    // Also checks the URL against the StopModReposts list.

        if (checkCache()) return true;

        // Checks the URL against the StopModReposts list.
        for (int i = 0; i < Main.blacklist.length; i += 1) {
            if (this.url.contains(Main.blacklist[i])) {
                Main.errorContext = "URL is from a repost website.";
                return false;
            }
        }

        // Support for curseforge's API.
        if (this.type.equals("curseforge")) {

            // If the file already has hashes defined and is already in cache, it won't do any request.
            if (this.checkCache()) return true;

            // If the mod can generate the download URL without doing any API call, it will do that instead.
            if (projectId == null && genCurseforgeUrl()) return true;

            // Otherwise, it will use the Curseforge API to grab stuff like the download url, file name and SHA-1 hash.
            if (getCurseforgeData()) {
                if (checkCache()) return true; // If the file is already cached using this downloaded hash, it won't download anything else.

                String urlBackup = this.url;
                if (genCurseforgeUrl()) return true; // If the mod was able to generate a download URL from those properties, it will use it.
                else this.url = urlBackup;
            }
            else {
                Main.errorContext = "Error while getting the data from Curseforge.";
                return false;
            }

            this.follows = new String[0]; // Sets the follows list to be empty, so the WebHelper class doesn't try to follow it if the user specified it.
        }


        // Support for Modrinth's API.
        else if (this.type.equals("modrinth")) {

            // If the file already has hashes defined and is already in cache, it won't do any request.
            if (this.checkCache()) return true;

            if (getModrinthData()) {
                if (checkCache()) return true;
            }
            else {
                Main.errorContext = "Error while getting the data from Modrinth.";
                return false;
            }

            this.follows = new String[0]; // Sets the follows list to be empty, so the WebHelper class doesn't try to follow it if the user specified it.
        }

        // If it didn't download it from before (so either it's not of curseforge type, the resource didn't have a sourceFileName specified, or the direct download failed),
        // it will download it here. Anything other than the curseforge or modrinth type will directly go here.

        if (this.follows.length <= 0) return WebHelper.downloadFile(this.url, this.destination);
        else return WebHelper.downloadFile(this.url, this.destination, this.follows);
    }

}