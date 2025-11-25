package com.berational;

import static com.berational.Constants.*;
import static com.berational.RationalCubic.*;
import static com.berational.NormalDistribution.*;
import static com.berational.CodyErf.erfcx;

/**
 * Let's Be Rational - Black Implied Volatility Algorithm
 *
 * Implementation of Peter Jäckel's algorithm for computing Black's implied volatility
 * from option prices. Achieves machine precision in exactly 2 iterations for all inputs.
 *
 * Key features:
 * - Convergence: Two iterations to machine precision for all inputs
 * - Method: Fourth-order Householder iteration (third-order method)
 * - Initial Guess: Four rational function branches adapted to log-moneyness
 *
 * Reference: www.jaeckel.org/LetsBeRational.7z
 *
 * @author Based on Peter Jäckel's original implementation
 */
public class LetsBeRational {

    /**
     * Check if value is below denormalization horizon (effectively zero).
     */
    private static boolean isBelowHorizon(double x) {
        return Math.abs(x) < DENORMALIZATION_CUTOFF;
    }

    /**
     * Square a number.
     */
    private static double square(double x) {
        return x * x;
    }

    /**
     * Compute normalized intrinsic value.
     *
     * For out-of-the-money options (q*x <= 0), returns 0.
     * For in-the-money options, returns normalized intrinsic value.
     *
     * @param x log-moneyness ln(F/K)
     * @param q +1 for call, -1 for put
     * @return normalized intrinsic value
     */
    private static double normalisedIntrinsic(double x, int q) {
        if (q * x <= 0) {
            return 0.0;
        }

        double x2 = x * x;

        // Use Taylor expansion for small x to avoid numerical issues
        if (x2 < 98.0 * FOURTH_ROOT_DBL_EPSILON) {
            // Taylor series: exp(x/2) - exp(-x/2) ≈ x(1 + x²/24 + x⁴/1920 + ...)
            return Math.abs(Math.max(
                (q < 0 ? -1.0 : 1.0) * x * (1.0 + x2 * ((1.0 / 24.0) + x2 * ((1.0 / 1920.0) +
                    x2 * ((1.0 / 322560.0) + (1.0 / 92897280.0) * x2)))),
                0.0));
        }

        double bMax = Math.exp(0.5 * x);
        double oneOverBMax = 1.0 / bMax;
        return Math.abs(Math.max((q < 0 ? -1.0 : 1.0) * (bMax - oneOverBMax), 0.0));
    }

