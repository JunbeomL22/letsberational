package com.berational;

import static com.berational.Constants.*;

/**
 * Rational cubic interpolation utilities.
 *
 * Based on "Shape preserving piecewise rational interpolation"
 * by R. Delbourgo and J.A. Gregory, SIAM 1985.
 *
 * Used for computing initial guesses in the Let's Be Rational algorithm.
 */
public class RationalCubic {

    public static final double MINIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE = -(1.0 - Math.sqrt(DBL_EPSILON));
    public static final double MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE = 2.0 / (DBL_EPSILON * DBL_EPSILON);

    /**
     * Check if a value is effectively zero (below minimum normal).
     */
    private static boolean isZero(double x) {
        return Math.abs(x) < DBL_MIN;
    }

    /**
     * Compute rational cubic control parameter to fit second derivative at left side.
     *
     * @param xL left x coordinate
     * @param xR right x coordinate
     * @param yL left y value
     * @param yR right y value
     * @param dL left derivative
     * @param dR right derivative
     * @param secondDerivativeL second derivative at left side
     * @return control parameter r
     */
    public static double rationalCubicControlParameterToFitSecondDerivativeAtLeftSide(
            double xL, double xR, double yL, double yR,
            double dL, double dR, double secondDerivativeL) {

        double h = xR - xL;
        double numerator = 0.5 * h * secondDerivativeL + (dR - dL);

        if (isZero(numerator)) {
            return 0.0;
        }

        double denominator = (yR - yL) / h - dL;

        if (isZero(denominator)) {
            return numerator > 0 ? MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE
                                 : MINIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE;
        }

        return numerator / denominator;
    }

    /**
     * Compute rational cubic control parameter to fit second derivative at right side.
     *
     * @param xL left x coordinate
     * @param xR right x coordinate
     * @param yL left y value
     * @param yR right y value
     * @param dL left derivative
     * @param dR right derivative
     * @param secondDerivativeR second derivative at right side
     * @return control parameter r
     */
    public static double rationalCubicControlParameterToFitSecondDerivativeAtRightSide(
            double xL, double xR, double yL, double yR,
            double dL, double dR, double secondDerivativeR) {

        double h = xR - xL;
        double numerator = 0.5 * h * secondDerivativeR + (dR - dL);

        if (isZero(numerator)) {
            return 0.0;
        }

        double denominator = dR - (yR - yL) / h;

        if (isZero(denominator)) {
            return numerator > 0 ? MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE
                                 : MINIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE;
        }

        return numerator / denominator;
    }

    /**
     * Compute minimum rational cubic control parameter for shape preservation.
     *
     * @param dL left derivative
     * @param dR right derivative
     * @param s slope (yR - yL) / (xR - xL)
     * @param preferShapePreservationOverSmoothness shape preservation preference
     * @return minimum control parameter
     */
    public static double minimumRationalCubicControlParameter(
            double dL, double dR, double s,
            boolean preferShapePreservationOverSmoothness) {

        boolean monotonic = dL * s >= 0 && dR * s >= 0;
        boolean convex = dL <= s && s <= dR;
        boolean concave = dL >= s && s >= dR;

        if (!monotonic && !convex && !concave) {
            // Revert to standard cubic
            return MINIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE;
        }

        double dRmDL = dR - dL;
        double dRmS = dR - s;
        double smDL = s - dL;

        double r1 = -DBL_MAX;
        double r2 = -DBL_MAX;

        // Monotonicity condition (3.8)
        if (monotonic) {
            if (!isZero(s)) {
                r1 = (dR + dL) / s;
            } else if (preferShapePreservationOverSmoothness) {
                r1 = MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE;
            }
        }

        // Convexity/concavity condition (3.18)
        if (convex || concave) {
            if (!(isZero(smDL) || isZero(dRmS))) {
                r2 = Math.max(Math.abs(dRmDL / dRmS), Math.abs(dRmDL / smDL));
            } else if (preferShapePreservationOverSmoothness) {
                r2 = MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE;
            }
        } else if (monotonic && preferShapePreservationOverSmoothness) {
            r2 = MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE;
        }

        return Math.max(MINIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE, Math.max(r1, r2));
    }

