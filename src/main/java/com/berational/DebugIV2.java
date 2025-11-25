package com.berational;

public class DebugIV2 {
    public static void main(String[] args) {
        double x = 0.0;
        double beta = 0.07965567455405798;
        
        double bMax = Math.exp(0.5 * x);
        double sC = Math.sqrt(Math.abs(2.0 * x));
        double bC = LetsBeRational.normalisedBlackCall(x, sC);
        double vC = LetsBeRational.normalisedVega(x, sC);
        
        System.out.println("x = " + x);
        System.out.println("beta = " + beta);
        System.out.println("bMax = " + bMax);
        System.out.println("sC = " + sC);
        System.out.println("bC = " + bC);
        System.out.println("vC = " + vC);
        System.out.println();
        
        // Check which branch
        System.out.println("beta < bC? " + (beta < bC));
        
        if (beta >= bC) {
            // Upper half
            double sH = vC > Double.MIN_NORMAL ? sC + (bMax - bC) / vC : sC;
            double bH = LetsBeRational.normalisedBlackCall(x, sH);
            System.out.println("sH = " + sH);
            System.out.println("bH = " + bH);
            System.out.println("beta <= bH? " + (beta <= bH));
        }
    }
}
