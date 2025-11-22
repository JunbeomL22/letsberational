package com.berational.benchmark;

import com.berational.CodyNormDist;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing Cody-based Normal CDF with Apache Commons Math.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Benchmark)
public class NormalDistributionBenchmark {

    @Param({"-5.0", "-3.0", "-1.0", "0.0", "1.0", "3.0", "5.0"})
    private double x;

    private org.apache.commons.math3.distribution.NormalDistribution apacheNormal;

    @Setup
    public void setup() {
        apacheNormal = new org.apache.commons.math3.distribution.NormalDistribution(0.0, 1.0);
    }

    @Benchmark
    public double codyCdf() {
        return CodyNormDist.cdf(x);
    }

    @Benchmark
    public double apacheCdf() {
        return apacheNormal.cumulativeProbability(x);
    }

    @Benchmark
    public double codyCdfErf() {
        return CodyNormDist.cdfErf(x);
    }

    @Benchmark
    public double codyCdfErfc() {
        return CodyNormDist.cdfErfc(x);
    }

    @Benchmark
    public double codyPdf() {
        return CodyNormDist.pdf(x);
    }

    @Benchmark
    public double apachePdf() {
        return apacheNormal.density(x);
    }
}
