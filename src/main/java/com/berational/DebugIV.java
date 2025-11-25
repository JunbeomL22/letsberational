package com.berational;

public class DebugIV {
    public static void main(String[] args) {
        // ATM call: F = K = 100, T = 1.0, sigma = 0.2
        double F = 100.0;
        double K = 100.0;
        double T = 1.0;
        double sigma = 0.2;
        int q = 1; // call
        
        // Compute the Black price
        double x = Math.log(F / K);  // 0
        double s = sigma * Math.sqrt(T);  // 0.2
        double normalizedPrice = LetsBeRational.normalisedBlackCall(x, s);
        double price = Math.sqrt(F * K) * normalizedPrice;
        
        System.out.println("=== ATM Call IV Test ===");
        System.out.println("F = " + F);
        System.out.println("K = " + K);
        System.out.println("T = " + T);
        System.out.println("sigma = " + sigma);
        System.out.println("x = " + x);
        System.out.println("s = " + s);
        System.out.println("normalizedPrice = " + normalizedPrice);
        System.out.println("price = " + price);
        System.out.println();
        
        // Compute implied vol
        double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
                price, F, K, T, q);
        System.out.println("impliedVol = " + impliedVol);
        System.out.println("sigma - impliedVol = " + (sigma - impliedVol));
        System.out.println();
        
        // Recalculate price
        double s2 = impliedVol * Math.sqrt(T);
        double normalizedPrice2 = LetsBeRational.normalisedBlackCall(x, s2);
        double price2 = Math.sqrt(F * K) * normalizedPrice2;
        
        System.out.println("s (original) = " + s);
        System.out.println("s2 (from impliedVol) = " + s2);
        System.out.println("normalizedPrice2 = " + normalizedPrice2);
        System.out.println("price2 = " + price2);
        System.out.println("price - price2 = " + (price - price2));
        System.out.println();
        
        // Test normalized implied volatility directly
        double beta = normalizedPrice;  // normalized price
        System.out.println("Testing normalized IV directly:");
        System.out.println("beta = " + beta);
        double normalizedImpliedVol = LetsBeRational.normalisedImpliedVolatilityFromATransformedRationalGuess(
                beta, x, q);
        System.out.println("normalizedImpliedVol = " + normalizedImpliedVol);
        System.out.println("s (original) = " + s);
        System.out.println("s - normalizedImpliedVol = " + (s - normalizedImpliedVol));
    }
}