    /**
     * Asymptotic expansion of normalized Black call for large negative (t - |h|).
     *
     * Valid when h is large negative and t is small relative to |h|.
     * Region I in the algorithm: |h| > 10 AND t < |h| - 10 + 0.21
     *
     * @param h x/σ
     * @param t σ/2
     * @return normalized Black call value
     */
    private static double asymptoticExpansionOfNormalizedBlackCall(double h, double t) {
        double e = (t / h) * (t / h);
        double r = (h + t) * (h - t);
        double q = (h / r) * (h / r);

        // 17th order asymptotic expansion sufficient for relative accuracy of 1.64E-16
        // Computed as nested polynomial in q (iteration index squared)
        double sum = 2.0;
        sum += q * (-6.0E0 - 2.0 * e);
        sum += q * q * 3.0 * (1.0E1 + e * (2.0E1 + 2.0 * e));
        sum += q * q * q * 5.0 * (-1.4E1 + e * (-7.0E1 + e * (-4.2E1 - 2.0 * e)));
        sum += q * q * q * q * 7.0 * (1.8E1 + e * (1.68E2 + e * (2.52E2 + e * (7.2E1 + 2.0 * e))));
        sum += q * q * q * q * q * 9.0 * (-2.2E1 + e * (-3.3E2 + e * (-9.24E2 + e * (-6.6E2 + e * (-1.1E2 - 2.0 * e)))));
        sum += Math.pow(q, 6) * 1.1E1 * (2.6E1 + e * (5.72E2 + e * (2.574E3 + e * (3.432E3 + e * (1.43E3 + e * (1.56E2 + 2.0 * e))))));
        sum += Math.pow(q, 7) * 1.3E1 * (-3.0E1 + e * (-9.1E2 + e * (-6.006E3 + e * (-1.287E4 + e * (-1.001E4 + e * (-2.73E3 + e * (-2.1E2 - 2.0 * e)))))));
        sum += Math.pow(q, 8) * 1.5E1 * (3.4E1 + e * (1.36E3 + e * (1.2376E4 + e * (3.8896E4 + e * (4.862E4 + e * (2.4752E4 + e * (4.76E3 + e * (2.72E2 + 2.0 * e))))))));
        sum += Math.pow(q, 9) * 1.7E1 * (-3.8E1 + e * (-1.938E3 + e * (-2.3256E4 + e * (-1.00776E5 + e * (-1.84756E5 + e * (-1.51164E5 + e * (-5.4264E4 + e * (-7.752E3 + e * (-3.42E2 - 2.0 * e)))))))));
        sum += Math.pow(q, 10) * 1.9E1 * (4.2E1 + e * (2.66E3 + e * (4.0698E4 + e * (2.3256E5 + e * (5.8786E5 + e * (7.05432E5 + e * (4.0698E5 + e * (1.08528E5 + e * (1.197E4 + e * (4.2E2 + 2.0 * e))))))))));
        sum += Math.pow(q, 11) * 2.1E1 * (-4.6E1 + e * (-3.542E3 + e * (-6.7298E4 + e * (-4.90314E5 + e * (-1.63438E6 + e * (-2.704156E6 + e * (-2.288132E6 + e * (-9.80628E5 + e * (-2.01894E5 + e * (-1.771E4 + e * (-5.06E2 - 2.0 * e)))))))))));
        sum += Math.pow(q, 12) * 2.3E1 * (5.0E1 + e * (4.6E3 + e * (1.0626E5 + e * (9.614E5 + e * (4.08595E6 + e * (8.9148E6 + e * (1.04006E7 + e * (6.53752E6 + e * (2.16315E6 + e * (3.542E5 + e * (2.53E4 + e * (6.0E2 + 2.0 * e))))))))))));
        sum += Math.pow(q, 13) * 2.5E1 * (-5.4E1 + e * (-5.85E3 + e * (-1.6146E5 + e * (-1.77606E6 + e * (-9.37365E6 + e * (-2.607579E7 + e * (-4.01166E7 + e * (-3.476772E7 + e * (-1.687257E7 + e * (-4.44015E6 + e * (-5.9202E5 + e * (-3.51E4 + e * (-7.02E2 - 2.0 * e)))))))))))));
        sum += Math.pow(q, 14) * 2.7E1 * (5.8E1 + e * (7.308E3 + e * (2.3751E5 + e * (3.12156E6 + e * (2.003001E7 + e * (6.919458E7 + e * (1.3572783E8 + e * (1.5511752E8 + e * (1.0379187E8 + e * (4.006002E7 + e * (8.58429E6 + e * (9.5004E5 + e * (4.7502E4 + e * (8.12E2 + 2.0 * e))))))))))))));
        sum += Math.pow(q, 15) * 2.9E1 * (-6.2E1 + e * (-8.99E3 + e * (-3.39822E5 + e * (-5.25915E6 + e * (-4.032015E7 + e * (-1.6934463E8 + e * (-4.1250615E8 + e * (-6.0108039E8 + e * (-5.3036505E8 + e * (-2.8224105E8 + e * (-8.870433E7 + e * (-1.577745E7 + e * (-1.472562E6 + e * (-6.293E4 + e * (-9.3E2 - 2.0 * e)))))))))))))));
        sum += Math.pow(q, 16) * 3.1E1 * (6.6E1 + e * (1.0912E4 + e * (4.74672E5 + e * (8.544096E6 + e * (7.71342E7 + e * (3.8707344E8 + e * (1.14633288E9 + e * (2.07431664E9 + e * (2.33360622E9 + e * (1.6376184E9 + e * (7.0963464E8 + e * (1.8512208E8 + e * (2.7768312E7 + e * (2.215136E6 + e * (8.184E4 + e * (1.056E3 + 2.0 * e))))))))))))))));
        sum += Math.pow(q, 17) * 3.3E1 * (-7.0E1 + e * (-1.309E4 + e * (-6.49264E5 + e * (-1.344904E7 + e * (-1.4121492E8 + e * (-8.344518E8 + e * (-2.9526756E9 + e * (-6.49588632E9 + e * (-9.0751353E9 + e * (-8.1198579E9 + e * (-4.6399188E9 + e * (-1.6689036E9 + e * (-3.67158792E8 + e * (-4.707164E7 + e * (-3.24632E6 + e * (-1.0472E5 + e * (-1.19E3 - 2.0 * e)))))))))))))))));

        double asymptoticExpansionSum = sum;

        double b = ONE_OVER_SQRT_TWO_PI * Math.exp(-0.5 * (h * h + t * t)) * (t / r) * asymptoticExpansionSum;
        return Math.abs(Math.max(b, 0.0));
    }

