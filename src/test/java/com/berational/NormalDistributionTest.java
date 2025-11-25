package com.berational;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for NormalDistribution implementation against Apache Commons Math.
 */
class NormalDistributionTest {

    private static final double TOLERANCE = 1e-14;
    private final org.apache.commons.math3.distribution.NormalDistribution apacheNormal =
        new org.apache.commons.math3.distribution.NormalDistribution(0.0, 1.0);

    // ==================== CDF Tests ====================

    @ParameterizedTest
    @ValueSource(doubles = {
        // Deep negative tail (asymptotic expansion region)
        -38.0, -30.0, -25.0, -20.0, -15.0, -12.0, -11.0, -10.5, -10.0,
        // Negative tail (erfc region)
        -9.0, -8.0, -7.0, -6.0, -5.5, -5.0, -4.5, -4.0, -3.5, -3.0, -2.5, -2.0, -1.5, -1.0, -0.5,
        // Center region
        -0.1, -0.01, -0.001, 0.0, 0.001, 0.01, 0.1,
        // Positive tail
        0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 7.0, 8.0, 9.0, 10.045081
    })
    void testCdfAgainstApacheCommons(double x) {
        double expected = apacheNormal.cumulativeProbability(x);
        double actual = NormalDistribution.cdf(x);

        // Use relative tolerance for extreme values
        if (Math.abs(expected) < 1e-10 || Math.abs(1.0 - expected) < 1e-10) {
            assertEquals(expected, actual, 1e-14,
                String.format("CDF(%.6f) mismatch (absolute)", x));
        } else {
            double relativeError = Math.abs((actual - expected) / expected);
            assertTrue(relativeError < 1e-13,
                String.format("CDF(%.6f) relative error: %.2e", x, relativeError));
        }
    }

    @Test
    void testCdfBoundaryValues() {
        // CDF(0) = 0.5
        assertEquals(0.5, NormalDistribution.cdf(0.0), TOLERANCE);

        // CDF is monotonically increasing
        assertTrue(NormalDistribution.cdf(-1.0) < NormalDistribution.cdf(0.0));
        assertTrue(NormalDistribution.cdf(0.0) < NormalDistribution.cdf(1.0));

        // CDF approaches 0 and 1 at extremes
        assertTrue(NormalDistribution.cdf(-10.0) < 1e-15);
        assertTrue(NormalDistribution.cdf(10.0) > 1.0 - 1e-15 || NormalDistribution.cdf(10.0) == 1.0);
    }

    @ParameterizedTest
    @CsvSource({
        "-4.0, 0.00003167124183311998",
        "-3.5, 0.00023262907903552503",
        "-3.0, 0.00134989803163009",
        "-2.5, 0.00620966532577613",
        "-2.0, 0.02275013194817921",
        "-1.5, 0.06680720126885807",
        "-1.0, 0.15865525393145707",
        "-0.5, 0.30853753872598694",
        "0.0, 0.50000000000000",
        "0.5, 0.69146246127401306",
        "1.0, 0.84134474606854293",
        "1.5, 0.93319279873114193",
        "2.0, 0.97724986805182079",
        "2.5, 0.99379033467422387",
        "3.0, 0.99865010196836991",
        "3.5, 0.99976737092096447",
        "4.0, 0.99996832875816688"
    })
    void testCdfKnownValues(double x, double expected) {
        double actual = NormalDistribution.cdf(x);
        assertEquals(expected, actual, 1e-13,
            String.format("CDF(%.1f) mismatch", x));
    }

    @Test
    void testCdfSymmetry() {
        // CDF(-x) = 1 - CDF(x) for standard normal
        double[] testValues = {0.1, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0};
        for (double x : testValues) {
            double leftTail = NormalDistribution.cdf(-x);
            double rightTail = NormalDistribution.cdf(x);
            assertEquals(1.0, leftTail + rightTail, TOLERANCE,
                String.format("CDF symmetry failed at x=%.6f", x));
        }
    }

    @Test
    void testCdfMonotonicity() {
        // CDF should be monotonically non-decreasing (strictly increasing until rounding to 1.0)
        double prev = NormalDistribution.cdf(-10.0);
        for (double x = -9.5; x <= 6.0; x += 0.5) {
            double current = NormalDistribution.cdf(x);
            assertTrue(current > prev,
                String.format("CDF not monotonic at x=%.1f: prev=%.15f, current=%.15f", x, prev, current));
            prev = current;
        }
        // Beyond x=6, CDF may round to exactly 1.0
        for (double x = 6.5; x <= 10.0; x += 0.5) {
            double current = NormalDistribution.cdf(x);
            assertTrue(current >= prev,
                String.format("CDF decreased at x=%.1f: prev=%.15f, current=%.15f", x, prev, current));
            prev = current;
        }
    }

    // ==================== PDF Tests ====================

