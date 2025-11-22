# Cody's Error Function Implementation

## Overview

This document describes W. J. Cody's implementation of the error function and related functions, based on his 1969 paper "Rational Chebyshev approximations for the error function" (Math. Comp., 1969, PP. 631-638).

The implementation provides three related functions:
- `erf(x)` - Error function
- `erfc(x)` - Complementary error function
- `erfcx(x)` - Scaled complementary error function: exp(x²) × erfc(x)

## Mathematical Background

### Error Function Definition

The error function is defined as:

```
erf(x) = (2/√π) ∫₀ˣ exp(-t²) dt
```

The complementary error function is:

```
erfc(x) = 1 - erf(x) = (2/√π) ∫ₓ^∞ exp(-t²) dt
```

### Relationship to Normal Distribution

The cumulative distribution function (CDF) of the standard normal distribution Φ(x) is related to the error function:

```
Φ(x) = (1/2)[1 + erf(x/√2)]
```

Alternatively, using the complementary error function:

```
Φ(x) = (1/2) × erfc(-x/√2)
```

For numerical stability, the recommended implementation is:

```python
def norm_cdf(x):
    """Cumulative distribution function of standard normal distribution"""
    if x >= 0:
        return 0.5 * (1 + erf_cody(x / sqrt(2)))
    else:
        return 0.5 * erfc_cody(-x / sqrt(2))
```

## Cody's Algorithm

The algorithm uses different approximation strategies depending on the argument range to maintain accuracy and numerical stability.

### Core Function: CALERF

The main computation is performed by `calerf(x, jint)` where:
- `jint = 0`: Compute erf(x)
- `jint = 1`: Compute erfc(x)
- `jint = 2`: Compute erfcx(x) = exp(x²) × erfc(x)

### Three-Region Approach

The algorithm divides the real line into three regions:

#### Region 1: |x| ≤ 0.46875

For small arguments, erf(x) is computed directly using a rational approximation:

```
erf(x) = x × P(x²) / Q(x²)
```

Where P and Q are polynomials:
- P(y) = A[4]×y⁴ + A[0]×y³ + A[1]×y² + A[2]×y + A[3]
- Q(y) = y⁴ + B[0]×y³ + B[1]×y² + B[2]×y + B[3]

**Coefficients:**
```python
A = (3.1611237438705656, 113.864154151050156, 377.485237685302021,
     3209.37758913846947, 0.185777706184603153)
B = (23.6012909523441209, 244.024637934444173,
     1282.61652607737228, 2844.23683343917062)
```

**Implementation Logic:**
1. If |x| ≤ XSMALL (1.11e-16), return x × (2/√π) to avoid underflow
2. Compute ysq = x²
3. Evaluate numerator: xnum = A[4]×ysq, then xnum = (xnum + A[i]) × ysq for i=0,1,2
4. Evaluate denominator: xden = ysq, then xden = (xden + B[i]) × ysq for i=0,1,2
5. Result = x × (xnum + A[3]) / (xden + B[3])

#### Region 2: 0.46875 < |x| ≤ 4.0

For moderate arguments, erfc(x) is computed using:

```
erfc(x) = exp(-x²) × R(x)
```

Where R(x) is a rational function:
- R(x) = (C[8]×x⁷ + C[0]×x⁶ + ... + C[7]) / (x⁷ + D[0]×x⁶ + ... + D[7])

**Coefficients:**
```python
C = (0.564188496988670089, 8.88314979438837594, 66.1191906371416295,
     298.635138197400131, 881.95222124176909, 1712.04761263407058,
     2051.07837782607147, 1230.33935479799725, 2.15311535474403846e-8)
D = (15.7449261107098347, 117.693950891312499, 537.181101862009858,
     1621.38957456669019, 3290.79923573345963, 4362.61909014324716,
     3439.36767414372164, 1230.33935480374942)
```

**Implementation Logic:**
1. Compute y = |x|
2. Evaluate numerator: xnum = C[8]×y, then xnum = (xnum + C[i]) × y for i=0..6
3. Evaluate denominator: xden = y, then xden = (xden + D[i]) × y for i=0..6
4. result = (xnum + C[7]) / (xden + D[7])
5. For erfc (jint ≠ 2), multiply by exp(-x²) using careful computation:
   - Compute ysq = ⌊16×y⌋/16 (rounded)
   - Compute del = (y - ysq) × (y + ysq)
   - result = exp(-ysq²) × exp(-del) × result

The splitting of x² into ysq² + del avoids catastrophic cancellation.

#### Region 3: |x| > 4.0

For large arguments, use asymptotic expansion:

```
erfc(x) ≈ (exp(-x²) / (x√π)) × [1 - 1/(2x²) + ...]
```

The rational approximation is:
- R(x) = (1/x²) × P(1/x²) / Q(1/x²)

**Coefficients:**
```python
P = (0.305326634961232344, 0.360344899949804439, 0.125781726111229246,
     0.0160837851487422766, 6.58749161529837803e-4, 0.0163153871373020978)
Q = (2.56852019228982242, 1.87295284992346047, 0.527905102951428412,
     0.0605183413124413191, 0.00233520497626869185)
```

