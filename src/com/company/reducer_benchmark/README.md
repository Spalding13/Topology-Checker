Benchmark harness (reducer_benchmark)
===================================

This README explains what the benchmark harness does, how it works, how to run it, and how to interpret its outputs.

Overview
--------
The benchmark harness measures performance of your netlist parsing and reducer implementations. For each input netlist file it:

- Performs a small number of warm-up runs (JVM warming / JIT) to reduce variance.
- Runs the sequential and parallel reducers several times and records durations.
- Computes aggregated statistics (median and p90) for the measured phases.
- Writes a CSV summarizing results and a small human-readable summary file.

Key measured phases
-------------------
- parse: time to read the input file, parse it with the project's `NetlistInterpreter`, create `Net` and `Device` objects and build the `Graph` via `GraphFactory`.
- seq: time spent by the sequential reducer (calls `SequentialReducer.reduce(...)`) on a deep copy of the graph.
- par: time spent by the parallel reducer (calls `ParallelReducer.reduce(...)`) on a deep copy of the graph.

Why warm-up and multiple runs?
------------------------------
- The JVM performs JIT compilation and runtime optimizations; the first few runs are usually slower.
- Running several iterations and taking medians reduces noise from scheduling, GC and other system activity.

Files the harness produces
--------------------------
1) bench_results.csv (written next to the provided output CSV path)
   - CSV header (current):
     inputFile,devices,parse_ms_median,parse_ms_p90,seq_ms_median,seq_ms_p90,par_ms_median,par_ms_p90

   - Column meanings:
     - inputFile: absolute path to the netlist used.
     - devices: compact label for the number of devices in the test (e.g. "1k", "10k").
     - parse_ms_median: median parsing time (ms).
     - parse_ms_p90: 90th percentile parsing time (ms).
     - seq_ms_median: median sequential reducer time (ms).
     - seq_ms_p90: 90th percentile sequential reducer time (ms).
     - par_ms_median: median parallel reducer time (ms).
     - par_ms_p90: 90th percentile parallel reducer time (ms).

2) bench_results_summary.txt
   - A concise summary with harness start/end timestamps, total elapsed time (ms) and how many files were processed.

Console output
--------------
- The harness prints a simple progress line after each individual run (warm-up and measured), for example:
  [Progress] 3/45 (6.7%) Elapsed: 12.3s ETA: 173.4s
- It also prints per-file aggregated CSV rows and a final harness summary.

How the harness is organized (high level)
----------------------------------------
- `run(List<String> inputPaths, String outputCsvPath)` — main orchestration:
  - Writes CSV header
  - Iterates each input file:
    - Does warm-up runs (not recorded in final aggregates)
    - Calls `benchmarkFile(...)` to run the measured iterations and compute medians/p90
    - Writes a CSV row with compact devices label and statistics
  - Writes a `bench_results_summary.txt` with harness timing

- `runSingle(...)` — does a single parse + sequential reduce + parallel reduce and returns the three timings (ms). This method is what `benchmarkFile` invokes multiple times.

- `benchmarkFile(...)` — runs MEASURE_RUNS invocations of `runSingle(...)`, collects timings and computes medians/p90.

- `printProgress(...)` — prints percent complete, elapsed time and ETA to stdout.

How to run the harness
----------------------
- Recommended: run from your IDE using the `BenchmarkHarness` main method (class `com.company.reducer_benchmark.BenchmarkHarness`).

- Example (PowerShell) — adapt classpath to your build output / jars:

```powershell
# Example: adjust classpath to where your compiled classes are located
# Increase heap for large netlists
java -Xmx4g -cp "out/production/ESD Checks;libs/*" com.company.reducer_benchmark.BenchmarkHarness
```

- The harness has a `main(...)` that includes example input paths; you can edit it or call `run(...)` programmatically from other code.

Plotting results
----------------
- There is a small Python script (`plot_bench.py`) included in the project root which reads `bench_results.csv` and produces a PNG plot. The script expects the compact `devices` column and converts it to numeric device counts for plotting.
- Ensure Python + matplotlib + numpy are available (or use the project's `.venv`), then run:

```powershell
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python plot_bench.py
```

Interpreting the results and tips
--------------------------------
- Use the median (`*_ms_median`) as the primary metric.
- Check `*_ms_p90` to understand high tail latency.
- For large netlists (30k, 60k, 100k) increase JVM heap (e.g. `-Xmx4g`) to avoid frequent GC or OOM.
- Run the harness multiple times (or increase MEASURE_RUNS) and compare medians to be confident in trends.
- To observe scaling behavior, run the harness with a sequence of inputs (1k, 10k, 30k, 60k, 100k) and plot median times. The plotting script can draw exponential fit lines to visualize growth.

Recommendations for further improvements
---------------------------------------
- Replace `System.out` logging with a small logging framework (SLF4J / java.util.logging) for better control.
- Optionally export a processed CSV that includes `speedup = seq_ms_median / par_ms_median` for easier plotting.
- Add CLI flags to control warmup and measure run counts, output paths and whether to skip parsing, reduce-only measurements, etc.
- Consider using JMH for microbenchmarks if you need very rigorous measurements on hot code paths.

Contact / next steps
--------------------
If you'd like, I can:
- Add a processed CSV export (including `Ускорение`/speedup).
- Add CLI parsing to configure runs and target files.
- Replace the console progress with a single-line progress bar.