    /**
     * Small t expansion of normalized Black call.
     *
     * Valid for small t (σ/2) regardless of h.
     * Region II: t < 0.21
     *
     * Uses 12th order Taylor expansion in t of [Y(h+t) - Y(h-t)]
     * where Y(z) = Φ(z)/φ(z)
     *
     * @param h x/σ
     * @param t σ/2
     * @return normalized Black call value
     */
    private static double smallTExpansionOfNormalizedBlackCall(double h, double t) {
        // Y(h) = Φ(h)/φ(h) = √(π/2)·erfcx(-h/√2)
        // a = 1 + h·Y(h)
        double a = 1.0 + h * (0.5 * SQRT_TWO_PI) * erfcx(-ONE_OVER_SQRT_TWO * h);
        double w = t * t;
        double h2 = h * h;

        double expansion = 2.0 * t * (a + w * ((-1.0 + 3.0 * a + a * h2) / 6.0 + w * ((-7.0 + 15.0 * a + h2 * (-1.0 + 10.0 * a + a * h2)) / 120.0 + w * ((-57.0 + 105.0 * a + h2 * (-18.0 + 105.0 * a + h2 * (-1.0 + 21.0 * a + a * h2))) / 5040.0 + w * ((-561.0 + 945.0 * a + h2 * (-285.0 + 1260.0 * a + h2 * (-33.0 + 378.0 * a + h2 * (-1.0 + 36.0 * a + a * h2)))) / 362880.0 + w * ((-6555.0 + 10395.0 * a + h2 * (-4680.0 + 17325.0 * a + h2 * (-840.0 + 6930.0 * a + h2 * (-52.0 + 990.0 * a + h2 * (-1.0 + 55.0 * a + a * h2))))) / 39916800.0 + ((-89055.0 + 135135.0 * a + h2 * (-82845.0 + 270270.0 * a + h2 * (-20370.0 + 135135.0 * a + h2 * (-1926.0 + 25740.0 * a + h2 * (-75.0 + 2145.0 * a + h2 * (-1.0 + 78.0 * a + a * h2)))))) * w) / 6227020800.0))))));

        double b = ONE_OVER_SQRT_TWO_PI * Math.exp(-0.5 * (h * h + t * t)) * expansion;
        return Math.abs(Math.max(b, 0.0));
    }