**Implementation Logic:**
1. Handle edge cases:
   - If y ≥ XBIG (26.543) and jint ≠ 2, return 0
   - If y ≥ XMAX (2.53e307), return 0
   - If y ≥ XHUGE (6.71e7), return SQRPI/y
2. Compute ysq = 1/y²
3. Evaluate numerator: xnum = P[5]×ysq, then xnum = (xnum + P[i]) × ysq for i=0..3
4. Evaluate denominator: xden = ysq, then xden = (xden + Q[i]) × ysq for i=0..3
5. result = ysq × (xnum + P[4]) / (xden + Q[4])
6. result = (SQRPI - result) / y
7. If jint ≠ 2, multiply by exp(-x²) as in Region 2

### Handling Negative Arguments

After computing the result for |x|, adjust for negative x:

**For erf(x) [jint = 0]:**
```
erf(-x) = -erf(x)
```

**For erfc(x) [jint = 1]:**
```
erfc(-x) = 2 - erfc(x)
```

**For erfcx(x) [jint = 2]:**
If x < XNEG (-26.628), return XINF (overflow)
Otherwise:
```
erfcx(-x) = 2×exp(x²) - erfcx(x)
```

## Machine-Dependent Constants (IEEE Double Precision)

```python
XINF = 1.79e308      # Largest positive finite float
XNEG = -26.628       # Largest negative argument for erfcx
XSMALL = 1.11e-16    # Threshold for erf(x) ≈ 2x/√π
XBIG = 26.543        # Largest argument for erfc
XHUGE = 6.71e7       # Threshold for 1 - 1/(2x²) = 1
XMAX = 2.53e307      # Largest acceptable argument for erfcx
```

## Algorithm to Compute Normal CDF Using Cody's Functions

### Method 1: Using erf_cody

```python
from math import sqrt

def norm_cdf_erf(x):
    """
    Compute Φ(x) = CDF of standard normal distribution
    Using: Φ(x) = (1/2)[1 + erf(x/√2)]
    """
    SQRT2 = 1.4142135623730951  # √2
    return 0.5 * (1.0 + erf_cody(x / SQRT2))
```

### Method 2: Using erfc_cody (Better for Negative x)

```python
def norm_cdf_erfc(x):
    """
    Compute Φ(x) = CDF of standard normal distribution
    Using: Φ(x) = (1/2) × erfc(-x/√2)

    This method is more accurate for negative x values.
    """
    SQRT2 = 1.4142135623730951  # √2
    return 0.5 * erfc_cody(-x / SQRT2)
```

### Method 3: Branch for Optimal Accuracy

```python
def norm_cdf(x):
    """
    Compute Φ(x) = CDF of standard normal distribution

    Uses erf for x ≥ 0 and erfc for x < 0 to maintain
    maximum accuracy across the entire domain.
    """
    SQRT2 = 1.4142135623730951  # √2

    if x >= 0:
        # For non-negative x, use erf
        # Φ(x) = (1/2)[1 + erf(x/√2)]
        return 0.5 * (1.0 + erf_cody(x / SQRT2))
    else:
        # For negative x, use erfc to avoid cancellation
        # Φ(x) = (1/2) × erfc(-x/√2)
        return 0.5 * erfc_cody(-x / SQRT2)
```

### Inverse: Normal Quantile Function

To compute the inverse CDF (quantile function), you would need to use numerical root-finding methods on the CDF. Common approaches:

1. **Newton-Raphson iteration** using the PDF as the derivative
2. **Brent's method** for robust root-finding
3. **Rational approximations** for initial guesses (e.g., Acklam's method)

## Numerical Stability Considerations

### Why Split exp(-x²)?

The computation of exp(-x²) is split as:
```
exp(-x²) = exp(-ysq²) × exp(-del)
```
where:
```
ysq = ⌊16×|x|⌋/16
del = (|x| - ysq) × (|x| + ysq)
```

This ensures:
1. **Avoids overflow**: ysq² and del are both smaller than x²
2. **Maintains precision**: Uses the identity (a-b)(a+b) = a² - b² to compute x² - ysq² without catastrophic cancellation

### Threshold Selection

The threshold 0.46875 = 15/32 is chosen to:
- Minimize maximum relative error across regions
- Balance polynomial degrees for efficiency
- Ensure smooth transition between approximations

## Performance Characteristics

- **Accuracy**: 18+ significant decimal digits (IEEE double precision)
- **Range**: Full double precision range with proper overflow/underflow handling
- **Speed**: Rational function evaluation is significantly faster than Taylor series
- **Stability**: Carefully designed to avoid cancellation and overflow

## References

1. W. J. Cody, "Rational Chebyshev approximations for the error function", Mathematics of Computation, 1969, pp. 631-638
2. W. J. Cody, "Algorithm 715: SPECFUN - A portable FORTRAN package of special function routines and test drivers", ACM Transactions on Mathematical Software, 1993
3. Abramowitz and Stegun, "Handbook of Mathematical Functions", 1964

## Implementation in py_lets_be_rational

The Python implementation in `erf_cody.py` is a direct translation of Cody's FORTRAN code with:
- JIT compilation support via Numba (`@maybe_jit` decorator)
- Preservation of original algorithm structure and comments
- Machine constants optimized for IEEE 754 double precision
