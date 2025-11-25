package com.berational;

/**
 * Mathematical and machine precision constants for Let's Be Rational algorithm.
 *
 * Based on Peter Jäckel's LetsBeRational implementation.
 * These constants are critical for numerical stability and accuracy.
 */
public class Constants {

    // Machine precision constants for IEEE 754 double precision
    public static final double DBL_EPSILON = Math.ulp(1.0);  // Machine epsilon: ~2.22e-16
    public static final double DBL_MIN = Double.MIN_NORMAL;   // ~2.225e-308
    public static final double DBL_MAX = Double.MAX_VALUE;    // ~1.798e+308

    // Derived precision constants
    public static final double SQRT_DBL_EPSILON = Math.sqrt(DBL_EPSILON);
    public static final double FOURTH_ROOT_DBL_EPSILON = Math.sqrt(SQRT_DBL_EPSILON);
    public static final double EIGHTH_ROOT_DBL_EPSILON = Math.sqrt(FOURTH_ROOT_DBL_EPSILON);
    public static final double SIXTEENTH_ROOT_DBL_EPSILON = Math.sqrt(EIGHTH_ROOT_DBL_EPSILON);
    public static final double SQRT_DBL_MIN = Math.sqrt(DBL_MIN);
    public static final double SQRT_DBL_MAX = Math.sqrt(DBL_MAX);

    // Denormalization cutoff: set to 0 for positive results from denormalized inputs
    // Note: full machine accuracy cannot be achieved from denormalized inputs
    public static final double DENORMALIZATION_CUTOFF = 0.0;

    // Signal values for exceptional cases
    public static final double VOLATILITY_VALUE_TO_SIGNAL_PRICE_IS_BELOW_INTRINSIC = -DBL_MAX;
    public static final double VOLATILITY_VALUE_TO_SIGNAL_PRICE_IS_ABOVE_MAXIMUM = DBL_MAX;

    // Mathematical constants with high precision
    public static final double ONE_OVER_SQRT_TWO = 0.7071067811865475244008443621048490392848359376887;
    public static final double ONE_OVER_SQRT_TWO_PI = 0.3989422804014326779399460599343818684758586311649;
    public static final double SQRT_TWO_PI = 2.506628274631000502415765284811045253006986740610;

    public static final double TWO_PI = 6.283185307179586476925286766559005768394338798750;
    public static final double SQRT_PI_OVER_TWO = 1.253314137315500251207882642405522626503493370305;
    public static final double SQRT_THREE = 1.732050807568877293527446341505872366942805253810;
    public static final double SQRT_ONE_OVER_THREE = 0.577350269189625764509148780501957455647601751270;
    public static final double TWO_PI_OVER_SQRT_TWENTY_SEVEN = 1.209199576156145233729385505094770488189377498728;
    public static final double PI_OVER_SIX = 0.523598775598298873077107230546583814032861566563;

    // Algorithm-specific thresholds
    public static final int IMPLIED_VOLATILITY_MAXIMUM_ITERATIONS = 2;
    public static final double ASYMPTOTIC_EXPANSION_ACCURACY_THRESHOLD = -10.0;
    public static final double SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD = 2.0 * SIXTEENTH_ROOT_DBL_EPSILON;

    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}
