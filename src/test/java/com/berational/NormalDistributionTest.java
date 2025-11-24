package com.berational;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test CodyNormDist implementation against Apache Commons Math.
 */
class NormalDistributionTest {

    private static final double TOLERANCE = 1e-14;
    private final org.apache.commons.math3.distribution.NormalDistribution apacheNormal =
        new org.apache.commons.math3.distribution.NormalDistribution(0.0, 1.0);

    @ParameterizedTest
    @ValueSource(doubles = {
        // Negative tail
        -5.0, -4.0, -3.5, -3.0, -2.5, -2.0, -1.5, -1.0, -0.5,
        // Center
        0.0, 0.001, 0.01, 0.1,
        // Positive tail
        0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0
    })
    void testCdfAgainstApacheCommons(double x) {
        double expected = apacheNormal.cumulativeProbability(x);
        double actual = CodyNormDist.cdf(x);

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
        assertEquals(0.5, CodyNormDist.cdf(0.0), TOLERANCE);

        // CDF is monotonically increasing
        assertTrue(CodyNormDist.cdf(-1.0) < CodyNormDist.cdf(0.0));
        assertTrue(CodyNormDist.cdf(0.0) < CodyNormDist.cdf(1.0));

        // CDF approaches 0 and 1 at extremes (use realistic precision)
        assertTrue(CodyNormDist.cdf(-10.0) < 1e-15);
        assertTrue(CodyNormDist.cdf(10.0) > 1.0 - 1e-15 || CodyNormDist.cdf(10.0) == 1.0);
    }

    @ParameterizedTest
    @CsvSource({
        "-3.0, 0.00134989803163",
        "-2.0, 0.02275013194817",
        "-1.0, 0.15865525393146",
        "0.0, 0.50000000000000",
        "1.0, 0.84134474606854",
        "2.0, 0.97724986805183",
        "3.0, 0.99865010196837"
    })
    void testCdfKnownValues(double x, double expected) {
        double actual = CodyNormDist.cdf(x);
        assertEquals(expected, actual, 1e-13,
            String.format("CDF(%.1f) mismatch", x));
    }

    @Test
    void testCdfSymmetry() {
        // CDF(-x) = 1 - CDF(x) for standard normal
        double[] testValues = {0.5, 1.0, 1.5, 2.0, 3.0};
        for (double x : testValues) {
            double leftTail = CodyNormDist.cdf(-x);
            double rightTail = CodyNormDist.cdf(x);
            assertEquals(1.0, leftTail + rightTail, TOLERANCE,
                String.format("CDF symmetry failed at x=%.6f", x));
        }
    }

    @Test
    void testCdfMethodsConsistency() {
        // Compare all three CDF methods
        double[] testValues = {-3.0, -1.0, 0.0, 1.0, 3.0};

        for (double x : testValues) {
            double cdf = CodyNormDist.cdf(x);
            double cdfErf = CodyNormDist.cdfErf(x);
            double cdfErfc = CodyNormDist.cdfErfc(x);

            // For positive x, cdf and cdfErf should be identical
            if (x >= 0) {
                assertEquals(cdf, cdfErf, TOLERANCE,
                    String.format("CDF methods differ at x=%.6f", x));
            } else {
                // For negative x, cdf uses erfc which is more accurate
                assertEquals(cdf, cdfErfc, TOLERANCE,
                    String.format("CDF methods differ at x=%.6f", x));
            }

            // All methods should be close to each other
            assertEquals(cdfErf, cdfErfc, 1e-12,
                String.format("CDF erf/erfc methods differ at x=%.6f", x));
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {-5.0, -3.0, -2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 5.0})
    void testPdfAgainstApacheCommons(double x) {
        double expected = apacheNormal.density(x);
        double actual = CodyNormDist.pdf(x);

        assertEquals(expected, actual, 1e-15,
            String.format("PDF(%.6f) mismatch", x));
    }

    @Test
    void testPdfSymmetry() {
        // PDF is symmetric: pdf(x) = pdf(-x)
        double[] testValues = {0.5, 1.0, 2.0, 3.0};
        for (double x : testValues) {
            assertEquals(CodyNormDist.pdf(x), CodyNormDist.pdf(-x), TOLERANCE,
                String.format("PDF symmetry failed at x=%.6f", x));
        }
    }

    @Test
    void testPdfMaximum() {
        // PDF is maximum at x=0
        double pdfZero = CodyNormDist.pdf(0.0);
        double[] testValues = {0.5, 1.0, 2.0};

        for (double x : testValues) {
            assertTrue(CodyNormDist.pdf(x) < pdfZero,
                String.format("PDF not maximum at 0 (tested x=%.1f)", x));
        }
    }

    @Test
    void testIntegralRelationship() {
        // The PDF should be the derivative of the CDF (approximately)
        double h = 1e-8;  // small step
        double[] testValues = {-2.0, -1.0, 0.0, 1.0, 2.0};

        for (double x : testValues) {
            double numericalDerivative = (CodyNormDist.cdf(x + h) - CodyNormDist.cdf(x - h)) / (2 * h);
            double pdf = CodyNormDist.pdf(x);

            assertEquals(pdf, numericalDerivative, 1e-6,
                String.format("PDF/CDF derivative relationship failed at x=%.1f", x));
        }
    }

    @Test
    void testExtremeTails() {
        // Test very extreme tail values
        double[] extremeNegative = {-10.0, -20.0, -26.0};
        double[] extremePositive = {10.0, 20.0, 26.0};

        for (double x : extremeNegative) {
            double cdf = CodyNormDist.cdf(x);
            // Use realistic precision threshold
            assertTrue(cdf >= 0.0 && cdf < 1e-15,
                String.format("CDF(%.1f) = %.2e out of expected range", x, cdf));
        }

        for (double x : extremePositive) {
            double cdf = CodyNormDist.cdf(x);
            // Allow for rounding to exactly 1.0 due to limited double precision
            assertTrue((cdf > 1.0 - 1e-15 && cdf <= 1.0) || cdf == 1.0,
                String.format("CDF(%.1f) = %.15f out of expected range", x, cdf));
        }
    }

    @Test
    void testNegativeXAccuracy() {
        // Test that negative x uses erfc for better accuracy
        // This is particularly important for values where cancellation would occur
        double[] negativeValues = {-5.0, -4.0, -3.0, -2.0, -1.0};

        for (double x : negativeValues) {
            double expected = apacheNormal.cumulativeProbability(x);
            double actual = CodyNormDist.cdf(x);

            // Should maintain high relative accuracy even for very small probabilities
            if (expected > 0) {
                double relativeError = Math.abs((actual - expected) / expected);
                assertTrue(relativeError < 1e-12,
                    String.format("Poor accuracy for negative x=%.1f: rel error=%.2e", x, relativeError));
            }
        }
    }
}
