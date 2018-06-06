package fr.diguiet.grpc.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

/**
 * Utils class with static method to simplify the use of image, color related function and object
 * @see BufferedImage
 * @see Color
 */
public final class ImageUtils {
    private static final String DEFAULT_IMG_EXT = "png";
    private static final int NB_GREY_COLORS = 256;

    /**
     * Class is not instantiable and inheritable
     */
    private ImageUtils() {

    }

    /**
     * Get the default image extension used in this class
     * @return The default image extension, for example "png"
     */
    public static String getDefaultImgExt() {
        return (ImageUtils.DEFAULT_IMG_EXT);
    }

    /**
     * Convert a RGB pixel into its greyscale representation
     * @param rgb The pixel
     * @return The greyscale of the pixel
     */
    public static byte toGrayScale(final int rgb) {
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = (rgb & 0xFF);
        final int grayLevel = (r + g + b) / 3;
        return ((byte) ((grayLevel << 16) + (grayLevel << 8) + grayLevel));
    }

    /**
     * Compute and return a new array containing the image grey levels
     * @param image The image to compute
     * @return The image grey levels
     * @see <a href="https://en.wikipedia.org/wiki/Grayscale">https://en.wikipedia.org/wiki/Grayscale</a>
     */
    public static long[] getLevels(final BufferedImage image) {
        Objects.requireNonNull(image);
        final long[] intensityMap = new long[ImageUtils.NB_GREY_COLORS];
        for (int y = 0; y < image.getWidth(); ++y) {
            for (int x = 0; x < image.getHeight(); ++x) {
                ++intensityMap[ImageUtils.toGrayScale(image.getRGB(x, y))];
            }
        }
        return (intensityMap);
    }

    /**
     * Mix two colors with a specific percentage
     * @param begin The begin color
     * @param end The end color
     * @param percent a value between 0 and 1 included representing the percentage of end color
     * @return the mixed color
     * @throws IllegalArgumentException if you provide an invalid percent
     */
    public static Color mixColors(final Color begin, final Color end, final double percent) {
        Objects.requireNonNull(begin);
        Objects.requireNonNull(end);
        final double inversePercent = 1.0 - percent;
        final int redPart = (int) (begin.getRed() * percent + end.getRed() * inversePercent);
        final int greenPart = (int) (begin.getGreen() * percent + end.getGreen() * inversePercent);
        final int bluePart = (int) (begin.getBlue() * percent + end.getBlue() * inversePercent);
        return (new Color(redPart, greenPart, bluePart));
    }

    /**
     * Save a BufferedImage into the specified file
     * @param image The image to save
     * @param file The file where to save the image
     * @throws IOException If unable to save the image at the specified file
     */
    public static void save(final BufferedImage image, final File file) throws IOException {
        Objects.requireNonNull(image);
        Objects.requireNonNull(file);
        ImageIO.write(image, ImageUtils.DEFAULT_IMG_EXT, file);
    }

    /**
     * Convert a BufferedImage into a new byte array
     * @param image The image to convert
     * @return A new byte array containing the image
     * @throws IOException If unable to write the BufferedImage into the byte array
     */
    public static byte[] toBytes(final BufferedImage image) throws IOException {
        Objects.requireNonNull(image);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, ImageUtils.DEFAULT_IMG_EXT, stream);
            stream.flush();
            return (stream.toByteArray());
        }
    }

    /**
     * Convert a byte array into a new BufferedImage
     * @param bytes the byte array to convert
     * @return A new BufferedImage
     * @throws IOException If unable to read or convert the byte array
     */
    public static BufferedImage fromBytes(final byte[] bytes) throws IOException {
        final InputStream in = new ByteArrayInputStream(bytes);
        return (ImageIO.read(in));
    }


    /**
     * Generator a color palette of the specified size from a color to another
     * @param begin The begin color
     * @param end The end color
     * @param length The length of palette
     * @return A new palette of the specified length with colors
     */
    public static Color[] generateColorPalette(final Color begin, final Color end, final int length) {
        Objects.requireNonNull(begin);
        Objects.requireNonNull(end);
        if (length < 1)
            throw new IllegalArgumentException("Palette length must be greater than 0");
        Color[] colorPalette = new Color[length];
        for (int i = 0; i < length; ++i) {
            colorPalette[i] = ImageUtils.mixColors(begin, end, (float)i / (float)length);
        }
        return (colorPalette);
    }
}
