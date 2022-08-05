package com.hrudyplayz.mcinstanceloader.utils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;


/**
An helper class to make zip management easier.
It uses the Zip4J library (even though i would've prefered to not add an external dependency).
Java is pretty annoying with them by default, but srikanth-lingala made a massive work to
provide an easy to use and fairly licensed library, so huge thanks to him.

Current Zip4J version: 2.11.1
The Zip4J library is licensed under the <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.

@author srikanth-lingala
@author HRudyPlayZ
*/
@SuppressWarnings("unused")
public class ZipHelper {

    /**
    Archives a folder's content or a file into a zip file saved at a given location.
    If the target location already exists and isn't a valid zip file, it will delete it first.

    @param source The path of the content to archive.
    @param saveLocation The path where to save the outputed zip.
    @param putContentInSubdirectory If the source content is a folder, whether the function should put the content inside a subdirectory with the folder's name or not.
    @param overwriteFile If set to true, this will delete the existing file instead of adding to it.
    @param password The password of the zip, set to null to disable it.
    @return Whether the operation succeeded or not.
    */
    public static boolean zip(String source, String saveLocation, boolean putContentInSubdirectory, boolean overwriteFile, String password) {
        try {
            if (!FileHelper.exists(source)) return false; // If the source doesn't exist, immediately return false.

            // Deletes the save location if it already exists and isn't a valid zip file or the overwriteFile argument is set to true.
            if (FileHelper.exists(saveLocation) && (!isValidZip(saveLocation) || overwriteFile)) FileHelper.delete(saveLocation);

            ZipFile zipFile;
            ZipParameters zipParameters = new ZipParameters();

            if (password != null) {  // Sets the zip password if added.
                zipParameters.setEncryptFiles(true);
                zipParameters.setEncryptionMethod(EncryptionMethod.AES);

                zipFile = new ZipFile(saveLocation, password.toCharArray());
            }
            else zipFile = new ZipFile(saveLocation);

            if (!FileHelper.isDirectory(source)) { // If the source is just a file, we zip this file and don't continue further.
                zipFile.addFile(source, zipParameters);
                return true;
            }

            // Otherwise, we prepare to add a folder's content to the zip instead.
            if (!source.endsWith(File.separator)) source += File.separator;

            if (putContentInSubdirectory) zipFile.addFolder(new File(source));
            else {
                String[] fileList = FileHelper.listDirectory(source, false);

                for (int i = 0; i < fileList.length; i += 1) {
                    fileList[i] = source + fileList[i];

                    File file = new File(fileList[i]);
                    if (file.isDirectory()) zipFile.addFolder(file, zipParameters);
                    else zipFile.addFile(file, zipParameters);
                }
            }

            return true;
        }

        catch (net.lingala.zip4j.exception.ZipException e) {
            return false;
        }
    }

    /**
     Archives a folder's content or a file into a zip file saved at a given location.
     If the target location already exists and isn't a valid zip file, it will delete it first.
     The outputed zip won't have any password.

     @param source The path of the content to archive.
     @param saveLocation The path where to save the outputed zip.
     @param putContentInSubdirectory If the source content is a folder, whether the function should put the content inside a subdirectory with the folder's name or not.
     @param overwriteFile If set to true, this will delete the existing file instead of adding to it.
     @return Whether the operation succeeded or not.
     */
    public static boolean zip(String source, String saveLocation, boolean putContentInSubdirectory, boolean overwriteFile) {
        return zip(source, saveLocation, putContentInSubdirectory, overwriteFile, null);
    }

    /**
     Archives a folder's content or a file into a zip file saved at a given location.
     If the target location already exists and isn't a valid zip file, it will delete it first.
     If a zip file already exists, this will try to add to it instead of overwriting it. The outputed zip won't have any password.

     @param source The path of the content to archive.
     @param saveLocation The path where to save the outputed zip.
     @param putContentInSubdirectory If the source content is a folder, whether the function should put the content inside a subdirectory with the folder's name or not.
     @return Whether the operation succeeded or not.
     */
    public static boolean zip(String source, String saveLocation, boolean putContentInSubdirectory) {
        return zip(source, saveLocation, putContentInSubdirectory, false, null);
    }


    /**
    Extracts a given list of files from a zip file into a specified location.

    @param zipFile The path of the zip file to extract from.
    @param destination The path where to save the extracted content.
    @param password The password of the zip if there's one, set to null otherwise.
    @param filesToExtract A list of files to extract, uses "/" for the folder separator. Add a "/" after folder names.
    @return Whether the operation succeeded or not.
    */
    public static boolean extract(String zipFile, String destination, String password, String[] filesToExtract) {
        try {
            ZipFile file;
            if (password != null) file = new ZipFile(zipFile, password.toCharArray());
            else file = new ZipFile(zipFile);

            if (filesToExtract != null) {
                for (String s : filesToExtract) file.extractFile(s, destination);
            }
            else file.extractAll(destination);

            return true;
        }
        catch (net.lingala.zip4j.exception.ZipException e) {
            return false;
        }
    }

    /**
    Extracts an entire zip file's content into a specified location.

    @param zipFile The path of the zip file to extract from.
    @param destination The path where to save the extracted content.
    @param password The password of the zip if there's one, set to null otherwise.
    @return Whether the operation succeeded or not.
    */
    public static boolean extract(String zipFile, String destination, String password) {
        return extract(zipFile, destination, password, null);
    }

    /**
    Extracts an entire unencrypted (passwordless) zip file's content into a specified location.

    @param zipFile The path of the zip file to extract from.
    @param destination The path where to save the extracted content.
    @return Whether the operation succeeded or not.
    */
    public static boolean extract(String zipFile, String destination) {
        return extract(zipFile, destination, null);
    }


    /**
    Checks whether a given zip file is encrypted (has a password) or not.
    Will return false if the given path isn't a zip file.

    @param zipFile The path of the zip file to check.
    @return Whether the zip file is encrypted (has a password) or not.
    */
    public static boolean isEncrypted(String zipFile) {
        try {
            return new ZipFile(zipFile).isEncrypted();
        }
        catch (net.lingala.zip4j.exception.ZipException e) {
            return false;
        }
    }

    /**
    Checks whether a given path is a valid zip file or not.

    @param zipFile The path to check.
    @return Whether the given path is a valid zip file or not.
    */
    public static boolean isValidZip(String zipFile) {
        return new ZipFile(zipFile).isValidZipFile();
    }


    /**
    Returns the list of files present inside a given zip file.
    Will return an empty list if an error happens or the given path isn't a valid zip file.

    @param zipFile The path of the zip file to read.
    @return The list of files inside the zip file.
    */
    public static String[] listZip(String zipFile) {
        try {
            List<FileHeader> fileHeaders = new ZipFile(zipFile).getFileHeaders();
            String[] result = new String[fileHeaders.size()];

            for (int i = 0; i < fileHeaders.size(); i += 1) {
                result[i] = fileHeaders.get(i).getFileName();
            }

            return result;
        }
        catch (net.lingala.zip4j.exception.ZipException e) {
            return new String[0];
        }
    }


    /**
    Deletes a specific list of files or folders from a given zip file.
    To delete a folder, Add a "/" after its name.

    @param zipFile The path of the zip file to modify.
    @param filesToDelete The list of files to delete from the archive.
    @return Whether the operation succeeded or not.
    */
    public static boolean delete(String zipFile, String[] filesToDelete) {
        try {
            ZipFile file = new ZipFile(zipFile);
            for (String s : filesToDelete) file.removeFile(s);
            return true;
        }
        catch (net.lingala.zip4j.exception.ZipException e) {
            return false;
        }
    }

}
