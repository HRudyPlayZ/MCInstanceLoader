package com.hrudyplayz.mcinstanceloader.utils;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.FileUtils;



public class FileHelper {
// This class aims to make file management less stupid than it is by default.


    private static boolean isInvalidPath (String path) {
    // Makes sure a given path is possible, regardless of the OS.
    // Every other method will use it to have valid paths (just to make sure it gets handled properly, in case the system doesn't return an IOException for those).

        String[] Forbidden = new String[] {"..", "<", ">", ":", "\"", "|", "?", "*"}; // Any path containing those characters will be denied.
        String[] ForbiddenNames = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3",
                "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3",
                "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"}; // Any path containing those as folder/file names will be denied.

        String trimmedPath = path.toLowerCase().trim();

        // Checks if the path contains a forbidden character, or the two folder separators at once.
        for (String s : Forbidden) {
            if (trimmedPath.contains(s) || (trimmedPath.contains("/") && trimmedPath.contains("\\"))) return true;
        }

        // Checks if the path contains any forbidden names
        for (String forbiddenName : ForbiddenNames) {
            if (trimmedPath.equals(forbiddenName) || trimmedPath.contains(forbiddenName + "/") ||
                    trimmedPath.contains(forbiddenName + "\\") || trimmedPath.contains("/" + forbiddenName) ||
                    trimmedPath.contains("\\" + forbiddenName)) {
                LogHelper.error("Could not validate the path " + path);
                return true;
            }
        }

        return false; // If nothing weird was found, then i guess it is valid.
    }


    public static boolean createDirectory (String path) {
    // Creates a new directory in the specified path (as a string).
    // If the directory already exists, it doesn't do anything (just suceeds).
    // If one of the parent directories doesn't exist, it creates them recursively.

        try {

            if (FileHelper.isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            Files.createDirectories(realPath);

            return true;
        }

        catch (IOException e) {
            return false;
        }

    }


    public static boolean createFile (String path) {
    // Creates a new empty file at the given path (must contain its name, as a string).
    // If the file already exists, it deletes the previous one.
    // If one of the parent directories doesn't exist or if the given path is actually a folder, it fails.

        try {

            if (FileHelper.isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            Files.deleteIfExists(realPath);
            Files.createFile(realPath);

            return true;
        }

        catch (IOException e) {
            return false;
        }

    }


    public static boolean appendFile (String path, String[] lines) {
    // Adds a list of lines to the end of a specific file.
    // If the file doesn't exist, it creates it.

        try {

            if (FileHelper.isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            if (!Files.exists(realPath)) FileHelper.createFile(path);
            Files.write(realPath, Arrays.asList(lines), StandardCharsets.UTF_8, StandardOpenOption.APPEND);

            return true;
        }

        catch (IOException e) {
            return false;
        }

    }


    public static boolean overwriteFile (String path, String[] lines) {
    // Replaces a specific file's content with a precise list of lines.
    // If the file doesn't exist, it creates it.

        try {

            if (FileHelper.isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            FileHelper.createFile(path);
            Files.write(realPath, Arrays.asList(lines), StandardCharsets.UTF_8);

            return true;
        }

        catch (IOException e) {
            return false;
        }

    }


    public static String[] listLines (String path) {
    // Returns a list of each line in a specific file.

        try {

            if (FileHelper.isInvalidPath(path)) return new String[0]; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            List<String> list = Files.readAllLines(realPath);

            return list.toArray(new String[0]);
        }

        catch (IOException e) {
            return new String[0];
        }
    }


    public static String[] listDirectory(String path, boolean isRecursive) {
    // Returns the list of files and folders in a specific directory.
    // It can be recursive or not.

        if (FileHelper.isInvalidPath(path)) return new String[0]; // Checks if the given path isn't valid.

        String[] result;
        File file = new File(path);

        if (file.exists() && file.isDirectory() && isRecursive) {
            Collection<File> list = FileUtils.listFilesAndDirs(new File(path), TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
            result = new String[list.size() - 1];

            int cursor = 0;
            for (File i : list) {

                if (!i.getPath().equals(path)) {
                    result[cursor] = i.getPath();
                    result[cursor] = result[cursor].replace(path, "");

                    if (result[cursor].startsWith(File.separator)) result[cursor] = result[cursor].substring(1);
                    if (i.isDirectory()) result[cursor] += File.separator;
                    cursor += 1;
                }



            }
        }

        else if (file.exists() && file.isDirectory()) result = file.list();
        else {
            result = new String[0];
        }

        return result;
    }
    

    public static boolean copy (String source, String target, boolean replaceTarget) {
    // Copies either a folder or a file to a specific location.
    // Files need to have the target file name (with its extension) specified.

        try {

            Path sourcePath = Paths.get(source);

            if (FileHelper.isInvalidPath(source)) return false; // Checks if the source path isn't valid.
            if (FileHelper.isInvalidPath(target)) return false; // Checks if the target path isn't valid.


            if (target.contains(File.separator)) {
                String path = target.substring(0, target.lastIndexOf(File.separator));
                if (!FileHelper.exists(path)) FileHelper.createDirectory(path);
            }

            if (replaceTarget && FileHelper.exists(target)) FileHelper.delete(target);

            if (Files.isDirectory(sourcePath)) FileUtils.copyDirectory(new File(source), new File(target));
            else Files.copy(sourcePath, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);

            return true;
        }

        catch (IOException e) {
            return false;
        }
    }


    public static boolean move (String source, String target, boolean replaceTarget) {
    // Move either a folder or a file to a specific location.
    // Files need to have the target file name (with its extension) specified.
        boolean result = FileHelper.copy(source, target, replaceTarget);
        if (result) FileHelper.delete(source);

        return result;
    }


    public static boolean delete (String path) {
    // Deletes a specific file/folder from the disk.
    // This method should not validate the path, just in case something managed to go through it and you need to delete something.

        try {
            Path realPath = Paths.get(path);

            if (!Files.exists(realPath)) return false;

            if (Files.isDirectory(realPath)) FileUtils.deleteDirectory(new File(path));
            else Files.delete(realPath);

            return true;
        }
        catch (IOException e) {
            return false;
        }

    }


    public static boolean exists(String path) {
    // Returns a boolean indicating if a file/folder exists or not.
    // Basically just acts as a lazy interface for the Files.exists method.
    // I could have also used the File class, but both work fine.

        Path realPath = Paths.get(path);
        return Files.exists(realPath);
    }


    public static boolean isDirectory(String path) {
    // Returns a boolean indicating if a path corresponds to a folder or not.
    // Basically just acts as a lazy interface for the Files.isDirectory method.
    // I could have also used the File class, but both work fine.

        Path realPath = Paths.get(path);
        return Files.isDirectory(realPath);
    }
}
