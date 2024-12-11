package org.fao.geonet.domain;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ISODateBenchmark {
    @Param({"1976-06-03", "1976/06/03", "24-06-06"})
    public String arg;

    @Benchmark
    public void measureIsoSimple(Blackhole bh) {
        ISODate isoDate = new ISODate(arg);
        bh.consume(isoDate);
    }
}
