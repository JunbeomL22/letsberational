package com.berational.benchmark;

import com.berational.NormalDistribution;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing NormalDistribution.cdf with Apache Commons Math.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Benchmark)
public class NormalCdfComparisonBenchmark {

    @Param({"-10.0", "-7.5", "-5.0", "-4.0", "-3.0", "-2.0", "-1.0", "0.0", "1.0", "2.0", "3.0", "4.0", "5.0", "7.5", "10.0"})
    private double x;

    private org.apache.commons.math3.distribution.NormalDistribution apacheNormal;

    @Setup
    public void setup() {
        apacheNormal = new org.apache.commons.math3.distribution.NormalDistribution(0.0, 1.0);
    }

    @Benchmark
    public double beRationalCdf() {
        return NormalDistribution.cdf(x);
    }

    @Benchmark
    public double apacheCdf() {
        return apacheNormal.cumulativeProbability(x);
    }
}
