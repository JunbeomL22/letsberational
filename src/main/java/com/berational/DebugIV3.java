package com.berational;

import static com.berational.RationalCubic.*;

public class DebugIV3 {
    public static void main(String[] args) {
        double x = 0.0;
        double beta = 0.07965567455405798;
        
        double bMax = Math.exp(0.5 * x);
        double sC = Math.sqrt(Math.abs(2.0 * x));  // 0
        double bC = LetsBeRational.normalisedBlackCall(x, sC);  // 0
        double vC = LetsBeRational.normalisedVega(x, sC);  // ~0.399
        
        System.out.println("=== Branch 3 (Center-right) ===");
        System.out.println("sC = " + sC);
        System.out.println("bC = " + bC);
        System.out.println("vC = " + vC);
        
        // sH calculation
        double sH = vC > Double.MIN_NORMAL ? sC + (bMax - bC) / vC : sC;
        double bH = LetsBeRational.normalisedBlackCall(x, sH);
        double vH = LetsBeRational.normalisedVega(x, sH);
        
        System.out.println("sH = " + sH);
        System.out.println("bH = " + bH);
        System.out.println("vH = " + vH);
        System.out.println();
        
        // Rational cubic parameters for branch 3
        System.out.println("Rational cubic interpolation:");
        System.out.println("  xl = bC = " + bC);
        System.out.println("  xr = bH = " + bH);
        System.out.println("  fl = sC = " + sC);
        System.out.println("  fr = sH = " + sH);
        System.out.println("  dl = 1/vC = " + (1.0/vC));
        System.out.println("  dr = 1/vH = " + (1.0/vH));
        System.out.println();
        
        // Compute r parameter
        double rHM = convexRationalCubicControlParameterToFitSecondDerivativeAtLeftSide(
                bC, bH, sC, sH, 1.0 / vC, 1.0 / vH, 0.0, false);
        System.out.println("rHM = " + rHM);
        
        // Do the interpolation
        double s = rationalCubicInterpolation(beta, bC, bH, sC, sH, 1.0 / vC, 1.0 / vH, rHM);
        System.out.println("Initial guess s = " + s);
        System.out.println("Expected s = 0.2");
        System.out.println("Error = " + (s - 0.2));
        System.out.println();
        
        // Verify the Black function at this s
        double b_at_s = LetsBeRational.normalisedBlackCall(x, s);
        System.out.println("normalisedBlackCall(x, s) = " + b_at_s);
        System.out.println("beta = " + beta);
        System.out.println("Difference = " + (b_at_s - beta));
    }
}