    /**
     * Normalized Black call using direct CDF evaluation.
     *
     * Region III: Large t where b ≈ 0.85·b_max
     * Uses: b(x,s) = Φ(h+t)·exp(ht) - Φ(h-t)·exp(-ht)
     *
     * @param x log-moneyness ln(F/K)
     * @param s σ√T
     * @return normalized Black call value
     */
    private static double normalizedBlackCallUsingNormCdf(double x, double s) {
        double h = x / s;
        double t = 0.5 * s;
        double bMax = Math.exp(0.5 * x);
        double b = cdf(h + t) * bMax - cdf(h - t) / bMax;
        return Math.abs(Math.max(b, 0.0));
    }

    /**
     * Normalized Black call using erfcx (scaled complementary error function).
     *
     * Region IV: Everywhere else
     * Uses: b = ½·exp(-½(h²+t²))·[erfcx(-(h+t)/√2) - erfcx(-(h-t)/√2)]
     *
     * This formulation minimizes exponential evaluations and provides
     * better accuracy than direct CDF evaluation for moderate h and t.
     *
     * @param h x/σ
     * @param t σ/2
     * @return normalized Black call value
     */
    private static double normalizedBlackCallUsingErfcx(double h, double t) {
        double b = 0.5 * Math.exp(-0.5 * (h * h + t * t)) *
                   (erfcx(-ONE_OVER_SQRT_TWO * (h + t)) - erfcx(-ONE_OVER_SQRT_TWO * (h - t)));
        return Math.abs(Math.max(b, 0.0));
    }

    /**
     * Compute normalized Black call value.
     *
     * Automatically selects the most accurate evaluation method based on
     * the region defined by x and s.
     *
     * @param x log-moneyness ln(F/K)
     * @param s σ√T (normalized volatility)
     * @return normalized Black call value
     */
    public static double normalisedBlackCall(double x, double s) {
        // Use put-call symmetry for positive x
        if (x > 0) {
            return normalisedIntrinsic(x, 1) + normalisedBlackCall(-x, s);
        }

        double ax = Math.abs(x);

        // If σ is too small, return intrinsic value
        if (s <= ax * DENORMALIZATION_CUTOFF) {
            return normalisedIntrinsic(x, 1);
        }

        double h = x / s;
        double t = 0.5 * s;

        // Region I: Asymptotic expansion for large negative h and small t
        if (x < s * ASYMPTOTIC_EXPANSION_ACCURACY_THRESHOLD &&
            0.5 * s * s + x < s * (SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD +
                                   ASYMPTOTIC_EXPANSION_ACCURACY_THRESHOLD)) {
            return asymptoticExpansionOfNormalizedBlackCall(h, t);
        }

        // Region II: Small t expansion
        if (t < SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD) {
            return smallTExpansionOfNormalizedBlackCall(h, t);
        }

        // Region III: Large t where b is dominated by first term
        if (x + 0.5 * s * s > s * 0.85) {
            return normalizedBlackCallUsingNormCdf(x, s);
        }

        // Region IV: Use erfcx formulation
        return normalizedBlackCallUsingErfcx(h, t);
    }

    /**
     * Compute normalized vega.
     *
     * vega = ∂b/∂s = (1/√(2π))·exp(-½((x/s)² + (s/2)²))
     *
     * @param x log-moneyness ln(F/K)
     * @param s σ√T
     * @return normalized vega
     */
    public static double normalisedVega(double x, double s) {
        double ax = Math.abs(x);

        if (ax <= 0) {
            return ONE_OVER_SQRT_TWO_PI * Math.exp(-0.125 * s * s);
        } else {
            if (s <= 0 || s <= ax * SQRT_DBL_MIN) {
                return 0.0;
            }
            return ONE_OVER_SQRT_TWO_PI * Math.exp(-0.5 * (square(x / s) + square(0.5 * s)));
        }
    }

