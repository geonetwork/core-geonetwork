# Geonetwork JMH Benchmark Suite

This module contains micro benchmarks for some GN functions.
It is used to validate the performance of individual functions and snippets of code.
For performance tests at a larger scale, consider using another tool like jmeter

To get started using JMH, see the [JMH docs](https://github.com/openjdk/jmh)

## Adding new benchmarks

New benchmark can be added by simply
1. Adding the module of the class to test to the gn-jmh module
2. Writing a benchmark in this module

## Running the benchmarks

1. Make sure the `jmh` profile is enabled (or enable it using `-Pjmh` when running maven)
2. Run `mvn verify` to build the benchmarks
3. Run the benchmark using `java -jar jmh/target/benchmarks.jar` in this module

If you want to get additional inside, you can append `--prof stack`, which outputs text-base stack sampling, or `--prof jfr` to get java flight recorder profile that can be read by applications like VisualVM.
Make sure to only use additional profilers when analysing the data, not when actually recording numbers.
