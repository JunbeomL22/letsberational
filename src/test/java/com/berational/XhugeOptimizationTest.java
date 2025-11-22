package com.berational;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify XHUGE optimization is working correctly.
 * XHUGE = 6.71e7 is the threshold where 1/(2x²) underflows in double precision.
 */
class XhugeOptimizationTest {

    @Test
    void testErfcxAtXhugeBoundary() {
        // Test values around XHUGE threshold
        double xhuge = 6.71e7;

        // Just below XHUGE - should use full rational approximation
        double x1 = xhuge * 0.99;
        double erfcx1 = CodyErf.erfcx(x1);

        // Just above XHUGE - should use simplified approximation
        double x2 = xhuge * 1.01;
        double erfcx2 = CodyErf.erfcx(x2);

        // Both should give reasonable values close to the asymptotic formula: 1/(x√π)
        double sqrpi = 1.0 / Math.sqrt(Math.PI);
        double expected1 = sqrpi / x1;
        double expected2 = sqrpi / x2;

        // The relative error should be small
        double relError1 = Math.abs((erfcx1 - expected1) / expected1);
        double relError2 = Math.abs((erfcx2 - expected2) / expected2);

        assertTrue(relError1 < 0.01,
            String.format("erfcx(%.2e) relative error: %.2e", x1, relError1));
        assertTrue(relError2 < 0.01,
            String.format("erfcx(%.2e) relative error: %.2e", x2, relError2));

        // Values should be very close to each other (smooth transition)
        double transition = Math.abs((erfcx1 - erfcx2) / erfcx1);
        assertTrue(transition < 0.02,
            String.format("Transition discontinuity: %.2e", transition));
    }

    @Test
    void testErfcAtLargeValues() {
        // For large x values in the XHUGE range
        double[] testValues = {1e7, 5e7, 6.71e7, 1e8};

        for (double x : testValues) {
            double erfc = CodyErf.erfc(x);
            double erfcx = CodyErf.erfcx(x);

            // erfc should be extremely small (close to 0)
            assertTrue(erfc >= 0.0 && erfc < 1e-10,
                String.format("erfc(%.2e) = %.2e should be very small", x, erfc));

            // erfcx should be close to 1/(x√π)
            double sqrpi = 1.0 / Math.sqrt(Math.PI);
            double expected = sqrpi / x;
            double relError = Math.abs((erfcx - expected) / expected);

            assertTrue(relError < 0.01,
                String.format("erfcx(%.2e) relative error: %.2e", x, relError));
        }
    }

    @Test
    void testErfAtLargeValues() {
        // For large positive x, erf should be very close to 1
        double[] testValues = {1e7, 5e7, 6.71e7, 1e8};

        for (double x : testValues) {
            double erf = CodyErf.erf(x);

            // Should be extremely close to 1.0
            assertTrue(erf > 1.0 - 1e-15 && erf <= 1.0,
                String.format("erf(%.2e) = %.15f should be ~1.0", x, erf));
        }
    }

    @Test
    void testNegativeValuesNotAffected() {
        // XHUGE optimization only applies to positive x in Region 3
        // Negative values should still work correctly
        double x = -1e7;

        double erf = CodyErf.erf(x);
        double erfc = CodyErf.erfc(x);

        // erf(-x) should be close to -1
        assertTrue(erf < -1.0 + 1e-15 && erf >= -1.0,
            String.format("erf(%.2e) = %.15f", x, erf));

        // erfc(-x) should be close to 2
        assertTrue(Math.abs(erfc - 2.0) < 1e-15,
            String.format("erfc(%.2e) = %.15f", x, erfc));
    }
}
