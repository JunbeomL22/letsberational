package com.berational;

import org.apache.commons.math3.util.FastMath;

/**
 * Normal (Gaussian) distribution implementation using Cody's error function.
 *
 * Provides cumulative distribution function (CDF) for the standard normal distribution.
 */
public class CodyNormDist {

    private static final double SQRT2 = 1.4142135623730951;  // √2

    /**
     * Cumulative distribution function of standard normal distribution.
     * Uses erf for x ≥ 0 and erfc for x < 0 to maintain maximum accuracy.
     *
     * Φ(x) = (1/2)[1 + erf(x/√2)] for x ≥ 0
     * Φ(x) = (1/2) × erfc(-x/√2) for x < 0
     *
     * @param x the argument
     * @return Φ(x), the probability that a standard normal random variable is ≤ x
     */
    public static double cdf(double x) {
        double result;
        if (x >= 0) {
            // For non-negative x, use erf
            result = 0.5 * (1.0 + CodyErf.erf(x / SQRT2));
        } else {
            // For negative x, use erfc to avoid cancellation
            result = 0.5 * CodyErf.erfc(-x / SQRT2);
        }
        // Clamp to [0, 1] to handle floating point edge cases
        return Math.max(0.0, Math.min(1.0, result));
    }

    /**
     * Cumulative distribution function using only erf.
     * Less accurate for negative x values due to cancellation.
     *
     * @param x the argument
     * @return Φ(x)
     */
    public static double cdfErf(double x) {
        return 0.5 * (1.0 + CodyErf.erf(x / SQRT2));
    }

    /**
     * Cumulative distribution function using only erfc.
     * More accurate for negative x values.
     *
     * @param x the argument
     * @return Φ(x)
     */
    public static double cdfErfc(double x) {
        return 0.5 * CodyErf.erfc(-x / SQRT2);
    }

    /**
     * Probability density function of standard normal distribution.
     *
     * φ(x) = (1/√(2π)) × exp(-x²/2)
     *
     * @param x the argument
     * @return φ(x)
     */
    public static double pdf(double x) {
        return FastMath.exp(-0.5 * x * x) / FastMath.sqrt(2.0 * FastMath.PI);
    }
}