    /**
     * Compute f_lower_map and its first two derivatives.
     *
     * Used for the lower branch of the initial guess.
     * Maps normalized price β to an intermediate value via non-linear transformation.
     *
     * @param x log-moneyness
     * @param s σ√T
     * @return array [f, f', f'']
     */
    private static double[] computeFLowerMapAndFirstTwoDerivatives(double x, double s) {
        double ax = Math.abs(x);
        double z = SQRT_ONE_OVER_THREE * ax / s;
        double y = z * z;
        double s2 = s * s;
        double Phi = cdf(-z);
        double phi = pdf(z);

        double fpp = PI_OVER_SIX * y / (s2 * s) * Phi *
                     (8.0 * SQRT_THREE * s * ax + (3.0 * s2 * (s2 - 8.0) - 8.0 * x * x) * Phi / phi) *
                     Math.exp(2.0 * y + 0.25 * s2);

        double fp, f;
        if (isBelowHorizon(s)) {
            fp = 1.0;
            f = 0.0;
        } else {
            double Phi2 = Phi * Phi;
            fp = TWO_PI * y * Phi2 * Math.exp(y + 0.125 * s * s);
            if (isBelowHorizon(x)) {
                f = 0.0;
            } else {
                f = TWO_PI_OVER_SQRT_TWENTY_SEVEN * ax * (Phi2 * Phi);
            }
        }

        return new double[]{f, fp, fpp};
    }

    /**
     * Compute f_upper_map and its first two derivatives.
     *
     * Used for the upper branch of the initial guess.
     *
     * @param x log-moneyness
     * @param s σ√T
     * @return array [f, f', f'']
     */
    private static double[] computeFUpperMapAndFirstTwoDerivatives(double x, double s) {
        double f = cdf(-0.5 * s);
        double fp, fpp;

        if (isBelowHorizon(x)) {
            fp = -0.5;
            fpp = 0.0;
        } else {
            double w = square(x / s);
            fp = -0.5 * Math.exp(0.5 * w);
            fpp = SQRT_PI_OVER_TWO * Math.exp(w + 0.125 * s * s) * w / s;
        }

        return new double[]{f, fp, fpp};
    }

    /**
     * Inverse of f_lower_map.
     *
     * @param x log-moneyness
     * @param f intermediate value
     * @return σ√T
     */
    private static double inverseFLowerMap(double x, double f) {
        if (isBelowHorizon(f)) {
            return 0.0;
        }
        return Math.abs(x / (SQRT_THREE * inverseCdf(Math.pow(f / (TWO_PI_OVER_SQRT_TWENTY_SEVEN * Math.abs(x)), 1.0 / 3.0))));
    }

    /**
     * Inverse of f_upper_map.
     *
     * @param f intermediate value
     * @return σ√T
     */
    private static double inverseFUpperMap(double f) {
        return -2.0 * inverseCdf(f);
    }

    /**
     * Householder factor for third-order method.
     *
     * @param newton Newton step: -g/g'
     * @param halley Halley correction: g''/g'
     * @param hh3 Third derivative term: g'''/g'
     * @return Householder correction factor
     */
    private static double householderFactor(double newton, double halley, double hh3) {
        return (1.0 + 0.5 * halley * newton) / (1.0 + newton * (halley + hh3 * newton / 6.0));
    }

