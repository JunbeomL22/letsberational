package com.berational;

/**
 * Example usage of the Let's Be Rational algorithm.
 *
 * Demonstrates how to compute Black implied volatility from option prices.
 */
public class LetsBeRationalExample {

    public static void main(String[] args) {
        System.out.println("=== Let's Be Rational - Implied Volatility Examples ===\n");

        // Example 1: At-the-money call
        example1_ATMCall();

        // Example 2: Out-of-the-money put
        example2_OTMPut();

        // Example 3: In-the-money call
        example3_ITMCall();

        // Example 4: Extreme cases
        example4_ExtremeCases();

        // Example 5: Performance demonstration
        example5_Performance();
    }

    private static void example1_ATMCall() {
        System.out.println("Example 1: At-the-Money Call Option");
        System.out.println("------------------------------------");

        double F = 100.0;      // Forward price
        double K = 100.0;      // Strike price (ATM)
        double T = 1.0;        // Time to expiry (1 year)
        double price = 7.965;  // Option price
        int q = 1;             // Call option

        double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                price, F, K, T, q);

        System.out.println("Forward Price (F):     " + F);
        System.out.println("Strike Price (K):      " + K);
        System.out.println("Time to Expiry (T):    " + T + " years");
        System.out.println("Option Price:          " + price);
        System.out.println("Option Type:           Call");
        System.out.println("\nImplied Volatility:    " + String.format("%.4f", impliedVol) + " (" +
                String.format("%.2f", impliedVol * 100) + "%)");
        System.out.println();
    }

    private static void example2_OTMPut() {
        System.out.println("Example 2: Out-of-the-Money Put Option");
        System.out.println("---------------------------------------");

        double F = 110.0;      // Forward price
        double K = 100.0;      // Strike price (OTM for put)
        double T = 0.5;        // Time to expiry (6 months)
        double price = 2.5;    // Option price
        int q = -1;            // Put option

        double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                price, F, K, T, q);

        System.out.println("Forward Price (F):     " + F);
        System.out.println("Strike Price (K):      " + K);
        System.out.println("Time to Expiry (T):    " + T + " years");
        System.out.println("Option Price:          " + price);
        System.out.println("Option Type:           Put");
        System.out.println("Moneyness (F/K):       " + String.format("%.2f", F/K));
        System.out.println("\nImplied Volatility:    " + String.format("%.4f", impliedVol) + " (" +
                String.format("%.2f", impliedVol * 100) + "%)");
        System.out.println();
    }

    private static void example3_ITMCall() {
        System.out.println("Example 3: In-the-Money Call Option");
        System.out.println("------------------------------------");

        double F = 120.0;      // Forward price
        double K = 100.0;      // Strike price (ITM for call)
        double T = 2.0;        // Time to expiry (2 years)
        double price = 28.0;   // Option price
        int q = 1;             // Call option

        double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                price, F, K, T, q);

        double intrinsic = Math.max(F - K, 0.0);
        double timeValue = price - intrinsic;

        System.out.println("Forward Price (F):     " + F);
        System.out.println("Strike Price (K):      " + K);
        System.out.println("Time to Expiry (T):    " + T + " years");
        System.out.println("Option Price:          " + price);
        System.out.println("Intrinsic Value:       " + intrinsic);
        System.out.println("Time Value:            " + String.format("%.2f", timeValue));
        System.out.println("Option Type:           Call");
        System.out.println("\nImplied Volatility:    " + String.format("%.4f", impliedVol) + " (" +
                String.format("%.2f", impliedVol * 100) + "%)");
        System.out.println();
    }

    private static void example4_ExtremeCases() {
        System.out.println("Example 4: Extreme Cases");
        System.out.println("------------------------");

        // Very low volatility
        testCase("Very Low Vol (1%)", 100.0, 100.0, 1.0, 0.01, 1);

        // Very high volatility
        testCase("Very High Vol (200%)", 100.0, 100.0, 1.0, 2.0, 1);

        // Deep OTM
        testCase("Deep OTM Call", 100.0, 150.0, 1.0, 0.3, 1);

        // Deep ITM
        testCase("Deep ITM Put", 80.0, 100.0, 1.0, 0.25, -1);

        // Short expiry
        testCase("Short Expiry (1 day)", 100.0, 100.0, 1.0/365.0, 0.2, 1);

        System.out.println();
    }

    private static void testCase(String description, double F, double K, double T,
                                 double inputVol, int q) {
        // First compute price from input volatility
        double price = computeBlackPrice(F, K, inputVol, T, q);

        // Then recover implied volatility
        double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                price, F, K, T, q);

        double error = Math.abs(impliedVol - inputVol);
        String status = error < 1e-10 ? "✓" : "✗";

        System.out.println(String.format("%-25s Input: %.4f  Implied: %.4f  Error: %.2e %s",
                description, inputVol, impliedVol, error, status));
    }

    private static void example5_Performance() {
        System.out.println("Example 5: Performance Demonstration");
        System.out.println("------------------------------------");

        int numIterations = 100000;
        double F = 100.0;
        double K = 100.0;
        double T = 1.0;
        int q = 1;

        // Compute a price
        double price = computeBlackPrice(F, K, 0.25, T, q);

        // Warm up
        for (int i = 0; i < 1000; i++) {
            LetsBeRational.impliedVolatilityFromATransformedRationalGuess(price, F, K, T, q);
        }

        // Time the computation
        long startTime = System.nanoTime();
        for (int i = 0; i < numIterations; i++) {
            LetsBeRational.impliedVolatilityFromATransformedRationalGuess(price, F, K, T, q);
        }
        long endTime = System.nanoTime();

        double avgTimeNanos = (endTime - startTime) / (double) numIterations;
        double avgTimeMicros = avgTimeNanos / 1000.0;

        System.out.println("Iterations:            " + numIterations);
        System.out.println("Average time:          " + String.format("%.3f", avgTimeMicros) + " microseconds");
        System.out.println("Throughput:            " + String.format("%.0f", 1_000_000.0 / avgTimeMicros) + " ops/sec");
        System.out.println("\nNote: Two iterations achieve machine precision for all inputs!");
        System.out.println();
    }

    /**
     * Helper method to compute Black option price from volatility.
     * Uses the normalized Black call function.
     */
    private static double computeBlackPrice(double F, double K, double sigma, double T, int q) {
        double x = Math.log(F / K);
        double s = sigma * Math.sqrt(T);

        // For puts, use put-call symmetry
        double normalizedPrice = LetsBeRational.normalisedBlackCall(q < 0 ? -x : x, s);

        return Math.sqrt(F * K) * normalizedPrice;
    }
}
