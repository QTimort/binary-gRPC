package fr.diguiet.grpc.common.utils;

import java.util.Objects;

/**
 * Utils class with static method to simplify the use of string function and object
 * @see String
 */
public final class StringUtils {

    /**
     * Class is not instantiable and inheritable
     */
    private StringUtils() {

    }

    /**
     * Replace the newline from a string with the specified replacer in a new string
     * @param str The string to clean
     * @param replaceBy The string that replace newline
     * @return A new string without newline
     */
    public static String replaceNewLine(final String str, final String replaceBy) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(replaceBy);
        return (str.replace("\r", "").replace("\n", replaceBy));
    }
}