    /**
     * Core algorithm: unchecked normalized implied volatility computation.
     *
     * Computes implied volatility from normalized price β using:
     * 1. Four-branch initial guess
     * 2. Three-branch objective function
     * 3. Two iterations of Householder(3) method
     *
     * @param beta normalized price
     * @param x log-moneyness ln(F/K)
     * @param q +1 for call, -1 for put
     * @param N maximum iterations (typically 2)
     * @return σ√T (normalized implied volatility)
     */
    private static double uncheckedNormalisedImpliedVolatilityFromATransformedRationalGuessWithLimitedIterations(
            double beta, double x, int q, int N) {

        // Subtract intrinsic and map to out-of-the-money
        if (q * x > 0) {
            beta = Math.abs(Math.max(beta - normalisedIntrinsic(x, q), 0.0));
            q = -q;
        }

        // Map puts to calls
        if (q < 0) {
            x = -x;
            q = -q;
        }

        // Handle edge cases
        if (beta <= 0) {
            return 0.0;
        }
        if (beta < DENORMALIZATION_CUTOFF) {
            return 0.0;
        }

        double bMax = Math.exp(0.5 * x);
        if (beta >= bMax) {
            throw new AboveMaximumException();
        }

        // Compute inflection point
        double sC = Math.sqrt(Math.abs(2.0 * x));
        double bC = normalisedBlackCall(x, sC);
        double vC = normalisedVega(x, sC);

        // Initial guess and iteration setup
        int iterations = 0;
        int directionReversalCount = 0;
        double s = -DBL_MAX;
        double ds = 0.0;
        double dsPrevious = 0.0;
        double sLeft = DBL_MIN;
        double sRight = DBL_MAX;

        // Four branches for initial guess
        if (beta < bC) {
            // Lower half: branches 1 and 2
            double sL = sC - bC / vC;
            double bL = normalisedBlackCall(x, sL);

            if (beta < bL) {
                // Branch 1: Very low prices
                double[] fLower = computeFLowerMapAndFirstTwoDerivatives(x, sL);
                double fLowerMapL = fLower[0];
                double dFLowerMapLdBeta = fLower[1];
                double d2FLowerMapLdBeta2 = fLower[2];

                double rLL = convexRationalCubicControlParameterToFitSecondDerivativeAtRightSide(
                        0.0, bL, 0.0, fLowerMapL, 1.0, dFLowerMapLdBeta, d2FLowerMapLdBeta2, true);

                double f = rationalCubicInterpolation(beta, 0.0, bL, 0.0, fLowerMapL,
                                                      1.0, dFLowerMapLdBeta, rLL);

                if (!(f > 0)) {
                    // Fallback to quadratic
                    double t = beta / bL;
                    f = (fLowerMapL * t + bL * (1.0 - t)) * t;
                }

                s = inverseFLowerMap(x, f);
                sRight = sL;
                ds = s;  // Ensure iteration loop executes

                // Objective function: g(s) = 1/ln(b(s)) - 1/ln(β)
                while (iterations < N && Math.abs(ds) > DBL_EPSILON * s) {
                    if (ds * dsPrevious < 0) {
                        directionReversalCount++;
                    }

                    if (iterations > 0 && (directionReversalCount == 3 || !(s > sLeft && s < sRight))) {
                        // Binary nesting
                        s = 0.5 * (sLeft + sRight);
                        if (sRight - sLeft <= DBL_EPSILON * s) {
                            break;
                        }
                        directionReversalCount = 0;
                        ds = 0.0;
                    }

                    dsPrevious = ds;
                    double b = normalisedBlackCall(x, s);
                    double bp = normalisedVega(x, s);

                    if (b > beta && s < sRight) {
                        sRight = s;
                    } else if (b < beta && s > sLeft) {
                        sLeft = s;
                    }

                    if (b <= 0 || bp <= 0) {
                        // Numerical underflow
                        ds = 0.5 * (sLeft + sRight) - s;
                    } else {
                        double lnB = Math.log(b);
                        double lnBeta = Math.log(beta);
                        double bpob = bp / b;
                        double h = x / s;
                        double bHalley = h * h / s - s / 4.0;
                        double newton = (lnBeta - lnB) * lnB / lnBeta / bpob;
                        double halley = bHalley - bpob * (1.0 + 2.0 / lnB);
                        double bHh3 = bHalley * bHalley - 3.0 * square(h / s) - 0.25;
                        double hh3 = bHh3 + 2.0 * square(bpob) * (1.0 + 3.0 / lnB * (1.0 + 1.0 / lnB)) -
                                     3.0 * bHalley * bpob * (1.0 + 2.0 / lnB);
                        ds = newton * householderFactor(newton, halley, hh3);
                    }

                    ds = Math.max(-0.5 * s, ds);
                    s += ds;
                    iterations++;
                }
                return s;

            } else {
                // Branch 2: Center-left
                double vL = normalisedVega(x, sL);
                double rLM = convexRationalCubicControlParameterToFitSecondDerivativeAtRightSide(
                        bL, bC, sL, sC, 1.0 / vL, 1.0 / vC, 0.0, false);
                s = rationalCubicInterpolation(beta, bL, bC, sL, sC, 1.0 / vL, 1.0 / vC, rLM);
                sLeft = sL;
                sRight = sC;
                ds = s;  // Ensure iteration loop executes
            }
        } else {
            // Upper half: branches 3 and 4
            double sH = vC > DBL_MIN ? sC + (bMax - bC) / vC : sC;
            double bH = normalisedBlackCall(x, sH);

            if (beta <= bH) {
                // Branch 3: Center-right
                double vH = normalisedVega(x, sH);
                double rHM = convexRationalCubicControlParameterToFitSecondDerivativeAtLeftSide(
                        bC, bH, sC, sH, 1.0 / vC, 1.0 / vH, 0.0, false);
                s = rationalCubicInterpolation(beta, bC, bH, sC, sH, 1.0 / vC, 1.0 / vH, rHM);
                sLeft = sC;
                sRight = sH;
                ds = s;  // Ensure iteration loop executes
            } else {
                // Branch 4: Very high prices
                double[] fUpper = computeFUpperMapAndFirstTwoDerivatives(x, sH);
                double fUpperMapH = fUpper[0];
                double dFUpperMapHdBeta = fUpper[1];
                double d2FUpperMapHdBeta2 = fUpper[2];

                double f = 0.0;
                if (d2FUpperMapHdBeta2 > -SQRT_DBL_MAX && d2FUpperMapHdBeta2 < SQRT_DBL_MAX) {
                    double rHH = convexRationalCubicControlParameterToFitSecondDerivativeAtLeftSide(
                            bH, bMax, fUpperMapH, 0.0, dFUpperMapHdBeta, -0.5, d2FUpperMapHdBeta2, true);
                    f = rationalCubicInterpolation(beta, bH, bMax, fUpperMapH, 0.0,
                                                   dFUpperMapHdBeta, -0.5, rHH);
                }

                if (f <= 0) {
                    // Fallback to quadratic
                    double h = bMax - bH;
                    double t = (beta - bH) / h;
                    f = (fUpperMapH * (1.0 - t) + 0.5 * h * t) * (1.0 - t);
                }

                s = inverseFUpperMap(f);
                sLeft = sH;
                ds = s;  // Ensure iteration loop executes if we fall through

                if (beta > 0.5 * bMax) {
                    // Objective function: g(s) = ln((b_max-β)/(b_max-b(s)))
                    ds = s;  // Ensure iteration loop executes
                    while (iterations < N && Math.abs(ds) > DBL_EPSILON * s) {
                        if (ds * dsPrevious < 0) {
                            directionReversalCount++;
                        }

                        if (iterations > 0 && (directionReversalCount == 3 || !(s > sLeft && s < sRight))) {
                            s = 0.5 * (sLeft + sRight);
                            if (sRight - sLeft <= DBL_EPSILON * s) {
                                break;
                            }
                            directionReversalCount = 0;
                            ds = 0.0;
                        }

                        dsPrevious = ds;
                        double b = normalisedBlackCall(x, s);
                        double bp = normalisedVega(x, s);

                        if (b > beta && s < sRight) {
                            sRight = s;
                        } else if (b < beta && s > sLeft) {
                            sLeft = s;
                        }

                        if (b >= bMax || bp <= DBL_MIN) {
                            ds = 0.5 * (sLeft + sRight) - s;
                        } else {
                            double bMaxMinusB = bMax - b;
                            double g = Math.log((bMax - beta) / bMaxMinusB);
                            double gp = bp / bMaxMinusB;
                            double bHalley = square(x / s) / s - s / 4.0;
                            double bHh3 = bHalley * bHalley - 3.0 * square(x / (s * s)) - 0.25;
                            double newton = -g / gp;
                            double halley = bHalley + gp;
                            double hh3 = bHh3 + gp * (2.0 * gp + 3.0 * bHalley);
                            ds = newton * householderFactor(newton, halley, hh3);
                        }

                        ds = Math.max(-0.5 * s, ds);
                        s += ds;
                        iterations++;
                    }
                    return s;
                }
            }
        }

        // Middle branches: objective function g(s) = b(s) - β
        while (iterations < N && Math.abs(ds) > DBL_EPSILON * s) {
            if (ds * dsPrevious < 0) {
                directionReversalCount++;
            }

            if (iterations > 0 && (directionReversalCount == 3 || !(s > sLeft && s < sRight))) {
                s = 0.5 * (sLeft + sRight);
                if (sRight - sLeft <= DBL_EPSILON * s) {
                    break;
                }
                directionReversalCount = 0;
                ds = 0.0;
            }

            dsPrevious = ds;
            double b = normalisedBlackCall(x, s);
            double bp = normalisedVega(x, s);

            if (b > beta && s < sRight) {
                sRight = s;
            } else if (b < beta && s > sLeft) {
                sLeft = s;
            }

            double newton = (beta - b) / bp;
            double halley = square(x / s) / s - s / 4.0;
            double hh3 = halley * halley - 3.0 * square(x / (s * s)) - 0.25;
            ds = Math.max(-0.5 * s, newton * householderFactor(newton, halley, hh3));
            s += ds;
            iterations++;
        }

        return s;
    }

