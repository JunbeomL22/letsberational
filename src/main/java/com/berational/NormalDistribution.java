package com.berational;

import org.apache.commons.math3.util.FastMath;
import static com.berational.Constants.*;

/**
 * Normal distribution functions extending CodyNormDist with inverse CDF.
 *
 * Provides:
 * - PDF: probability density function
 * - CDF: cumulative distribution function
 * - Inverse CDF: quantile function (normal deviate)
 */
public class NormalDistribution {

    // Thresholds for asymptotic expansion
    private static final double NORM_CDF_ASYMPTOTIC_EXPANSION_FIRST_THRESHOLD = -10.0;
    private static final double NORM_CDF_ASYMPTOTIC_EXPANSION_SECOND_THRESHOLD = -1.0 / Math.sqrt(DBL_EPSILON);

    // ALGORITHM AS241 APPL. STATIST. (1988) VOL. 37, NO. 3
    // Coefficients for inverse normal CDF
    private static final double SPLIT1 = 0.425;
    private static final double SPLIT2 = 5.0;
    private static final double CONST1 = 0.180625;
    private static final double CONST2 = 1.6;

    // Coefficients for P close to 0.5
    private static final double A0 = 3.3871328727963666080E0;
    private static final double A1 = 1.3314166789178437745E+2;
    private static final double A2 = 1.9715909503065514427E+3;
    private static final double A3 = 1.3731693765509461125E+4;
    private static final double A4 = 4.5921953931549871457E+4;
    private static final double A5 = 6.7265770927008700853E+4;
    private static final double A6 = 3.3430575583588128105E+4;
    private static final double A7 = 2.5090809287301226727E+3;

    private static final double B1 = 4.2313330701600911252E+1;
    private static final double B2 = 6.8718700749205790830E+2;
    private static final double B3 = 5.3941960214247511077E+3;
    private static final double B4 = 2.1213794301586595867E+4;
    private static final double B5 = 3.9307895800092710610E+4;
    private static final double B6 = 2.8729085735721942674E+4;
    private static final double B7 = 5.2264952788528545610E+3;

    // Coefficients for P not close to 0, 0.5 or 1
    private static final double C0 = 1.42343711074968357734E0;
    private static final double C1 = 4.63033784615654529590E0;
    private static final double C2 = 5.76949722146069140550E0;
    private static final double C3 = 3.64784832476320460504E0;
    private static final double C4 = 1.27045825245236838258E0;
    private static final double C5 = 2.41780725177450611770E-1;
    private static final double C6 = 2.27238449892691845833E-2;
    private static final double C7 = 7.74545014278341407640E-4;

    private static final double D1 = 2.05319162663775882187E0;
    private static final double D2 = 1.67638483018380384940E0;
    private static final double D3 = 6.89767334985100004550E-1;
    private static final double D4 = 1.48103976427480074590E-1;
    private static final double D5 = 1.51986665636164571966E-2;
    private static final double D6 = 5.47593808499534494600E-4;
    private static final double D7 = 1.05075007164441684324E-9;

    // Coefficients for P very close to 0 or 1
    private static final double E0 = 6.65790464350110377720E0;
    private static final double E1 = 5.46378491116411436990E0;
    private static final double E2 = 1.78482653991729133580E0;
    private static final double E3 = 2.96560571828504891230E-1;
    private static final double E4 = 2.65321895265761230930E-2;
    private static final double E5 = 1.24266094738807843860E-3;
    private static final double E6 = 2.71155556874348757815E-5;
    private static final double E7 = 2.01033439929228813265E-7;

    private static final double F1 = 5.99832206555887937690E-1;
    private static final double F2 = 1.36929880922735805310E-1;
    private static final double F3 = 1.48753612908506148525E-2;
    private static final double F4 = 7.86869131145613259100E-4;
    private static final double F5 = 1.84631831751005468180E-5;
    private static final double F6 = 1.42151175831644588870E-7;
    private static final double F7 = 2.04426310338993978564E-15;

    /**
     * Standard normal probability density function.
     * φ(x) = (1/√(2π)) × exp(-x²/2)
     */
    public static double pdf(double x) {
        return ONE_OVER_SQRT_TWO_PI * FastMath.exp(-0.5 * x * x);
    }

    /**
     * Standard normal cumulative distribution function using asymptotic expansion
     * for very negative values and Cody's erfc for other values.
     *
     * @param z the argument
     * @return Φ(z)
     */
    public static double cdf(double z) {
        if (z <= NORM_CDF_ASYMPTOTIC_EXPANSION_FIRST_THRESHOLD) {
            // Asymptotic expansion for very negative z
            // Following (26.2.12) in Abramowitz & Stegun
            double sum = 1.0;

            if (z >= NORM_CDF_ASYMPTOTIC_EXPANSION_SECOND_THRESHOLD) {
                double zsqr = z * z;
                int i = 1;
                double g = 1.0;
                double a = DBL_MAX;
                double lasta;

                do {
                    lasta = a;
                    double x = (4.0 * i - 3.0) / zsqr;
                    double y = x * ((4.0 * i - 1.0) / zsqr);
                    a = g * (x - y);
                    sum -= a;
                    g *= y;
                    i++;
                    a = Math.abs(a);
                } while (lasta > a && a >= Math.abs(sum * DBL_EPSILON));
            }

            return -pdf(z) * sum / z;
        }

        return 0.5 * CodyErf.erfc(-z * ONE_OVER_SQRT_TWO);
    }

    /**
     * Inverse of cumulative distribution function (quantile function).
     *
     * ALGORITHM AS241 APPL. STATIST. (1988) VOL. 37, NO. 3
     * Produces the normal deviate Z corresponding to a given lower tail area.
     * Accurate to about 1 part in 10^16.
     *
     * @param u probability in [0, 1]
     * @return z such that Φ(z) = u
     */
    public static double inverseCdf(double u) {
        if (u <= 0.0) {
            return Math.log(u);  // Returns -Infinity for u=0
        }
        if (u >= 1.0) {
            return Math.log(1.0 - u);  // Returns -Infinity for u=1
        }

        double q = u - 0.5;

        // Central region: |u - 0.5| <= 0.425
        if (Math.abs(q) <= SPLIT1) {
            double r = CONST1 - q * q;
            return q * (((((((A7 * r + A6) * r + A5) * r + A4) * r + A3) * r + A2) * r + A1) * r + A0) /
                       (((((((B7 * r + B6) * r + B5) * r + B4) * r + B3) * r + B2) * r + B1) * r + 1.0);
        }

        // Tail regions
        double r = (q < 0.0) ? u : 1.0 - u;
        r = Math.sqrt(-Math.log(r));

        double ret;
        if (r < SPLIT2) {
            r -= CONST2;
            ret = (((((((C7 * r + C6) * r + C5) * r + C4) * r + C3) * r + C2) * r + C1) * r + C0) /
                  (((((((D7 * r + D6) * r + D5) * r + D4) * r + D3) * r + D2) * r + D1) * r + 1.0);
        } else {
            r -= SPLIT2;
            ret = (((((((E7 * r + E6) * r + E5) * r + E4) * r + E3) * r + E2) * r + E1) * r + E0) /
                  (((((((F7 * r + F6) * r + F5) * r + F4) * r + F3) * r + F2) * r + F1) * r + 1.0);
        }

        return (q < 0.0) ? -ret : ret;
    }

    // Prevent instantiation
    private NormalDistribution() {
        throw new AssertionError("NormalDistribution class should not be instantiated");
    }
}
