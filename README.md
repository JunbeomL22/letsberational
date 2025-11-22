# Let's be rational - Java Implementation

A Java implementation of Peter Jäckel's "Let's Be Rational" algorithm for computing Black's implied volatility from option prices.

## Overview

This library implements the algorithm described in the paper **"Let's Be Rational"** by Peter Jäckel (March 25, 2016). The algorithm achieves machine precision (relative error < 10⁻¹⁵) in exactly **2 iterations** for all possible inputs when computing Black implied volatility from option prices.

## Key Features

- **Fast Convergence**: Two iterations to machine precision for all inputs
- **Robust**: Handles extreme parameter values encountered in practice
- **Accurate**: Uses fourth-order Householder iteration (third-order method)
- **Industrial Strength**: Four rational function branches for initial guess adapted to log-moneyness
- **Highly Optimized**: Four evaluation regimes for the Black function minimize numerical errors

## Algorithm Highlights

The implementation uses:
1. **Four-branch initial guess** using rational cubic interpolation adapted to different price regimes
2. **Three-branch objective function** optimized for numerical stability
3. **Householder(3) iteration** providing fourth-order convergence
4. **Cody's rational approximations** for error functions (erf, erfc, erfcx)

## Public Interface

### Main Entry Point

```java
public static double impliedVolatilityFromATransformedRationalGuess(
    double price,  // Option price
    double F,      // Forward price
    double K,      // Strike price
    double T,      // Time to expiration
    int q          // +1 for call, -1 for put
)
```

**Returns**: Implied volatility σ

**Throws**:
- `BelowIntrinsicException` - if price is below intrinsic value
- `AboveMaximumException` - if price exceeds maximum possible value

### Normalized Interface

For advanced usage with pre-normalized inputs:

```java
public static double normalisedImpliedVolatilityFromATransformedRationalGuess(
    double beta,  // Normalized price: β = price / √(F·K)
    double x,     // Log-moneyness: ln(F/K)
    int q         // +1 for call, -1 for put
)
```

**Returns**: Normalized implied volatility σ√T

### Supporting Functions

```java
// Compute normalized Black call value
public static double normalisedBlackCall(double x, double s)

// Compute normalized vega
public static double normalisedVega(double x, double s)
```

**Parameters**:
- `x` - log-moneyness ln(F/K)
- `s` - normalized volatility σ√T

## Usage Example

```java
import com.berational.LetsBeRational;

// Market data
double optionPrice = 5.0;
double forwardPrice = 100.0;
double strikePrice = 105.0;
double timeToExpiry = 0.25;  // 3 months
int optionType = 1;           // Call option

try {
    double impliedVol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
        optionPrice,
        forwardPrice,
        strikePrice,
        timeToExpiry,
        optionType
    );

    System.out.println("Implied Volatility: " + impliedVol);
} catch (BelowIntrinsicException e) {
    System.err.println("Price is below intrinsic value");
} catch (AboveMaximumException e) {
    System.err.println("Price exceeds maximum possible value");
}
```

## Dependencies

- Apache Commons Math 3 - for `FastMath` operations

## Implementation Details

### Black Function Evaluation

The implementation uses four distinct evaluation regimes for optimal accuracy:

1. **Region I** - Large negative h with small t: Asymptotic expansion (17 terms)
2. **Region II** - Small t: 12th order Taylor expansion
3. **Region III** - Large t: Direct CDF evaluation
4. **Region IV** - Elsewhere: Scaled complementary error function (erfcx)

### Error Functions

Implements W. J. Cody's rational Chebyshev approximations for:
- `erf(x)` - Error function
- `erfc(x)` - Complementary error function
- `erfcx(x)` - Scaled complementary error function: exp(x²) × erfc(x)

## Performance

- **Speed**: Approximately 0.2 microsecond per calculation
- **Accuracy**: Relative error < 10⁻¹⁵ (machine epsilon level)
- **Range**: Works for |x| from near 0 to -707 (IEEE 754 double precision limit)

## References

- **Paper**: "Let's Be Rational" by Peter Jäckel, March 25, 2016
- **Cody's Algorithm**: "Rational Chebyshev approximations for the error function" (Math. Comp., 1969, PP. 631-638)

## Notes

This algorithm is designed for **industrial applications** where:
- Black formula is used in analytical transformations
- Extreme parameter values may be encountered
- Machine-level precision is required
- Fast computation is essential
- Robustness is critical

It is NOT designed for simple trading desk approximations (where Brenner-Subrahmanyam or Corrado-Miller formulas may suffice).
