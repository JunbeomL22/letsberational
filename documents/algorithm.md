# Let's Be Rational - Black Implied Volatility Algorithm

**Author:** Peter Jäckel
**First Version:** November 24, 2013
**This Version:** March 25, 2016

## Abstract

This document describes an algorithm for computing Black's implied volatility from option prices with as little as two iterations to maximum attainable precision on standard 64-bit floating point hardware for all possible inputs.

### Key Features

- **Convergence:** Two iterations to machine precision for all inputs
- **Method:** Fourth-order Householder iteration (third-order method)
- **Initial Guess:** Four rational function branches adapted to log-moneyness
- **Accuracy:** Relies on highly accurate Black function implementation

## 1. Introduction

The algorithm improves upon previous methods by:

1. Using four segments for the initial guess function with rational approximations
2. Employing non-linear transformations for correct asymptotic behavior
3. Defining three branches for the objective function
4. Using two iterations of the third-order Householder method (convergence order four)

### The Black Formula

The standard Black formula for options is:

```
B(F, K, σ̂, T, θ) = θ · [F · Φ(θ · [ln(F/K)/(σ̂√T) + σ̂√T/2])
                      - K · Φ(θ · [ln(F/K)/(σ̂√T) - σ̂√T/2])]
```

where:
- θ = 1 for call options
- θ = -1 for put options
- F = forward price
- K = strike price
- σ̂ = volatility
- T = time to expiration
- Φ(·) = cumulative normal distribution

## 2. Preliminaries

### Normalized Form

The algorithm works with normalized variables:

```
x := ln(F/K)
σ := σ̂√T
b(x, σ, θ) := B(F, K, σ̂, T, θ) / √(FK)
```

The normalized Black function is:

```
b(x, σ, θ) = θ · [e^(x/2) · Φ(θ[x/σ + σ/2]) - e^(-x/2) · Φ(θ[x/σ - σ/2])]
```

### Invariances

**Reciprocal-strike-put-call invariance:**
```
b(x, σ, θ) = b(-x, σ, -θ)
```

**Time-value-put-call invariance:**
```
b(x, σ, θ) - ι(x, θ) = b(x, σ, -θ) - ι(x, -θ)
```

where:
```
ι(x, θ) := (bmax - b^(-1)max · θx)+
bmax := e^(θx/2)
```

### Simplification

Without loss of generality, we only consider **out-of-the-money call options** (θ = +1 and x ≤ 0), giving bounds:

```
0 ≤ b ≤ bmax ≤ 1
bmax|θ=1 = e^(x/2)
```

## 3. Asymptotics

### Small Volatility Limit (σ → 0)

```
lim(σ→0) b ≈ (2π|x|)/(3√3) · Φ(-|x|/(√3·σ))³
```

This is **invertible** and asymptotically correct to first order.

### Large Volatility Limit (σ → ∞)

```
lim(σ→∞) b ≈ bmax - 2Φ(-σ/2)
```

Also **invertible** and asymptotically correct to first order.

## 4. The Initial Guess (Four Branches)

### Branch Points

The normalized Black function has a single inflection point at:

```
σc = √(2|x|)
bc = b(x, σc)
```

We define four zones using:
- **σl:** lower limit from tangent at inflection point
- **σc:** inflection point
- **σu:** upper limit from tangent at inflection point

These translate to price zones:
1. **[0, bl)** - very low prices
2. **[bl, bc]** - center-left
3. **(bc, bu]** - center-right
4. **(bu, bmax)** - very high prices

### Branch Definitions

#### Lower Branch: β ∈ [0, bl)

Uses non-linear transformation based on small σ asymptotics:

```
fl(β) := (2π|x|)/(3√3) · Φ(z)³  where z := -|x|/(√3·σ(β))
```

Approximate fl(β) with rational cubic interpolation f^rc_l(β), then solve:

```
σ0(β)|β∈[0,bl) = |x/√3 · Φ^(-1)(√3 · ∛(f^rc_l(β)/(2π|x|)))^(-1)|
```

#### Center-Left Branch: β ∈ [bl, bc]

Rational cubic interpolation matching:
- Levels and slopes at bl and bc
- Second derivative at bc (which is 0 at inflection point)

```
σ0(β)|β∈[bl,bc] = f^rc_cl(β)
```

#### Center-Right Branch: β ∈ (bc, bu]

Similar rational cubic interpolation:

```
σ0(β)|β∈(bc,bu] = f^rc_cr(β)
```

#### Upper Branch: β ∈ (bu, bmax)

Uses non-linear transformation based on large σ asymptotics:

```
fu(β) := Φ(-σ(β)/2)
```

Approximate with rational cubic interpolation, then solve:

```
σ0(β)|β∈(bu,bmax) = -2 · Φ^(-1)(f^rc_u(β))
```

## 5. Rational Iteration

### Objective Function (Three Branches)