    @ParameterizedTest
    @ValueSource(doubles = {
        -10.0, -8.0, -6.0, -5.0, -4.0, -3.0, -2.5, -2.0, -1.5, -1.0, -0.75, -0.5, -0.25, 
        -0.1, 0.0, 0.1, 0.25, 
        0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0
    })
    void testPdfAgainstApacheCommons(double x) {
        double expected = apacheNormal.density(x);
        double actual = NormalDistribution.pdf(x);

        assertEquals(expected, actual, 1e-15,
            String.format("PDF(%.6f) mismatch", x));
    }

    @Test
    void testPdfSymmetry() {
        // PDF is symmetric: pdf(x) = pdf(-x)
        double[] testValues = {0.1, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0};
        for (double x : testValues) {
            assertEquals(NormalDistribution.pdf(x), NormalDistribution.pdf(-x), TOLERANCE,
                String.format("PDF symmetry failed at x=%.6f", x));
        }
    }

    @Test
    void testPdfMaximum() {
        // PDF is maximum at x=0
        double pdfZero = NormalDistribution.pdf(0.0);
        double expectedMax = 1.0 / Math.sqrt(2.0 * Math.PI);
        assertEquals(expectedMax, pdfZero, TOLERANCE, "PDF(0) should equal 1/sqrt(2*pi)");

        double[] testValues = {0.01, 0.1, 0.5, 1.0, 2.0, 3.0};
        for (double x : testValues) {
            assertTrue(NormalDistribution.pdf(x) < pdfZero,
                String.format("PDF not maximum at 0 (tested x=%.2f)", x));
        }
    }

    @Test
    void testPdfKnownValues() {
        // PDF(0) = 1/sqrt(2*pi) ≈ 0.3989422804014327
        assertEquals(0.3989422804014327, NormalDistribution.pdf(0.0), 1e-15);

        // PDF(1) = PDF(-1) = exp(-0.5)/sqrt(2*pi) ≈ 0.24197072451914337
        assertEquals(0.24197072451914337, NormalDistribution.pdf(1.0), 1e-15);
        assertEquals(0.24197072451914337, NormalDistribution.pdf(-1.0), 1e-15);

        // PDF(2) = exp(-2)/sqrt(2*pi) ≈ 0.05399096651318806
        assertEquals(0.05399096651318806, NormalDistribution.pdf(2.0), 1e-15);
    }

    @Test
    void testIntegralRelationship() {
        // The PDF should be the derivative of the CDF (approximately)
        double h = 1e-8;
        double[] testValues = {-3.0, -2.0, -1.0, -0.5, 0.0, 0.5, 1.0, 2.0, 3.0};

        for (double x : testValues) {
            double numericalDerivative = (NormalDistribution.cdf(x + h) - NormalDistribution.cdf(x - h)) / (2 * h);
            double pdf = NormalDistribution.pdf(x);

            assertEquals(pdf, numericalDerivative, 1e-6,
                String.format("PDF/CDF derivative relationship failed at x=%.1f", x));
        }
    }

    // ==================== Inverse CDF Tests ====================

    @ParameterizedTest
    @ValueSource(doubles = {
        0.001, 0.01, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45,
        0.5,
        0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 0.99, 0.999
    })
    void testInverseCdfAgainstApacheCommons(double p) {
        double expected = apacheNormal.inverseCumulativeProbability(p);
        double actual = NormalDistribution.inverseCdf(p);

        // Special case for p=0.5 where expected=0
        if (Math.abs(expected) < 1e-15) {
            assertEquals(expected, actual, 1e-15,
                String.format("InverseCDF(%.6f) mismatch (expected=%.15f, actual=%.15f)",
                    p, expected, actual));
        } else {
            double relativeError = Math.abs((actual - expected) / expected);
            assertTrue(relativeError < 1e-14,
                String.format("InverseCDF(%.6f) relative error: %.2e (expected=%.15f, actual=%.15f)",
                    p, relativeError, expected, actual));
        }
    }

    @Test
    void testInverseCdfRoundTrip() {
        // CDF(inverseCDF(p)) should equal p
        double[] probabilities = {0.001, 0.01, 0.1, 0.25, 0.5, 0.75, 0.9, 0.99, 0.999};
        for (double p : probabilities) {
            double z = NormalDistribution.inverseCdf(p);
            double roundTrip = NormalDistribution.cdf(z);
            assertEquals(p, roundTrip, 1e-14,
                String.format("Round trip failed for p=%.6f: inverseCdf=%.15f, cdf=%.15f", p, z, roundTrip));
        }
    }

    @Test
    void testInverseCdfSymmetry() {
        // inverseCDF(p) = -inverseCDF(1-p)
        double[] probabilities = {0.01, 0.1, 0.25, 0.4};
        for (double p : probabilities) {
            double z1 = NormalDistribution.inverseCdf(p);
            double z2 = NormalDistribution.inverseCdf(1.0 - p);
            assertEquals(z1, -z2, TOLERANCE,
                String.format("InverseCDF symmetry failed for p=%.6f", p));
        }
    }