    /**
     * Compute normalized implied volatility from normalized price.
     *
     * @param beta normalized price β = price / √(F·K)
     * @param x log-moneyness ln(F/K)
     * @param q +1 for call, -1 for put
     * @return σ√T (normalized implied volatility)
     * @throws BelowIntrinsicException if price is below intrinsic value
     */
    public static double normalisedImpliedVolatilityFromATransformedRationalGuess(
            double beta, double x, int q) {

        // Map in-the-money to out-of-the-money
        if (q * x > 0) {
            beta -= normalisedIntrinsic(x, q);
            q = -q;
        }

        if (beta < 0) {
            throw new BelowIntrinsicException();
        }

        return uncheckedNormalisedImpliedVolatilityFromATransformedRationalGuessWithLimitedIterations(
                beta, x, q, IMPLIED_VOLATILITY_MAXIMUM_ITERATIONS);
    }

    /**
     * Compute Black implied volatility from option price.
     *
     * Main entry point for the algorithm.
     *
     * @param price option price
     * @param F forward price
     * @param K strike price
     * @param T time to expiration
     * @param q +1 for call, -1 for put
     * @return implied volatility σ
     * @throws BelowIntrinsicException if price is below intrinsic value
     * @throws AboveMaximumException if price exceeds maximum possible value
     */
    public static double impliedVolatilityFromATransformedRationalGuess(
            double price, double F, double K, double T, int q) {

        double intrinsic = Math.abs(Math.max(q < 0 ? K - F : F - K, 0.0));

        if (price < intrinsic) {
            throw new BelowIntrinsicException();
        }

        double maxPrice = q < 0 ? K : F;
        if (price >= maxPrice) {
            throw new AboveMaximumException();
        }

        double x = Math.log(F / K);

        // Map in-the-money to out-of-the-money
        if (q * x > 0) {
            price = Math.abs(Math.max(price - intrinsic, 0.0));
            q = -q;
        }

        return uncheckedNormalisedImpliedVolatilityFromATransformedRationalGuessWithLimitedIterations(
                price / (Math.sqrt(F) * Math.sqrt(K)), x, q, IMPLIED_VOLATILITY_MAXIMUM_ITERATIONS) / Math.sqrt(T);
    }

    // Prevent instantiation
    private LetsBeRational() {
        throw new AssertionError("LetsBeRational class should not be instantiated");
    }
}
