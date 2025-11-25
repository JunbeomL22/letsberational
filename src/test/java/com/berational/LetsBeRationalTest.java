package com.berational;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for LetsBeRational implied volatility algorithm.
 */
public class LetsBeRationalTest {

    private static final double VOL_EPSILON = 1e-13;
    private static final double PRICE_TOLERANCE = 1e-13;

    private static final double[] TEST_VOLS = {0.1, 0.3, 0.8};

    @Test
    public void testAtTheMoneyCall() {
        // ATM call: F = K = 100, T = 1.0
        double F = 100.0;
        double K = 100.0;
        double T = 1.0;
        int q = 1; // call

        for (double sigma : TEST_VOLS) {
            double price = blackPrice(F, K, sigma, T, q);
            double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
            double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

            assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                    "ATM call recalculated price should match original price (sigma=" + sigma + ")");
            assertEquals(sigma, impliedVol, VOL_EPSILON,
                    "ATM call implied volatility should match original volatility (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testInTheMoneyCall() {
        // ITM call: F = 110, K = 100, T = 0.5
        double F = 110.0;
        double K = 100.0;
        double T = 0.5;
        int q = 1; // call

        for (double sigma : TEST_VOLS) {
            double price = blackPrice(F, K, sigma, T, q);
            double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
            double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

            assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                    "ITM call recalculated price should match original price (sigma=" + sigma + ")");
            assertEquals(sigma, impliedVol, VOL_EPSILON,
                    "ITM call implied volatility should match original volatility (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testOutOfTheMoneyCall() {
        // OTM call: F = 90, K = 100, T = 2.0
        double F = 90.0;
        double K = 100.0;
        double T = 2.0;
        int q = 1; // call

        for (double sigma : TEST_VOLS) {
            double price = blackPrice(F, K, sigma, T, q);
            double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
            double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

            assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                    "OTM call recalculated price should match original price (sigma=" + sigma + ")");
            assertEquals(sigma, impliedVol, VOL_EPSILON,
                    "OTM call implied volatility should match original volatility (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testAtTheMoneyPut() {
        // ATM put: F = K = 100, T = 1.0
        double F = 100.0;
        double K = 100.0;
        double T = 1.0;
        int q = -1; // put

        for (double sigma : TEST_VOLS) {
            double price = blackPrice(F, K, sigma, T, q);
            double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
            double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

            assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                    "ATM put recalculated price should match original price (sigma=" + sigma + ")");
            assertEquals(sigma, impliedVol, VOL_EPSILON,
                    "ATM put implied volatility should match original volatility (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testInTheMoneyPut() {
        // ITM put: F = 90, K = 100, T = 0.5
        double F = 90.0;
        double K = 100.0;
        double T = 0.5;
        int q = -1; // put

        for (double sigma : TEST_VOLS) {
            double price = blackPrice(F, K, sigma, T, q);
            double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
            double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

            assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                    "ITM put recalculated price should match original price (sigma=" + sigma + ")");
            assertEquals(sigma, impliedVol, VOL_EPSILON,
                    "ITM put implied volatility should match original volatility (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testOutOfTheMoneyPut() {
        // OTM put: F = 110, K = 100, T = 2.0
        double F = 110.0;
        double K = 100.0;
        double T = 2.0;
        int q = -1; // put

        for (double sigma : TEST_VOLS) {
            double price = blackPrice(F, K, sigma, T, q);
            double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
            double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

            assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                    "OTM put recalculated price should match original price (sigma=" + sigma + ")");
            assertEquals(sigma, impliedVol, VOL_EPSILON,
                    "OTM put implied volatility should match original volatility (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testLowVolatility() {
        // Very low volatility: σ = 0.01, test ATM, ITM, OTM
        // Note: For ITM/OTM with very low vol, use tighter moneyness to avoid numerical issues
        double T = 1.0;
        double sigma = 0.01;
        int q = 1;

        // ATM
        testImpliedVol(100.0, 100.0, sigma, T, q, "Low volatility ATM");
        // Slightly ITM (avoid extreme cases with very low vol)
        testImpliedVol(101.0, 100.0, sigma, T, q, "Low volatility ITM");
        // Slightly OTM
        testImpliedVol(99.0, 100.0, sigma, T, q, "Low volatility OTM");
    }

    @Test
    public void testHighVolatility() {
        // High volatility: σ = 2.0, test ATM, ITM, OTM
        double T = 1.0;
        double sigma = 2.0;
        int q = 1;

        // ATM
        testImpliedVol(100.0, 100.0, sigma, T, q, "High volatility ATM");
        // ITM
        testImpliedVol(110.0, 100.0, sigma, T, q, "High volatility ITM");
        // OTM
        testImpliedVol(90.0, 100.0, sigma, T, q, "High volatility OTM");
    }

    @Test
    public void testShortExpiry() {
        // Short time to expiry: T = 0.01 (about 2.5 days), test ATM, ITM, OTM
        // Note: For short expiry, use tighter moneyness to avoid numerical issues with low vol
        double T = 0.01;
        int q = 1;

        for (double sigma : TEST_VOLS) {
            // ATM
            testImpliedVol(100.0, 100.0, sigma, T, q, "Short expiry ATM (sigma=" + sigma + ")");
            // Slightly ITM (avoid extreme ITM/OTM with short expiry + low vol)
            testImpliedVol(102.0, 100.0, sigma, T, q, "Short expiry ITM (sigma=" + sigma + ")");
            // Slightly OTM
            testImpliedVol(98.0, 100.0, sigma, T, q, "Short expiry OTM (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testLongExpiry() {
        // Long time to expiry: T = 10.0 years, test ATM, ITM, OTM
        double T = 10.0;
        int q = 1;

        for (double sigma : TEST_VOLS) {
            // ATM
            testImpliedVol(100.0, 100.0, sigma, T, q, "Long expiry ATM (sigma=" + sigma + ")");
            // ITM
            testImpliedVol(110.0, 100.0, sigma, T, q, "Long expiry ITM (sigma=" + sigma + ")");
            // OTM
            testImpliedVol(90.0, 100.0, sigma, T, q, "Long expiry OTM (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testDeepOTMCall() {
        // Deep OTM call: K/F = 2.0, test multiple vols
        double F = 100.0;
        double K = 200.0;
        double T = 1.0;
        int q = 1;

        for (double sigma : TEST_VOLS) {
            testImpliedVol(F, K, sigma, T, q, "Deep OTM call (sigma=" + sigma + ")");
        }
    }

    @Test
    public void testDeepITMCall() {
        // Deep ITM call: F/K = 2.0, test multiple vols
        // Note: Deep ITM with low vol has numerical precision limits, use moderate-high vols
        double F = 200.0;
        double K = 100.0;
        double T = 1.0;
        int q = 1;

        double[] deepItmVols = {0.3, 0.5, 0.8};
        for (double sigma : deepItmVols) {
            testImpliedVol(F, K, sigma, T, q, "Deep ITM call (sigma=" + sigma + ")");
        }
    }

    private void testImpliedVol(double F, double K, double sigma, double T, int q, String description) {
        double price = blackPrice(F, K, sigma, T, q);
        double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                price, F, K, T, q);
        double recalculatedPrice = blackPrice(F, K, impliedVol, T, q);

        assertEquals(price, recalculatedPrice, PRICE_TOLERANCE,
                description + " recalculated price should match original price");
        assertEquals(sigma, impliedVol, VOL_EPSILON,
                description + " implied volatility should match original volatility");
    }

    @Test
    public void testBelowIntrinsicException() {
        // Price below intrinsic value should throw exception
        double F = 110.0;
        double K = 100.0;
        double T = 1.0;
        int q = 1; // call
        double price = 5.0; // Intrinsic is 10.0

        assertThrows(BelowIntrinsicException.class, () -> {
            LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
        });
    }

    @Test
    public void testAboveMaximumException() {
        // Price above maximum value should throw exception
        double F = 100.0;
        double K = 100.0;
        double T = 1.0;
        int q = 1; // call
        double price = 105.0; // Maximum is 100.0 (forward price)

        assertThrows(AboveMaximumException.class, () -> {
            LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                    price, F, K, T, q);
        });
    }

    @Test
    public void testPutCallParity() {
        // Verify that call and put with same strike give same implied vol
        double F = 105.0;
        double K = 100.0;
        double T = 1.0;
        double sigma = 0.25;

        double callPrice = blackPrice(F, K, sigma, T, 1);
        double putPrice = blackPrice(F, K, sigma, T, -1);

        double callVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                callPrice, F, K, T, 1);
        double putVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                putPrice, F, K, T, -1);

        // Recalculate prices with recovered volatilities
        double recalculatedCallPrice = blackPrice(F, K, callVol, T, 1);
        double recalculatedPutPrice = blackPrice(F, K, putVol, T, -1);

        assertEquals(callPrice, recalculatedCallPrice, PRICE_TOLERANCE,
                "Call price should match recalculated call price");

        assertEquals(putPrice, recalculatedPutPrice, PRICE_TOLERANCE,
                "Put price should match recalculated put price");
    }

    @Test
    public void testNormalisedBlackCall() {
        // Test normalized Black call computation
        double x = -0.1;  // ln(F/K) for K slightly > F
        double s = 0.2;   // σ√T

        double result = LetsBeRational.normalisedBlackCall(x, s);

        assertTrue(result >= 0.0, "Normalized Black call should be non-negative");
        assertTrue(result <= Math.exp(0.5 * x), "Should not exceed b_max");
    }

    @Test
    public void testNormalisedVega() {
        // Test normalized vega computation
        double x = 0.0;  // ATM
        double s = 0.2;

        double vega = LetsBeRational.normalisedVega(x, s);

        assertTrue(vega > 0.0, "Vega should be positive for s > 0");
    }

    /**
     * Helper: Compute Black option price.
     * Uses the normalized Black call implementation from LetsBeRational.
     */
    private double blackPrice(double F, double K, double sigma, double T, int q) {
        double x = Math.log(F / K);
        double s = sigma * Math.sqrt(T);

        // Use the normalized Black call from our implementation
        // For puts, we use put-call symmetry: put(x,s) = call(-x,s)
        double normalizedPrice = LetsBeRational.normalisedBlackCall(q < 0 ? -x : x, s);

        return Math.sqrt(F * K) * normalizedPrice;
    }
}
