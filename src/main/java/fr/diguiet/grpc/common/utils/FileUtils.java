package fr.diguiet.grpc.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Utils class with static method to simplify the use of file, folder, directory related function and object
 * @see File
 */
public final class FileUtils {

    /**
     * Class is not instantiable and inheritable
     */
    private FileUtils() {

    }

    /**
     * Get the current working directory
     * @return the current working directory
     */
    public static File getCurrentWorkingDirectory() {
        return (Paths.get("").toAbsolutePath().toFile());
    }

    /**
     * Create a folder at the specified path if it is not already present
     * @param folderPath The path to the folder
     * @return The of the folder
     * @throws IOException If unable to create the directory
     */
    public static File createFolderIfNotPresent(final File folderPath) throws IOException {
        Objects.requireNonNull(folderPath);
        if (!folderPath.exists()) {
            if (!folderPath.mkdir()) {
                throw new IOException("Failed to create directory at " + folderPath.getAbsolutePath());
            }
        }
        return (folderPath);
    }

    /**
     * Create a folder at the specified name and path if it is not already present
     * @param folderName The path to the parent folder
     * @param path The name of the folder
     * @return The path of the folder
     * @throws IOException If unable to create the directory
     */
    public static File createFolderIfNotPresent(final String folderName, final File path) throws IOException {
        return (FileUtils.createFolderIfNotPresent(new File(path, folderName)));
    }

}
