package com.berational;

public class TestCDF {
    public static void main(String[] args) {
        double cdf10 = CodyNormDist.cdf(10.0);
        System.out.println("CDF(10.0) = " + cdf10);
        System.out.printf("CDF(10.0) = %.20f%n", cdf10);
        System.out.println("CDF(10.0) > 1.0 - 1e-20? " + (cdf10 > 1.0 - 1e-20));
        System.out.println("CDF(10.0) <= 1.0? " + (cdf10 <= 1.0));
        System.out.println("Both conditions? " + (cdf10 > 1.0 - 1e-20 && cdf10 <= 1.0));

        double erfValue = CodyErf.erf(10.0 / Math.sqrt(2.0));
        System.out.printf("erf(10/sqrt(2)) = %.20f%n", erfValue);
        System.out.printf("0.5 * (1 + erf) = %.20f%n", 0.5 * (1.0 + erfValue));
    }
}