    /**
     * Convex rational cubic control parameter to fit second derivative at left side.
     *
     * @param xL left x coordinate
     * @param xR right x coordinate
     * @param yL left y value
     * @param yR right y value
     * @param dL left derivative
     * @param dR right derivative
     * @param secondDerivativeL second derivative at left side
     * @param preferShapePreservationOverSmoothness shape preservation preference
     * @return control parameter r
     */
    public static double convexRationalCubicControlParameterToFitSecondDerivativeAtLeftSide(
            double xL, double xR, double yL, double yR,
            double dL, double dR, double secondDerivativeL,
            boolean preferShapePreservationOverSmoothness) {

        double r = rationalCubicControlParameterToFitSecondDerivativeAtLeftSide(
                xL, xR, yL, yR, dL, dR, secondDerivativeL);

        double rMin = minimumRationalCubicControlParameter(
                dL, dR, (yR - yL) / (xR - xL), preferShapePreservationOverSmoothness);

        return Math.max(r, rMin);
    }

    /**
     * Convex rational cubic control parameter to fit second derivative at right side.
     *
     * @param xL left x coordinate
     * @param xR right x coordinate
     * @param yL left y value
     * @param yR right y value
     * @param dL left derivative
     * @param dR right derivative
     * @param secondDerivativeR second derivative at right side
     * @param preferShapePreservationOverSmoothness shape preservation preference
     * @return control parameter r
     */
    public static double convexRationalCubicControlParameterToFitSecondDerivativeAtRightSide(
            double xL, double xR, double yL, double yR,
            double dL, double dR, double secondDerivativeR,
            boolean preferShapePreservationOverSmoothness) {

        double r = rationalCubicControlParameterToFitSecondDerivativeAtRightSide(
                xL, xR, yL, yR, dL, dR, secondDerivativeR);

        double rMin = minimumRationalCubicControlParameter(
                dL, dR, (yR - yL) / (xR - xL), preferShapePreservationOverSmoothness);

        return Math.max(r, rMin);
    }

    /**
     * Rational cubic interpolation.
     *
     * Interpolates the value at x using rational cubic spline defined by:
     * - Left point (xL, yL) with derivative dL
     * - Right point (xR, yR) with derivative dR
     * - Control parameter r
     *
     * @param x evaluation point
     * @param xL left x coordinate
     * @param xR right x coordinate
     * @param yL left y value
     * @param yR right y value
     * @param dL left derivative
     * @param dR right derivative
     * @param r control parameter
     * @return interpolated value at x
     */
    public static double rationalCubicInterpolation(
            double x, double xL, double xR, double yL, double yR,
            double dL, double dR, double r) {

        double h = xR - xL;

        if (Math.abs(h) <= 0) {
            return 0.5 * (yL + yR);
        }

        // Linear interpolation for very large r
        if (!(r < MAXIMUM_RATIONAL_CUBIC_CONTROL_PARAMETER_VALUE)) {
            double t = (x - xL) / h;
            return yR * t + yL * (1.0 - t);
        }

        // Rational cubic formula (2.4) / (2.5)
        double t = (x - xL) / h;
        double omt = 1.0 - t;
        double t2 = t * t;
        double omt2 = omt * omt;

        return (yR * t2 * t + (r * yR - h * dR) * t2 * omt +
                (r * yL + h * dL) * t * omt2 + yL * omt2 * omt) /
               (1.0 + (r - 3.0) * t * omt);
    }

    // Prevent instantiation
    private RationalCubic() {
        throw new AssertionError("RationalCubic class should not be instantiated");
    }
}
