package com.berational;

import static com.berational.Constants.*;

public class DebugIter {
    public static void main(String[] args) {
        double x = 0.0;
        double beta = 0.07965567455405798;
        double s = 0.2001430523428302;  // Initial guess from rational cubic
        double sLeft = 0.0;
        double sRight = 2.5066282746310002;
        
        System.out.println("=== Iteration Debug (Middle Branch) ===");
        System.out.println("beta = " + beta);
        System.out.println("x = " + x);
        System.out.println("Initial s = " + s);
        System.out.println("sLeft = " + sLeft);
        System.out.println("sRight = " + sRight);
        System.out.println();
        
        // Middle branch iteration (g(s) = b(s) - β)
        for (int i = 0; i < 3; i++) {
            double b = LetsBeRational.normalisedBlackCall(x, s);
            double bp = LetsBeRational.normalisedVega(x, s);
            
            System.out.println("Iteration " + i + ":");
            System.out.println("  s = " + s);
            System.out.println("  b(s) = " + b);
            System.out.println("  b'(s) = " + bp);
            System.out.println("  b(s) - beta = " + (b - beta));
            
            double newton = (beta - b) / bp;
            double halley = square(x / s) / s - s / 4.0;
            double hh3 = halley * halley - 3.0 * square(x / (s * s)) - 0.25;
            double ds = Math.max(-0.5 * s, newton * householderFactor(newton, halley, hh3));
            
            System.out.println("  newton = " + newton);
            System.out.println("  halley = " + halley);
            System.out.println("  hh3 = " + hh3);
            System.out.println("  ds = " + ds);
            
            // Check convergence
            if (Math.abs(ds) <= DBL_EPSILON * s) {
                System.out.println("  ** Converged (|ds| <= eps*s) **");
            }
            
            s += ds;
            System.out.println("  new s = " + s);
            System.out.println();
        }
        
        System.out.println("Final s = " + s);
        System.out.println("Expected s = 0.2");
        System.out.println("Error = " + (s - 0.2));
    }
    
    private static double square(double x) {
        return x * x;
    }
    
    private static double householderFactor(double newton, double halley, double hh3) {
        return (1.0 + 0.5 * halley * newton) / (1.0 + newton * (halley + hh3 * newton / 6.0));
    }
}
