package com.berational;

import org.apache.commons.math3.util.FastMath;
import static com.berational.NormalDistribution.*;

/**
 * Plain vanilla Black-Scholes implied volatility using Newton-Raphson method.
 *
 * This is a traditional implementation used for benchmarking comparison
 * against the Let's Be Rational algorithm. It uses standard Newton-Raphson
 * iteration with basic convergence criteria.
 *
 * Key characteristics:
 * - Simple Newton-Raphson iteration: σ_new = σ_old - (Price - Target) / Vega
 * - Variable iteration count (typically 5-20 iterations)
 * - No sophisticated initial guess
 * - Basic convergence criteria
 *
 * @author Benchmark comparison implementation
 */
public class BlackScholesNewtonRaphson {

    /** Maximum iterations for Newton-Raphson */
    private static final int MAX_ITERATIONS = 100;

    /** Convergence tolerance for price difference */
    private static final double PRICE_TOLERANCE = 1e-8;

    /** Convergence tolerance for volatility change */
    private static final double VOL_TOLERANCE = 1e-8;

    /** Minimum vega to avoid division by zero */
    private static final double MIN_VEGA = 1e-10;

    /** Initial volatility guess */
    private static final double INITIAL_GUESS = 0.2; // 20% vol

    /**
     * Compute Black-Scholes option price.
     *
     * @param F forward price
     * @param K strike price
     * @param T time to expiration
     * @param sigma volatility
     * @param isCall true for call, false for put
     * @return option price
     */
    private static double blackScholesPrice(double F, double K, double T, double sigma, boolean isCall) {
        if (T <= 0 || sigma <= 0) {
            return Math.max(isCall ? F - K : K - F, 0.0);
        }

        double sqrtT = Math.sqrt(T);
        double d1 = (FastMath.log(F / K) + 0.5 * sigma * sigma * T) / (sigma * sqrtT);
        double d2 = d1 - sigma * sqrtT;

        if (isCall) {
            return F * cdf(d1) - K * cdf(d2);
        } else {
            return K * cdf(-d2) - F * cdf(-d1);
        }
    }

    /**
     * Compute Black-Scholes vega (∂Price/∂σ).
     *
     * @param F forward price
     * @param K strike price
     * @param T time to expiration
     * @param sigma volatility
     * @return vega
     */
    private static double blackScholesVega(double F, double K, double T, double sigma) {
        if (T <= 0 || sigma <= 0) {
            return 0.0;
        }

        double sqrtT = Math.sqrt(T);
        double d1 = (FastMath.log(F / K) + 0.5 * sigma * sigma * T) / (sigma * sqrtT);

        return F * sqrtT * pdf(d1);
    }

    /**
     * Compute implied volatility using Newton-Raphson method.
     *
     * @param price target option price
     * @param F forward price
     * @param K strike price
     * @param T time to expiration
     * @param isCall true for call, false for put
     * @return implied volatility
     * @throws BelowIntrinsicException if price is below intrinsic value
     * @throws AboveMaximumException if price exceeds maximum possible value
     * @throws RuntimeException if convergence fails
     */
    public static double impliedVolatility(double price, double F, double K, double T, boolean isCall) {
        // Check intrinsic value
        double intrinsic = Math.max(isCall ? F - K : K - F, 0.0);
        if (price < intrinsic - PRICE_TOLERANCE) {
            throw new BelowIntrinsicException();
        }

        // Check maximum value
        double maxPrice = isCall ? F : K;
        if (price >= maxPrice) {
            throw new AboveMaximumException();
        }

        // Handle at-the-money or near-zero price
        if (price <= intrinsic + PRICE_TOLERANCE) {
            return 0.0;
        }

        // Initial guess
        double sigma = INITIAL_GUESS;

        // Newton-Raphson iteration
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double bsPrice = blackScholesPrice(F, K, T, sigma, isCall);
            double priceDiff = bsPrice - price;

            // Check price convergence
            if (Math.abs(priceDiff) < PRICE_TOLERANCE) {
                return sigma;
            }

            double vega = blackScholesVega(F, K, T, sigma);

            // Check vega validity
            if (Math.abs(vega) < MIN_VEGA) {
                throw new RuntimeException("Vega too small, cannot continue iteration");
            }

            // Newton-Raphson update: σ_new = σ_old - f/f'
            double sigmaNew = sigma - priceDiff / vega;

            // Ensure sigma stays positive
            if (sigmaNew <= 0) {
                sigmaNew = sigma * 0.5;
            }

            // Check volatility convergence
            if (Math.abs(sigmaNew - sigma) < VOL_TOLERANCE) {
                return sigmaNew;
            }

            sigma = sigmaNew;
        }

        throw new RuntimeException("Newton-Raphson failed to converge after " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Compute implied volatility from option price (q notation).
     *
     * @param price option price
     * @param F forward price
     * @param K strike price
     * @param T time to expiration
     * @param q +1 for call, -1 for put
     * @return implied volatility
     */
    public static double impliedVolatility(double price, double F, double K, double T, int q) {
        return impliedVolatility(price, F, K, T, q > 0);
    }

    // Prevent instantiation
    private BlackScholesNewtonRaphson() {
        throw new AssertionError("BlackScholesNewtonRaphson class should not be instantiated");
    }
}
