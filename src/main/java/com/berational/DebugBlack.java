package com.berational;

import static com.berational.Constants.*;
import static com.berational.NormalDistribution.*;
import static com.berational.CodyErf.erfcx;

public class DebugBlack {
    public static void main(String[] args) {
        // Test ATM call with sigma=0.2, T=1
        double F = 100.0, K = 100.0, T = 1.0, sigma = 0.2;
        double x = Math.log(F / K);  // 0
        double s = sigma * Math.sqrt(T);  // 0.2
        double h = x / s;  // 0
        double t = 0.5 * s;  // 0.1
        
        System.out.println("=== ATM Call Test ===");
        System.out.println("x = " + x);
        System.out.println("s = " + s);
        System.out.println("h = " + h);
        System.out.println("t = " + t);
        System.out.println("SMALL_T_THRESHOLD = " + SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD);
        System.out.println();
        
        // Region checks
        boolean region1 = x < s * ASYMPTOTIC_EXPANSION_ACCURACY_THRESHOLD &&
            0.5 * s * s + x < s * (SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD + ASYMPTOTIC_EXPANSION_ACCURACY_THRESHOLD);
        boolean region2 = t < SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD;
        boolean region3 = x + 0.5 * s * s > s * 0.85;
        
        System.out.println("Region 1 (asymptotic): " + region1);
        System.out.println("Region 2 (small t): " + region2);
        System.out.println("Region 3 (CDF): " + region3);
        System.out.println("Region 4 (erfcx): " + (!region1 && !region2 && !region3));
        System.out.println();
        
        // Compute using different methods
        double b_smallT = smallTExpansion(h, t);
        double b_cdf = usingCdf(x, s);
        double b_erfcx = usingErfcx(h, t);
        double b_actual = LetsBeRational.normalisedBlackCall(x, s);
        
        System.out.println("smallTExpansion = " + b_smallT);
        System.out.println("usingCdf = " + b_cdf);
        System.out.println("usingErfcx = " + b_erfcx);
        System.out.println("normalisedBlackCall = " + b_actual);
        System.out.println();
        
        // Check which one is used
        String region = "Region 4 (erfcx)";
        if (x > 0) region = "Put-call symmetry";
        else if (region1) region = "Region 1 (asymptotic)";
        else if (region2) region = "Region 2 (small t)";
        else if (region3) region = "Region 3 (CDF)";
        System.out.println("Used region: " + region);
        
        // Now test with high vol
        System.out.println("\n=== High Vol Test (sigma=2.0) ===");
        sigma = 2.0;
        s = sigma * Math.sqrt(T);
        h = x / s;
        t = 0.5 * s;
        
        System.out.println("x = " + x);
        System.out.println("s = " + s);
        System.out.println("h = " + h);
        System.out.println("t = " + t);
        
        region2 = t < SMALL_T_EXPANSION_OF_NORMALIZED_BLACK_THRESHOLD;
        region3 = x + 0.5 * s * s > s * 0.85;
        
        System.out.println("Region 2 (small t): " + region2);
        System.out.println("Region 3 (CDF): " + region3);
        
        b_cdf = usingCdf(x, s);
        b_erfcx = usingErfcx(h, t);
        b_actual = LetsBeRational.normalisedBlackCall(x, s);
        
        System.out.println("usingCdf = " + b_cdf);
        System.out.println("usingErfcx = " + b_erfcx);
        System.out.println("normalisedBlackCall = " + b_actual);
    }
    
    private static double smallTExpansion(double h, double t) {
        double a = 1.0 + h * (0.5 * SQRT_TWO_PI) * erfcx(-ONE_OVER_SQRT_TWO * h);
        double w = t * t;
        double h2 = h * h;
        double expansion = 2.0 * t * (a + w * ((-1.0 + 3.0 * a + a * h2) / 6.0 + w * ((-7.0 + 15.0 * a + h2 * (-1.0 + 10.0 * a + a * h2)) / 120.0 + w * ((-57.0 + 105.0 * a + h2 * (-18.0 + 105.0 * a + h2 * (-1.0 + 21.0 * a + a * h2))) / 5040.0 + w * ((-561.0 + 945.0 * a + h2 * (-285.0 + 1260.0 * a + h2 * (-33.0 + 378.0 * a + h2 * (-1.0 + 36.0 * a + a * h2)))) / 362880.0 + w * ((-6555.0 + 10395.0 * a + h2 * (-4680.0 + 17325.0 * a + h2 * (-840.0 + 6930.0 * a + h2 * (-52.0 + 990.0 * a + h2 * (-1.0 + 55.0 * a + a * h2))))) / 39916800.0 + ((-89055.0 + 135135.0 * a + h2 * (-82845.0 + 270270.0 * a + h2 * (-20370.0 + 135135.0 * a + h2 * (-1926.0 + 25740.0 * a + h2 * (-75.0 + 2145.0 * a + h2 * (-1.0 + 78.0 * a + a * h2)))))) * w) / 6227020800.0))))));;
        return ONE_OVER_SQRT_TWO_PI * Math.exp(-0.5 * (h * h + t * t)) * expansion;
    }
    
    private static double usingCdf(double x, double s) {
        double h = x / s;
        double t = 0.5 * s;
        double bMax = Math.exp(0.5 * x);
        return cdf(h + t) * bMax - cdf(h - t) / bMax;
    }
    
    private static double usingErfcx(double h, double t) {
        return 0.5 * Math.exp(-0.5 * (h * h + t * t)) *
               (erfcx(-ONE_OVER_SQRT_TWO * (h + t)) - erfcx(-ONE_OVER_SQRT_TWO * (h - t)));
    }
}
