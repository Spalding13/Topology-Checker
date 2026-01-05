ESD Checks — Project README
===========================

Overview
--------
This repository is a small tooling environment for analyzing ESD (electrostatic discharge) protection circuits represented as CDL (Circuit Description Language) netlists. The project contains:

- A netlist parser and graph builder (nets, devices, pins).
- A reducer module with sequential and parallel reduction strategies for simplifying the graph.
- Rule-based ESD analyzers (structural and parametric rules) that operate on the reduced graph.
- A JavaFX GUI to visualize graphs and interact with the toolchain.
- A benchmarking harness to measure performance of parsing and both reducers over different netlist sizes.
- Python plotting utilities to visualize benchmark results.

This README documents the code layout, how pieces interact, how to run the main components, and common troubleshooting tips.

Repository layout
-----------------
Top-level directories and important files:

- `input/` — Example netlists used for testing and benchmarking. Several synthetic netlists are included (1k, 10k, 30k, 60k, 100k devices) and other variants.
- `libs/` — Java/3rd-party libraries (JavaFX distribution included for local GUI runtime).
- `src/` — Java source tree (package root `com.company`). Important submodules:
  - `com.company.netFactory` — creates `Net` model objects from parsed names.
  - `com.company.devicefactory` — creates `Device` objects and maps pins to nets.
  - `com.company.graph` — Graph data structure (Nodes, Connections) and GraphFactory which builds graphs from devices & nets.
  - `com.company.reducer` and `com.company.reducer_benchmark` — reduction algorithms (sequential & parallel) and the benchmarking harness.
  - `com.company.esd` — ESD rule definitions and the analyzer that applies them to graphs.
  - `com.company.gui` — JavaFX-based UI for loading netlists, visualizing graphs before/after reduction, and presenting rule results.
  - `com.company.Main` — example entry point(s) for CLI use.
- `plot_bench.py` — Python script to plot benchmark CSV results and render an image (requires matplotlib & numpy).
- `requirements.txt` — Python dependencies for plotting.
- `bench_results.csv` (generated) — Example benchmark output CSV produced by the harness.

High-level design
-----------------
1) Netlist input -> parsing -> device + net models
   - `NetlistReader` reads raw .cdl files.
   - `NetlistInterpreter` tokenizes/pars the netlist into a list of net names and device definition lines.
   - `NetFactory` and `DeviceFactory` transform parsed tokens into typed, structured objects used by the rest of the system.

2) Build graph
   - `GraphFactory` ties devices and nets into a `Graph` consisting of device nodes, net nodes, and connections (edges labeled with pin names).

3) Reduce / simplify
   - The project has two reducers:
     - `SequentialReducer` — single-threaded reduction implementation.
     - `ParallelReducer` — parallel reduction implementation designed to speed up large graphs.
   - Both reducers accept a `Graph` and return a reduced `Graph`.

4) Rules & analysis
   - `ESDAnalyzer` applies structural and parametric rules to the reduced graph and emits results.
   - GUI presents rule results in a table (RuleName, RuleDescription, Result).

5) Visualization
   - `com.company.gui` uses JavaFX to display the graph, allow basic interactions, and show rule results.
   - The plotting script `plot_bench.py` creates PNG visualizations for benchmark results.

The benchmark harness
---------------------
Location: `src/com/company/reducer_benchmark/BenchmarkHarness.java`

Purpose:
- Runs warm-up and measured runs for a list of input netlists.
- Measures three phases: parsing, sequential reduction, parallel reduction.
- Aggregates results (median and p90) and emits a CSV and a simple summary file.

Outputs:
- `bench_results.csv` — rows with columns: `inputFile, devices, parse_ms_median, parse_ms_p90, seq_ms_median, seq_ms_p90, par_ms_median, par_ms_p90`.
- `bench_results_summary.txt` — harness-level start/end/timing summary.

How the harness works (summary):
- Warm-up runs (not included in aggregates) to let the JVM JIT the hot paths.
- `MEASURE_RUNS` measured invocations: `runSingle(...)` which performs parse + seq reduce + par reduce and returns the three timings.
- Statistics computed per-file: median and p90.
- A simple progress logger prints percent complete, elapsed time and ETA.

How to run the benchmark (example)
----------------------------------
1) Build the project in your IDE (IntelliJ, Eclipse) or compile from command line (javac) using your project's build system.
2) Run the harness main located in `BenchmarkHarness.main(...)` or call `run(List<String> inputPaths, String outputCsvPath)` with a list of absolute netlist paths.

Example (PowerShell/command line, adapt classpath to your build):

```powershell
# JVM memory tuned for large netlists
java -Xmx4g -cp "out/production/ESD Checks;libs/*" com.company.reducer_benchmark.BenchmarkHarness
```

Note: Large netlists (30k+) may require more heap (increase `-Xmx`), or the run may be interrupted by GC or OOM.

Plotting
--------
`plot_bench.py` reads `bench_results.csv`, converts the compact `devices` label (e.g. "10k") to numeric counts for plotting, and draws median parse/seq/par timings versus the number of devices. The script can be customized to show the speedup (seq/par) or to render logs or exponential fits.

To run the plotting script (Python 3 + venv recommended):
```powershell
py -3 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python plot_bench.py
```

GUI
---
- The JavaFX GUI is in `src/com/company/gui`. It allows loading netlists, visualizing the graph (before and after reduction), and viewing rule results. The GUI uses a lightweight graph view implementation; for very large graphs the visualization may be slow.
- To run the GUI, configure your IDE's run configuration to add JavaFX's `lib` to the module path or VM options. The included `libs/javafx-sdk-25.0.1` provides a local JavaFX SDK — if you run from the command line, add `--module-path` and `--add-modules javafx.controls,javafx.fxml` flags.

Example VM options for running JavaFX from command line (Windows PowerShell):
```powershell
# Adjust the path to your javafx-sdk lib folder
--module-path "e:\ESD Checks\libs\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml
```

Interpreting benchmark results
-----------------------------
- Prefer medians over single-run values.
- `p90` indicates tail behavior: if `p90` is much larger than median, the run has unstable long tail (GC/IO spikes).
- Use `par_ms_median` vs `seq_ms_median` to evaluate parallel speedup; compute speedup = `seq_ms_median / par_ms_median`.