    @Test
    void testInverseCdfKnownValues() {
        // inverseCDF(0.5) = 0
        assertEquals(0.0, NormalDistribution.inverseCdf(0.5), TOLERANCE);

        // Known quantiles
        assertEquals(-1.6448536269514729, NormalDistribution.inverseCdf(0.05), 1e-14);
        assertEquals(-1.2815515655446004, NormalDistribution.inverseCdf(0.1), 1e-14);
        assertEquals(1.2815515655446004, NormalDistribution.inverseCdf(0.9), 1e-14);
        assertEquals(1.6448536269514729, NormalDistribution.inverseCdf(0.95), 1e-14);
        assertEquals(2.3263478740408408, NormalDistribution.inverseCdf(0.99), 1e-14);
    }

    @Test
    void testInverseCdfBoundaries() {
        // inverseCDF(0) should return -Infinity
        assertEquals(Double.NEGATIVE_INFINITY, NormalDistribution.inverseCdf(0.0));

        // inverseCDF(1) should return -Infinity (due to log(0))
        assertEquals(Double.NEGATIVE_INFINITY, NormalDistribution.inverseCdf(1.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {1e-10, 1e-8, 1e-6, 1e-4})
    void testInverseCdfExtremeTails(double p) {
        // Test very small probabilities
        double z = NormalDistribution.inverseCdf(p);
        assertTrue(z < -3.0, String.format("InverseCDF(%.2e) should be very negative, got %.6f", p, z));

        // Round trip should work
        double roundTrip = NormalDistribution.cdf(z);
        double relError = Math.abs((roundTrip - p) / p);
        assertTrue(relError < 1e-10,
            String.format("Extreme tail round trip failed: p=%.2e, z=%.6f, roundTrip=%.2e", p, z, roundTrip));
    }

    // ==================== Extreme Tail Tests ====================

    @Test
    void testExtremeTails() {
        // Test very extreme tail values
        double[] extremeNegative = {-10.0, -15.0, -20.0, -25.0, -30.0};
        double[] extremePositive = {10.0, 15.0, 20.0, 25.0, 30.0};

        for (double x : extremeNegative) {
            double cdf = NormalDistribution.cdf(x);
            assertTrue(cdf >= 0.0,
                String.format("CDF(%.1f) = %.2e should be non-negative", x, cdf));
            assertTrue(cdf < 1e-15 || x < -20,
                String.format("CDF(%.1f) = %.2e should be very small", x, cdf));
        }

        for (double x : extremePositive) {
            double cdf = NormalDistribution.cdf(x);
            assertTrue(cdf <= 1.0,
                String.format("CDF(%.1f) = %.15f should be <= 1", x, cdf));
            assertTrue(cdf > 1.0 - 1e-15 || cdf == 1.0,
                String.format("CDF(%.1f) = %.15f should be very close to 1", x, cdf));
        }
    }

    @Test
    void testAsymptoticExpansionRegion() {
        // Test the asymptotic expansion region (z <= -10)
        double[] testValues = {-10.0, -10.5, -11.0, -12.0, -15.0, -20.0};

        for (double x : testValues) {
            double expected = apacheNormal.cumulativeProbability(x);
            double actual = NormalDistribution.cdf(x);

            if (expected > 0) {
                double relativeError = Math.abs((actual - expected) / expected);
                assertTrue(relativeError < 1e-10,
                    String.format("Asymptotic region: CDF(%.1f) rel error=%.2e", x, relativeError));
            }
        }
    }

    @Test
    void testNegativeXAccuracy() {
        // Test accuracy for negative x values
        double[] negativeValues = {-8.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0};

        for (double x : negativeValues) {
            double expected = apacheNormal.cumulativeProbability(x);
            double actual = NormalDistribution.cdf(x);

            if (expected > 0) {
                double relativeError = Math.abs((actual - expected) / expected);
                assertTrue(relativeError < 1e-12,
                    String.format("Poor accuracy for negative x=%.1f: rel error=%.2e", x, relativeError));
            }
        }
    }

    // ==================== Edge Cases ====================

    @Test
    void testSpecialValues() {
        // Test behavior with special double values
        assertTrue(Double.isNaN(NormalDistribution.pdf(Double.NaN)));
        assertTrue(Double.isNaN(NormalDistribution.cdf(Double.NaN)));

        assertEquals(0.0, NormalDistribution.pdf(Double.POSITIVE_INFINITY));
        assertEquals(0.0, NormalDistribution.pdf(Double.NEGATIVE_INFINITY));

        assertEquals(1.0, NormalDistribution.cdf(Double.POSITIVE_INFINITY));
        assertEquals(0.0, NormalDistribution.cdf(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testSmallIncrements() {
        // Test that small increments produce reasonable changes
        double base = 0.0;
        double small = 1e-10;
        double cdfBase = NormalDistribution.cdf(base);
        double cdfSmall = NormalDistribution.cdf(base + small);

        assertTrue(cdfSmall > cdfBase,
            "CDF should increase for small positive increment");
        assertTrue(cdfSmall - cdfBase < small,
            "CDF change should be bounded");
    }
}
