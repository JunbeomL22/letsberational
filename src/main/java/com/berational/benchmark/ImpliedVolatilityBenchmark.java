package com.berational.benchmark;

import com.berational.BlackScholesNewtonRaphson;
import com.berational.LetsBeRational;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark comparing implied volatility computation methods:
 * - Let's Be Rational (Jäckel's algorithm)
 * - Plain vanilla Newton-Raphson
 *
 * Benchmark Setup:
 * - ATM, ITM, and OTM options
 * - Various volatility levels
 * - Both calls and puts
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ImpliedVolatilityBenchmark {

    // Market parameters
    private static final double F = 100.0;  // Forward price
    private static final double T = 1.0;    // Time to expiration (1 year)

    /**
     * State for ATM call option (K = F = 100, σ = 0.25)
     */
    @State(Scope.Thread)
    public static class ATMCallState {
        double K = 100.0;
        double sigma = 0.25;
        double price;
        int q = 1; // call

        @Setup
        public void setup() {
            // Compute true price at σ = 0.25
            price = computeBlackScholesPrice(F, K, T, sigma, q);
        }
    }

    /**
     * State for ITM call option (K = 90, σ = 0.30)
     */
    @State(Scope.Thread)
    public static class ITMCallState {
        double K = 90.0;
        double sigma = 0.30;
        double price;
        int q = 1; // call

        @Setup
        public void setup() {
            price = computeBlackScholesPrice(F, K, T, sigma, q);
        }
    }

    /**
     * State for OTM call option (K = 110, σ = 0.20)
     */
    @State(Scope.Thread)
    public static class OTMCallState {
        double K = 110.0;
        double sigma = 0.20;
        double price;
        int q = 1; // call

        @Setup
        public void setup() {
            price = computeBlackScholesPrice(F, K, T, sigma, q);
        }
    }

    /**
     * State for ATM put option (K = F = 100, σ = 0.25)
     */
    @State(Scope.Thread)
    public static class ATMPutState {
        double K = 100.0;
        double sigma = 0.25;
        double price;
        int q = -1; // put

        @Setup
        public void setup() {
            price = computeBlackScholesPrice(F, K, T, sigma, q);
        }
    }

    /**
     * State for high volatility option (σ = 0.80)
     */
    @State(Scope.Thread)
    public static class HighVolState {
        double K = 100.0;
        double sigma = 0.80;
        double price;
        int q = 1; // call

        @Setup
        public void setup() {
            price = computeBlackScholesPrice(F, K, T, sigma, q);
        }
    }

    /**
     * State for low volatility option (σ = 0.05)
     */
    @State(Scope.Thread)
    public static class LowVolState {
        double K = 100.0;
        double sigma = 0.05;
        double price;
        int q = 1; // call

        @Setup
        public void setup() {
            price = computeBlackScholesPrice(F, K, T, sigma, q);
        }
    }

    /**
     * Helper to compute Black-Scholes price using Let's Be Rational's forward method
     */
    private static double computeBlackScholesPrice(double F, double K, double T, double sigma, int q) {
        double x = Math.log(F / K);
        double s = sigma * Math.sqrt(T);
        double beta = LetsBeRational.normalisedBlackCall(x, s);

        if (q < 0) {
            // Put-call parity: P = C - F + K
            double intrinsic = Math.abs(Math.max((q < 0 ? -1.0 : 1.0) * (Math.exp(0.5 * x) - Math.exp(-0.5 * x)), 0.0));
            beta = beta - intrinsic;
        }

        return beta * Math.sqrt(F) * Math.sqrt(K);
    }

    // ========== Let's Be Rational Benchmarks ==========

    @Benchmark
    public void letsBeRational_ATM_Call(ATMCallState state, Blackhole bh) {
        double vol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void letsBeRational_ITM_Call(ITMCallState state, Blackhole bh) {
        double vol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void letsBeRational_OTM_Call(OTMCallState state, Blackhole bh) {
        double vol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void letsBeRational_ATM_Put(ATMPutState state, Blackhole bh) {
        double vol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void letsBeRational_HighVol(HighVolState state, Blackhole bh) {
        double vol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void letsBeRational_LowVol(LowVolState state, Blackhole bh) {
        double vol = LetsBeRational.impliedVolatilityFromATransformedRationalGuess(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    // ========== Newton-Raphson Benchmarks ==========

    @Benchmark
    public void newtonRaphson_ATM_Call(ATMCallState state, Blackhole bh) {
        double vol = BlackScholesNewtonRaphson.impliedVolatility(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void newtonRaphson_ITM_Call(ITMCallState state, Blackhole bh) {
        double vol = BlackScholesNewtonRaphson.impliedVolatility(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void newtonRaphson_OTM_Call(OTMCallState state, Blackhole bh) {
        double vol = BlackScholesNewtonRaphson.impliedVolatility(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void newtonRaphson_ATM_Put(ATMPutState state, Blackhole bh) {
        double vol = BlackScholesNewtonRaphson.impliedVolatility(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void newtonRaphson_HighVol(HighVolState state, Blackhole bh) {
        double vol = BlackScholesNewtonRaphson.impliedVolatility(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }

    @Benchmark
    public void newtonRaphson_LowVol(LowVolState state, Blackhole bh) {
        double vol = BlackScholesNewtonRaphson.impliedVolatility(
            state.price, F, state.K, T, state.q);
        bh.consume(vol);
    }
}
