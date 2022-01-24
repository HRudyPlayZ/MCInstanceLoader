package com.hrudyplayz.mcinstanceloader.utils;

import java.io.*;
import java.util.List;

import com.hrudyplayz.mcinstanceloader.utils.FileHelper;
import com.hrudyplayz.mcinstanceloader.utils.LogHelper;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ZipHelper {
// This class aims to make zip files management much easier.
// It utilizes the zip4j library. I would've prefered to not rely on it,
// but Java is just too annoying when it comes to zip files, and i don't want to have to do
// this guy's work over again, so huge thanks to srikanth-lingala for providing a fairly
// licensed and easy to use library i could work with.

// The zip4j library is licensed un the Apache License 2.0.


    public static boolean zip (String input, String outputZip, String password, boolean doNotOverwrite, boolean putContentInSubdirectory) {
    // Lets you archive a folder's content into a zip file.
    // The folder itself is only included in the archive if you set putContentInSubdirectory to true.

        try {
            if ((!doNotOverwrite) && (!FileHelper.isDirectory(outputZip)) && FileHelper.exists(outputZip)) FileHelper.delete(outputZip);

            ZipFile zipFile;
            ZipParameters zipParameters = new ZipParameters();
            if (password != null) {
                zipParameters.setEncryptFiles(true);
                zipParameters.setEncryptionMethod(EncryptionMethod.AES);

                zipFile = new ZipFile(outputZip, password.toCharArray());
            }
            else zipFile = new ZipFile(outputZip);

            if (!new File(input).isDirectory()) {
                zipFile.addFile(input, zipParameters);
                return true;
            }

            if (putContentInSubdirectory) zipFile.addFolder(new File(input));
            else {
                String[] fileList = FileHelper.listDirectory(input, false);

                if (!input.endsWith(File.separator)) input += File.separator;

                for (int i = 0; i < fileList.length; i += 1) {
                    fileList[i] = input + fileList[i];

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


    public static boolean zip (String input, String outputZip, String password, boolean doNotOverwrite) {
    // Lets you archive a folder's content into a zip file.
    // This makes the putContentInDirectory setting an option and not a required argument.

        return zip(input, outputZip, password, doNotOverwrite, false);
    }

    public static boolean zip (String input, String outputZip, String password) {
    // Lets you archive a folder's content into a zip file.
    // This makes the doNotOverwrite setting an option and not a required argument.

        return zip(input, outputZip, password, false);
    }

    public static boolean zip (String input, String outputZip) {
    // Lets you archive a folder's content into a zip file.
    // This makes the password setting an option and not a required argument.

        return zip(input, outputZip, null);
    }


    public static boolean extract(String zipFile, String destinationFolder, String password, String fileToExtract) {
    // Lets you extract a zip archive into a specific folder.

        try {
            ZipFile file;
            if (password != null) file = new ZipFile(zipFile, password.toCharArray());
            else file = new ZipFile(zipFile);

            if (fileToExtract != null) file.extractFile(fileToExtract, destinationFolder);
            else file.extractAll(destinationFolder);

            return true;
        }

        catch (net.lingala.zip4j.exception.ZipException e) {
            return false;
        }

    }

    public static boolean extract(String zipFile, String destinationFolder, String password) {
    // Lets you extract a zip archive into a specific folder.
    // This makes the fileToExtract setting optional.

        return extract(zipFile, destinationFolder, password, null);
    }

    public static boolean extract(String zipFile, String destinationFolder) {
    // Lets you extract a zip archive into a specific folder.
    // This makes the password setting optional.

        return extract(zipFile, destinationFolder, null);
    }


    public static boolean isEncrypted (String zipFile) {
    // Lets you know if a specific archive has a password.

        try {
            return new ZipFile(zipFile).isEncrypted();
        }

        catch (net.lingala.zip4j.exception.ZipException e) {
            return false;
        }
    }


    public static boolean isValidZip (String zipFile) {
    // Lets you know if a specific archive has a password.

        return new ZipFile(zipFile).isValidZipFile();
    }


    public static String[] listZip (String zipFile) {
    // Lets you know the list of files included in a specific zip file.

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


    public static boolean delete (String zipFile, String file) {
    // Lets you delete a specific file/folder in a zip archive.
    // Add a "/" after folder names.

        try {
            new ZipFile(zipFile).removeFile(file);
            return true;
        }

        catch (net.lingala.zip4j.exception.ZipException e) {
            LogHelper.error(e.getMessage());
            return false;
        }
    }

}
