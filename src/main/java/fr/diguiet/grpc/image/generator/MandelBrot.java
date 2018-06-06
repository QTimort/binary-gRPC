package fr.diguiet.grpc.image.generator;

import fr.diguiet.grpc.common.utils.ImageUtils;
import fr.diguiet.grpc.common.utils.MathsUtils;

import javax.annotation.concurrent.Immutable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class representation of a MandelBrot fractal
 * Instance is immutable
 * @see <a href="https://en.wikipedia.org/wiki/Mandelbrot_set">https://en.wikipedia.org/wiki/Mandelbrot_set</a>
 */
@Immutable
public class MandelBrot {
    private final int maxIteration;
    private final int width;
    private final int height;
    private final Color colorPalette[];

    /**
     * Create a new MandelBrot instance from a builder
     * @param builder
     */
    private MandelBrot(final Builder builder) {
        this.maxIteration = builder.maxIteration;
        this.width = builder.width;
        this.height = builder.height;
        this.colorPalette = ImageUtils.generateColorPalette(
                builder.beginPaletteColor,
                builder.endPaletteColor,
                this.maxIteration + 1);
    }

    /**
     * Create a new builder instance
     * @return
     */
    public static Builder newInstance() {
        return (Builder.newInstance());
    }

    /**
     * Builder class to create a MandelBrot instance
     */
    public static class Builder {
        private int maxIteration = 20;
        private int width = 512;
        private int height = 512;
        private Color beginPaletteColor = Color.BLUE;
        private Color endPaletteColor = Color.BLACK;

        /**
         * Create a new builder instance
         * @return
         */
        public static Builder newInstance() {
            return (new Builder());
        }

        /**
         * Instantiate a new builder
         */
        private Builder() {

        }

        /**
         * Set the maximum number of iteration
         * @param maxIteration the maximum number of iteration
         * @return The builder
         * @throws IllegalArgumentException if max iteration below 1
         */
        public Builder setMaxIteration(final int maxIteration) {
            if (maxIteration < 1)
                throw new IllegalArgumentException("Max iteration must be greater than 1");
            this.maxIteration = maxIteration;
            return (this);
        }

        /**
         * Set the width of mandelbrot to compute
         * @param width the width of the mandelbrot to compute
         * @return The builder
         * @throws IllegalArgumentException if width is below 1
         */
        public Builder setWidth(final int width) {
            if (width < 1)
                throw new IllegalArgumentException("Width iteration must be greater than 1");
            this.width = width;
            return (this);
        }

        /**
         * Set the height of the madelbrot to compute
         * @param height the height of the mandelbrot to compute
         * @return The builder
         * @throws IllegalArgumentException if height is below 1
         */
        public Builder setHeight(final int height) {
            if (height < 1)
                throw new IllegalArgumentException("Height iteration must be greater than 1");
            this.height = height;
            return (this);
        }

        /**
         * Set the begin palette color
         * @param beginPaletteColor the begin palette color
         * @return The builder
         */
        public Builder setBeginPaletteColor(final Color beginPaletteColor) {
            Objects.requireNonNull(beginPaletteColor);
            this.beginPaletteColor = beginPaletteColor;
            return (this);
        }

        /**
         * Set the end palette color
         * @param endPaletteColor the end palette color
         * @return The builder
         */
        public Builder setEndPaletteColor(final Color endPaletteColor) {
            Objects.requireNonNull(endPaletteColor);
            this.endPaletteColor = endPaletteColor;
            return (this);
        }

        /**
         * Build the MandelBrot and return an instance
         * @return a MandelBrot instance
         */
        public MandelBrot build() {
            return (new MandelBrot(this));
        }
    }

    /**
     * Compute and generate the mandelbrot into an image
     * @return The generated image
     */
    public BufferedImage generateImage() {
        final BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                final double x0 = MathsUtils.scale(x, 0, this.width, -2.5, 1);
                final double y0 = MathsUtils.scale(y, 0, this.height, -1, 1);
                double itX = 0.0d;
                double itY = 0.0d;
                int i = 0;
                while ((Math.pow(itX, 2) + Math.pow(itY, 2)) < 4 && (i < this.maxIteration)) {
                    double tmpX = Math.pow(itX, 2) - Math.pow(itY, 2) + x0;
                    itY = 2 * itX * itY + y0;
                    itX = tmpX;
                    ++i;
                }
                image.setRGB(x, y, this.colorPalette[i].getRGB());
            }
        }
        return (image);
    }

    /**
     * The string representation of the instance
     * @return the string representation of the instance
     */
    @Override
    public String toString() {
        return "MandelBrot{" +
                "maxIteration=" + maxIteration +
                ", width=" + width +
                ", height=" + height +
                ", colorPalette=" + Arrays.toString(colorPalette) +
                '}';
    }
}