```
g(σ) = {
  1/ln(b(σ)) - 1/ln(β)           for β ∈ [0, bl)
  b(σ) - β                        for β ∈ [bl, b̃u]
  ln((bmax-β)/(bmax-b(σ)))        for β ∈ (b̃u, bmax)
}
```

where `b̃u := max(bu, bmax/2)`

### Householder(3) Iteration Method

The third-order Householder method (fourth-order convergence):

```
σn+1 = σn + νn · (1 + ½γnνn) / (1 + νn(γn + ⅙δnνn))
```

where:
```
νn := -g(σn)/g'(σn)
γn := g''(σn)/g'(σn)
δn := g'''(σn)/g'(σn)
```

**Two iterations** of this method achieve maximum attainable precision.

## 6. The Black Function Implementation

Critical for accuracy is a highly accurate Black function that minimizes:
- Round-off errors
- Numerical truncations
- Subtractive cancellation

### Four Evaluation Regimes

For h = x/σ and t = σ/2:

#### Region I: Large negative h with small t
- Condition: `(|h| > 10) AND (t < |h| - 10 + 0.21)`
- Method: Series approximation of Y(z) = Φ(z)/ϕ(z) with n=17 terms
- Form: Substitute into `b = (1/√2π)·e^(-½(h²+t²))·[Y(h+t) - Y(h-t)]`

#### Region II: Small t
- Condition: `t < 0.21`
- Method: 12th order Taylor expansion in t of `[Y(h+t) - Y(h-t)]`
- Avoids subtractive cancellation when σ is very small

#### Region III: Large t
- Condition: `t > 0.85 + |h|`
- Method: Direct evaluation as `b = Φ(h+t)·e^(ht) - Φ(h-t)·e^(-ht)`
- Uses Cody's erfc() with round-off limiting technique

#### Region IV: Everywhere else
- Method: Use formulation with erfcx() (scaled complementary error function)
- Relationship: `Y(z) = ½·√(2π)·erfcx(-z/√2)`
- Minimizes exponential function evaluations

### Key Special Functions

**Scaled complementary error function:**
```
erfcx(x) = e^(x²) · erfc(x)
```

**Relationship to cumulative normal:**
```
Φ(z) = ½ · erfc(-z/√2)
```

Implementation uses **Cody's rational approximations** [Cod69, Cod90].

## 7. Algorithm Summary

### Complete Algorithm

```
Input: Normalized price β, log-moneyness x
Output: Implied volatility σ

1. Compute branch points:
   σc = √(2|x|)
   bc = b(x, σc)
   σl, σu from tangent at (σc, bc)
   bl = b(x, σl), bu = b(x, σu)

2. Determine initial guess σ0(β) from four branches:
   - Lower: β ∈ [0, bl) → transform via Φ^(-1)
   - Center-left: β ∈ [bl, bc] → rational cubic
   - Center-right: β ∈ (bc, bu] → rational cubic
   - Upper: β ∈ (bu, bmax) → transform via Φ^(-1)

3. Select objective function g(σ):
   - Lower branch: 1/ln(b) - 1/ln(β)
   - Middle branch: b - β
   - Upper branch: ln((bmax-β)/(bmax-b))

4. Iterate twice with Householder(3):
   For n = 0, 1:
     νn = -g(σn)/g'(σn)
     γn = g''(σn)/g'(σn)
     δn = g'''(σn)/g'(σn)
     σn+1 = σn + νn·(1 + ½γnνn)/(1 + νn(γn + ⅙δnνn))

5. Return σ2
```

## 8. Numerical Results

### Accuracy
- **Relative error:** < 10^(-15) for all inputs (machine epsilon level)
- **Iterations:** Exactly 2 iterations for all possible inputs
- **Range:** Works for |x| from near 0 to -707 (hardware limit)

### Performance
- **Speed:** ~1 microsecond per calculation
- **Improvement:** 5× faster than previous "By Implication" method at high accuracy
- **Comparison:** Much better convergence than Newton-Raphson or Halley's method

## Key Advantages

1. **Universal Precision:** Two iterations achieve machine accuracy for ALL inputs
2. **Rational Approximations:** Fast evaluation, excellent numerical properties
3. **Correct Asymptotics:** Initial guess matches first-order behavior at extremes
4. **Smooth Black Function:** Four evaluation regimes minimize numerical noise
5. **Industrial Strength:** Handles extreme parameter values encountered in practice

## References

- [Jäc06] "By Implication", Wilmott Magazine, 2006
- [Vog07] Vogt - improvement via Lambert W function
- [Cod69, Cod90] Cody's rational approximations for erf/erfc
- [DG85] Delbourgo-Gregory rational cubic interpolation
- [Hou70] Householder's method for nonlinear equations

## Notes

The algorithm is designed for **industrial applications** where:
- Black formula is used in analytical transformations
- Extreme parameter values may be encountered
- Machine-level precision is required
- Fast computation is essential
- Robustness is critical

It is NOT designed for simple trading desk approximations (where Brenner-Subrahmanyam or Corrado-Miller formulas suffice).
