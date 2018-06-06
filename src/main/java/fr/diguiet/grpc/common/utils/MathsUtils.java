package fr.diguiet.grpc.common.utils;

/**
 * Utils class with static method to simplify the use of maths related function
 */
public final class MathsUtils {

    /**
     * Class is not instantiable and inheritable
     */
    private MathsUtils() {

    }

    /**
     * Return a scaled value from it's source interval to the destination interval
     * @param valueIn The value to scale
     * @param baseMin The minimum source value
     * @param baseMax The maximum source value
     * @param limitMin The minimum destination value
     * @param limitMax The maximum destination value
     * @return The scaled value
     * @throws IllegalArgumentException if baseMax - baseMin = 0
     */
    public static double scale(final double valueIn,
                                final double baseMin, final double baseMax,
                                final double limitMin, final double limitMax) {
        if ((baseMax - baseMin) == 0)
            throw new IllegalArgumentException("Invalid base interval");
        return (((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin);
    }
}
