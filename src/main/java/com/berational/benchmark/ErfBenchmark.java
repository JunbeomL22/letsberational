package com.berational.benchmark;

import com.berational.CodyErf;
import org.apache.commons.math3.special.Erf;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing Cody's erf implementation with Apache Commons Math.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Benchmark)
public class ErfBenchmark {

    @Param({"0.1", "0.5", "1.0", "2.0", "3.0", "5.0"})
    private double x;

    @Benchmark
    public double codyErf() {
        return CodyErf.erf(x);
    }

    @Benchmark
    public double apacheErf() {
        return Erf.erf(x);
    }

    @Benchmark
    public double codyErfc() {
        return CodyErf.erfc(x);
    }

    @Benchmark
    public double apacheErfc() {
        return Erf.erfc(x);
    }

    @Benchmark
    public double codyErfcx() {
        return CodyErf.erfcx(x);
    }
}
