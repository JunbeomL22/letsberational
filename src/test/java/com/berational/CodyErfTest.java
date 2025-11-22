package com.berational;

import org.apache.commons.math3.special.Erf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Cody's erf implementation against Apache Commons Math.
 */
class CodyErfTest {

    private static final double TOLERANCE = 1e-14;  // 14 digits of precision

    @ParameterizedTest
    @ValueSource(doubles = {
        // Small values (Region 1)
        0.0, 0.001, 0.01, 0.1, 0.2, 0.3, 0.4, 0.46875,
        // Medium values (Region 2)
        0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0,
        // Large values (Region 3)
        4.5, 5.0, 6.0, 8.0, 10.0, 15.0, 20.0, 26.0,
        // Negative values
        -0.001, -0.1, -0.5, -1.0, -2.0, -3.0, -4.0, -5.0, -10.0, -20.0
    })
    void testErfAgainstApacheCommons(double x) {
        double expected = Erf.erf(x);
        double actual = CodyErf.erf(x);

        assertEquals(expected, actual, TOLERANCE,
            String.format("erf(%.6f) mismatch", x));
    }

    @ParameterizedTest
    @ValueSource(doubles = {
        0.0, 0.001, 0.1, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0, 20.0,
        -0.001, -0.1, -0.5, -1.0, -2.0, -3.0, -4.0, -5.0, -10.0
    })
    void testErfcAgainstApacheCommons(double x) {
        double expected = Erf.erfc(x);
        double actual = CodyErf.erfc(x);

        // Use relative tolerance for very small values
        if (Math.abs(expected) < 1e-10) {
            assertEquals(expected, actual, 1e-24,
                String.format("erfc(%.6f) mismatch (absolute)", x));
        } else {
            double relativeError = Math.abs((actual - expected) / expected);
            assertTrue(relativeError < 1e-13,
                String.format("erfc(%.6f) relative error too large: %.2e", x, relativeError));
        }
    }

    @Test
    void testErfSymmetry() {
        // erf(-x) = -erf(x)
        double[] testValues = {0.5, 1.0, 2.0, 3.0, 5.0};
        for (double x : testValues) {
            assertEquals(-CodyErf.erf(x), CodyErf.erf(-x), TOLERANCE,
                String.format("erf symmetry failed at x=%.6f", x));
        }
    }

    @Test
    void testErfcComplement() {
        // erfc(x) = 1 - erf(x)
        double[] testValues = {0.0, 0.5, 1.0, 2.0, 3.0};
        for (double x : testValues) {
            double erfcValue = CodyErf.erfc(x);
            double complement = 1.0 - CodyErf.erf(x);
            assertEquals(complement, erfcValue, TOLERANCE,
                String.format("erfc complement property failed at x=%.6f", x));
        }
    }

    @Test
    void testErfcxDefinition() {
        // erfcx(x) = exp(x²) × erfc(x)
        double[] testValues = {0.0, 0.5, 1.0, 2.0, 3.0, 5.0};
        for (double x : testValues) {
            double erfcxValue = CodyErf.erfcx(x);
            double expected = Math.exp(x * x) * CodyErf.erfc(x);
            assertEquals(expected, erfcxValue, TOLERANCE,
                String.format("erfcx definition failed at x=%.6f", x));
        }
    }

    @Test
    void testErfBoundaryValues() {
        // erf(0) = 0
        assertEquals(0.0, CodyErf.erf(0.0), 0.0);

        // erf(∞) ≈ 1
        assertTrue(CodyErf.erf(10.0) > 0.9999999999);

        // erf(-∞) ≈ -1
        assertTrue(CodyErf.erf(-10.0) < -0.9999999999);
    }

    @Test
    void testErfcBoundaryValues() {
        // erfc(0) = 1
        assertEquals(1.0, CodyErf.erfc(0.0), 0.0);

        // erfc(∞) ≈ 0
        assertTrue(CodyErf.erfc(10.0) < 1e-10);

        // erfc(-∞) ≈ 2
        assertTrue(CodyErf.erfc(-10.0) > 1.9999999999);
    }

    @Test
    void testRegionBoundaries() {
        // Test at exact region boundaries
        double[] boundaries = {0.46875, 4.0};

        for (double x : boundaries) {
            double expected = Erf.erf(x);
            double actual = CodyErf.erf(x);
            assertEquals(expected, actual, TOLERANCE,
                String.format("erf failed at boundary x=%.6f", x));

            expected = Erf.erfc(x);
            actual = CodyErf.erfc(x);
            assertEquals(expected, actual, TOLERANCE,
                String.format("erfc failed at boundary x=%.6f", x));
        }
    }

    @Test
    void testHighPrecision() {
        // Test cases where high precision matters
        double[][] testCases = {
            {1.0, 0.8427007929497148},      // erf(1)
            {2.0, 0.9953222650189527},      // erf(2)
            {3.0, 0.9999779095030014},      // erf(3)
        };

        for (double[] testCase : testCases) {
            double x = testCase[0];
            double expected = testCase[1];
            double actual = CodyErf.erf(x);
            assertEquals(expected, actual, 1e-15,
                String.format("High precision erf(%.1f) failed", x));
        }
    }
}
