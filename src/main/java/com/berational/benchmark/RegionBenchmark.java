package com.berational.benchmark;

import com.berational.CodyErf;
import org.apache.commons.math3.special.Erf;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark testing performance across different regions of Cody's algorithm.
 * Region 1: |x| <= 0.46875
 * Region 2: 0.46875 < |x| <= 4.0
 * Region 3: |x| > 4.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Benchmark)
public class RegionBenchmark {

    // Region 1: Small values
    @State(Scope.Benchmark)
    public static class Region1State {
        @Param({"0.0", "0.1", "0.2", "0.3", "0.4", "0.46875"})
        double x;
    }

    // Region 2: Medium values
    @State(Scope.Benchmark)
    public static class Region2State {
        @Param({"0.5", "1.0", "2.0", "3.0", "4.0"})
        double x;
    }

    // Region 3: Large values
    @State(Scope.Benchmark)
    public static class Region3State {
        @Param({"5.0", "10.0", "15.0", "20.0"})
        double x;
    }

    @Benchmark
    public double codyRegion1(Region1State state) {
        return CodyErf.erf(state.x);
    }

    @Benchmark
    public double apacheRegion1(Region1State state) {
        return Erf.erf(state.x);
    }

    @Benchmark
    public double codyRegion2(Region2State state) {
        return CodyErf.erf(state.x);
    }

    @Benchmark
    public double apacheRegion2(Region2State state) {
        return Erf.erf(state.x);
    }

    @Benchmark
    public double codyRegion3(Region3State state) {
        return CodyErf.erf(state.x);
    }

    @Benchmark
    public double apacheRegion3(Region3State state) {
        return Erf.erf(state.x);
    }
}
