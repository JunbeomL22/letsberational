package com.berational;

import org.apache.commons.math3.util.FastMath;
/**
 * Implementation of W. J. Cody's error function algorithm.
 * Based on "Rational Chebyshev approximations for the error function"
 * (Math. Comp., 1969, PP. 631-638)
 *
 * Provides three related functions:
 * - erf(x)   - Error function
 * - erfc(x)  - Complementary error function
 * - erfcx(x) - Scaled complementary error function: exp(x²) × erfc(x)
 */
public class CodyErf {

    // Machine-dependent constants for IEEE 754 double precision
    private static final double XINF = 1.79e308;      // Largest positive finite float
    private static final double XNEG = -26.628;       // Largest negative argument for erfcx
    private static final double XSMALL = 1.11e-16;    // Threshold for erf(x) ≈ 2x/√π
    private static final double XBIG = 26.543;        // Largest argument for erfc
    private static final double XHUGE = 6.71e7;       // Threshold for 1 - 1/(2x²) = 1
    private static final double XMAX = 2.53e307;      // Largest acceptable argument for erfcx

    private static final double SQRPI = 0.56418958354775628695;  // 1/√π
    private static final double THRESHOLD = 0.46875;              // 15/32

    // Coefficients for Region 1: |x| <= 0.46875
    // P(y) and Q(y) for erf(x) = x * P(x²) / Q(x²)
    private static final double[] A = {
        3.16112374387056560,
        113.864154151050156,
        377.485237685302021,
        3209.37758913846947,
        0.185777706184603153
    };

    private static final double[] B = {
        23.6012909523441209,
        244.024637934444173,
        1282.61652607737228,
        2844.23683343917062
    };

    // Coefficients for Region 2: 0.46875 < |x| <= 4.0
    // erfc(x) = exp(-x²) × R(x)
    private static final double[] C = {
        0.564188496988670089,
        8.88314979438837594,
        66.1191906371416295,
        298.635138197400131,
        881.95222124176909,
        1712.04761263407058,
        2051.07837782607147,
        1230.33935479799725,
        2.15311535474403846e-8
    };

    private static final double[] D = {
        15.7449261107098347,
        117.693950891312499,
        537.181101862009858,
        1621.38957456669019,
        3290.79923573345963,
        4362.61909014324716,
        3439.36767414372164,
        1230.33935480374942
    };

    // Coefficients for Region 3: |x| > 4.0
    // R(x) = (1/x²) × P(1/x²) / Q(1/x²)
    private static final double[] P = {
        0.305326634961232344,
        0.360344899949804439,
        0.125781726111229246,
        0.0160837851487422766,
        6.58749161529837803e-4,
        0.0163153871373020978
    };

    private static final double[] Q = {
        2.56852019228982242,
        1.87295284992346047,
        0.527905102951428412,
        0.0605183413124413191,
        0.00233520497626869185
    };

    /**
     * Core computation function for error functions.
     *
     * @param x the argument
     * @param jint function selector: 0 for erf, 1 for erfc, 2 for erfcx
     * @return computed result
     */
    private static double calerf(double x, int jint) {
        double y = Math.abs(x);
        double result;

        // Region 1: |x| <= 0.46875
        if (y <= THRESHOLD) {
            double ysq = 0.0;
            if (y > XSMALL) {
                ysq = y * y;
            }

            double xnum = A[4] * ysq;
            double xden = ysq;

            for (int i = 0; i < 3; i++) {
                xnum = (xnum + A[i]) * ysq;
                xden = (xden + B[i]) * ysq;
            }

            result = x * (xnum + A[3]) / (xden + B[3]);

            if (jint != 0) {
                result = 1.0 - result;
            }
            if (jint == 2) {
                result = FastMath.exp(ysq) * result;
            }
            return result;
        }

        // Region 2: 0.46875 < |x| <= 4.0
        if (y <= 4.0) {
            double xnum = C[8] * y;
            double xden = y;

            for (int i = 0; i < 7; i++) {
                xnum = (xnum + C[i]) * y;
                xden = (xden + D[i]) * y;
            }

            result = (xnum + C[7]) / (xden + D[7]);

            if (jint != 2) {
                // Compute exp(-x²) carefully to avoid overflow
                double ysq = Math.floor(y * 16.0) / 16.0;
                double del = (y - ysq) * (y + ysq);
                result = FastMath.exp(-ysq * ysq) * FastMath.exp(-del) * result;
            }
        }
        // Region 3: |x| > 4.0
        else {
            result = 0.0;

            if (y >= XBIG) {
                if (jint != 2 || y >= XMAX) {
                    // Handle edge cases
                    if (jint != 2) {
                        result = 0.0;
                    } else if (y >= XMAX) {
                        result = 0.0;
                    } else {
                        result = SQRPI / y;
                    }
                } else {
                    result = SQRPI / y;
                }
            } else {
                // For very large x, use simplified asymptotic formula
                if (y < XHUGE) {
                    double ysq = 1.0 / (y * y);
                    double xnum = P[5] * ysq;
                    double xden = ysq;

                    for (int i = 0; i < 4; i++) {
                        xnum = (xnum + P[i]) * ysq;
                        xden = (xden + Q[i]) * ysq;
                    }

                    result = ysq * (xnum + P[4]) / (xden + Q[4]);
                    result = (SQRPI - result) / y;
                } else {
                    // When x >= XHUGE, 1/(2x²) underflows, so use asymptotic approximation
                    // erfcx(x) ≈ 1/(x√π) and erfc(x) ≈ exp(-x²)/(x√π)
                    result = SQRPI / y;
                }

                if (jint != 2) {
                    // Compute exp(-x²) carefully
                    double ysq2 = Math.floor(y * 16.0) / 16.0;
                    double del = (y - ysq2) * (y + ysq2);
                    result = FastMath.exp(-ysq2 * ysq2) * FastMath.exp(-del) * result;
                }
            }
        }

        // For regions 2 and 3, we computed erfc. If erf is requested, convert it.
        if (jint == 0) {
            result = 1.0 - result;
        }

        // Handle negative arguments
        if (jint == 0) {
            // erf(-x) = -erf(x)
            result = (x < 0) ? -result : result;
        } else if (jint == 1) {
            // erfc(-x) = 2 - erfc(x)
            if (x < 0) {
                result = 2.0 - result;
            }
        } else {
            // erfcx(-x) = 2*exp(x²) - erfcx(x)
            if (x < 0) {
                if (x < XNEG) {
                    result = XINF;
                } else {
                    double ysq = Math.floor(x * 16.0) / 16.0;
                    double del = (x - ysq) * (x + ysq);
                    y = FastMath.exp(ysq * ysq) * FastMath.exp(del);
                    result = (y + y) - result;
                }
            }
        }

        return result;
    }

    /**
     * Error function: erf(x) = (2/√π) ∫₀ˣ exp(-t²) dt
     *
     * @param x the argument
     * @return erf(x)
     */
    public static double erf(double x) {
        return calerf(x, 0);
    }

    /**
     * Complementary error function: erfc(x) = 1 - erf(x)
     *
     * @param x the argument
     * @return erfc(x)
     */
    public static double erfc(double x) {
        return calerf(x, 1);
    }

    /**
     * Scaled complementary error function: erfcx(x) = exp(x²) × erfc(x)
     *
     * @param x the argument
     * @return erfcx(x)
     */
    public static double erfcx(double x) {
        return calerf(x, 2);
    }
}
