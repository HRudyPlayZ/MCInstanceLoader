package com.hrudyplayz.mcinstanceloader.utils;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hrudyplayz.mcinstanceloader.Config;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.FileUtils;


/**
An helper class to do various file operations more easily.

@author HRudyPlayZ
*/
@SuppressWarnings("UnusedReturnValue")
public class FileHelper {

    /**
    Checks whether a given path is possible and valid regardless of the OS.
    Used to validate a string path. Used by every method of the {@link FileHelper} class to skip IOExceptions.

    @param path The string to validate.
    @return Whether the path is valid or not.
    */
    private static boolean isInvalidPath(String path) {
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

    /**
    Creates a new directory at the specified path.
    If the directory already exists, it doesn't do anything and succeeds.
    If one of the parent directories doesn't exist, it creates them recursively.

    @param path The path where to create the directory (Must contain its name at the end).
    @return Whether the operation succeeded or not.
    */
    public static boolean createDirectory(String path) {
        return createDirectory(path, false);
    }

    private static boolean createDirectory(String path, boolean doneIOException) {
        try {
            if (isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            Files.createDirectories(realPath);

            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            if (!doneIOException) {
                LogHelper.info("An error occured while creating the directory, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return createDirectory(path, true);
            }
            else return false;
        }
    }

    /**
    Creates a new empty file at the specified location (must contain the file name and extension, as a {@link String}).
    If the file already exists, it overwrites the previous one.
    If one of the parent directories doesn't exist or if the given path is actually a folder, it fails.

    @param path The path where to create the file (must contain its name and possibly extension at the end).
    @return Whether the operation succeeded or not.
    */
    public static boolean createFile(String path) {
        return createFile(path, false);
    }

    private static boolean createFile(String path, boolean doneIOException) {
        try {
            if (isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            if (FileHelper.exists(path)) FileHelper.delete(path);

            if (path.contains(File.separator)) {
                String folder = path.substring(0, path.lastIndexOf(File.separator));
                if (!FileHelper.exists(folder)) FileHelper.createDirectory(folder);
            }

            Path realPath = Paths.get(path);
            Files.createFile(realPath);

            return true;
        }
        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occured while creating the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return createFile(path, true);
            }

            e.printStackTrace();
            return false;
        }
    }

    /**
    Appends a specific list of line strings to the end of a given file.
    In other words, this adds a list of lines, each represented by a {@link String} at the end of a file.
    If the file doesn't exist, it creates it.

    @param path The path of the file to modify.
    @param lines The list of lines to add to the end of the file.
    @return Whether the operation succeeded or not.
    */
    public static boolean appendFile(String path, String[] lines) {
        return appendFile(path, lines, false);
    }

    private static boolean appendFile(String path, String[] lines, boolean doneIOException) {
        try {
            if (isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            if (!Files.exists(realPath)) createFile(path);
            Files.write(realPath, Arrays.asList(lines), StandardCharsets.UTF_8, StandardOpenOption.APPEND);

            return true;
        }
        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occured while changing the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return appendFile(path, lines, true);
            }

            e.printStackTrace();
            return false;
        }
    }

    /**
    Replaces a specific file's content with a given list of lines (as {@link String}s).
    If the file doesn't exist, it creates it.

    @param path The path of the file to modify.
    @param lines The list of lines that are present in the file.
    @return Whether the operation succeeded or not.
    */
    public static boolean overwriteFile(String path, String[] lines) {
        return overwriteFile(path, lines, false);
    }

    private static boolean overwriteFile(String path, String[] lines, boolean doneIOException) {
        try {
            if (isInvalidPath(path)) return false; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            if (!exists(path)) createFile(path);
            Files.write(realPath, Arrays.asList(lines), StandardCharsets.UTF_8);

            return true;
        }

        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occured while overwriting the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return overwriteFile(path, lines, true);
            }

            e.printStackTrace();
            return false;
        }
    }


    /**
    Returns a list of lines as {@link String}s present inside a specific file.
    It separates the file using the UTF-8 charset. And considers {@value \n} as the delimiter.
    It returns an empty list if an {@link IOException} occurs.

    @param path The path of the file to read
    @return The list of lines present in the file.
    */
    public static String[] listLines(String path) {
       return listLines(path, false);
    }

    private static String[] listLines(String path, boolean doneIOException) {
        try {
            if (isInvalidPath(path)) return new String[0]; // Checks if the given path isn't valid.

            Path realPath = Paths.get(path);
            List<String> list = Files.readAllLines(realPath);

            return list.toArray(new String[0]);
        }

        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occured while reading the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return listLines(path, true);
            }

            e.printStackTrace();
            return new String[0];
        }
    }


    /**
    Returns the list of files and folders inside a specific directory.
    It can be set to either include the files inside the directories recursively or not.
    It returns an empty list if the file doesn't exist or isn't a directory.

    @param path The path of the directory to read
    @param isRecursive Whether the list should include the content of subfolders recursively.
    @return The list of files and directories present in the specific path
    */
    public static String[] listDirectory(String path, boolean isRecursive) {
        if (isInvalidPath(path)) return new String[0]; // Checks if the given path isn't valid.

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
        else result = new String[0];

        return result;
    }


    /**
    Copies an entire folder or a file to a specific location.
    If the target path already exists, it deletes it.
    Files need to have the target file name (and extension) specified too.

    @param source The path of the content to copy
    @param target The path where to save the copied content
    @return Whether the operation succeeded or not.
    */
    public static boolean copy(String source, String target) {
        return copy(source, target, false);
    }

    private static boolean copy(String source, String target, boolean doneIOException) {
        try {
            if (isInvalidPath(source)) return false; // Checks if the source path isn't valid.
            if (isInvalidPath(target)) return false; // Checks if the target path isn't valid.

            if (target.contains(File.separator)) {
                String path = target.substring(0, target.lastIndexOf(File.separator));
                if (!exists(path)) createDirectory(path);
            }

            if (!isDirectory(target) && exists(target)) overwriteFile(target, new String[0]);

            if (isDirectory(source)) FileUtils.copyDirectory(new File(source), new File(target));
            else Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);

            return true;
        }

        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occured while copying the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return copy(source, target, true);
            }

            e.printStackTrace();
            return false;
        }
    }


    /**
    Moves (copies and deletes) an entire folder or a file to a specific location.
    If the the target files already exists, it deletes it.
    It will only delete the original file if the copy operation is successful.
    Files need to have the target file name (and extension) specified too.

    @param source The path of the content to copy and delete when successful.
    @param target The path where to save the copied content.
    @return Whether the operation succeeded or not.
    */
    public static boolean move(String source, String target) {
        boolean result = copy(source, target);
        if (result) result = delete(source);

        return result;
    }


    /**
    Deletes an entire folder (recursively) or file from the disk.
    This method doesn't validate the file path in case something manages to get through and needs to be deleted.

    @param path The path of the content to delete.
    @return Whether the operation succeeded or not.
    */
    public static boolean delete(String path) {
        return delete(path, false);
    }

    private static boolean delete(String path, boolean doneIOException) {
        try {
            Path realPath = Paths.get(path);

            if (!Files.exists(realPath)) return false;

            if (Files.isDirectory(realPath)) FileUtils.deleteDirectory(new File(path));
            else Files.delete(realPath);

            return true;
        }

        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occured while deleting the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return delete(path, true);
            }

            e.printStackTrace();
            return false;
        }
    }


    /**
    Checks if a file or folder exists.
    Basically acts as a lazy interface for {@link Files#exists(Path, LinkOption...)}.

    @param path The path to check
    @return Whether the path exists or not.
    */
    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }


    /**
    Checks whether the given path corresponds to a folder or a file.
    Returns true if the path is a folder and false otherwise.
    Basically acts as a lazy interface for {@link Files#isDirectory(Path, LinkOption...)}.

    @param path The path to check
    @return Whether the path is a directory or not.
    */
    public static boolean isDirectory(String path) {
        return Files.isDirectory(Paths.get(path));
    }
}
