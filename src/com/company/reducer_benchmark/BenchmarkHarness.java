package com.company.reducer_benchmark;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.netFactory.NetFactory;
import com.company.NetlistInterpreter;
import com.company.NetlistReader;
import com.company.reducer.ParallelReducer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Benchmark harness to run multiple netlist sizes with warmups and repeated runs.
 * Produces a CSV file with aggregated statistics (median and p90) and a raw CSV of all runs.
 */
public class BenchmarkHarness {

    private static final int WARMUP_RUNS = 2;
    private static final int MEASURE_RUNS = 7;

    public static void run(List<String> inputPaths, String outputCsvPath) {
        // Start harness timer (both wall-clock and monotonic)
        Instant startInstant = Instant.now();
        long harnessStartNano = System.nanoTime();

        // Defensive: if no inputs provided, skip running
        if (inputPaths == null || inputPaths.isEmpty()) {
            System.out.println("No input files provided to BenchmarkHarness.run(). Nothing to do.");
            return;
        }

        // CSV header: devices column will be a compact label (e.g. 1k, 10k)
        List<String> header = Arrays.asList("inputFile", "devices", "parse_ms_median", "parse_ms_p90",
                "seq_ms_median", "seq_ms_p90", "par_ms_median", "par_ms_p90");

        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputCsvPath))) {
            out.write(String.join(",", header));
            out.newLine();

            // Progress tracking: total number of runSingle invocations (warmups + measurements)
            int totalRuns = inputPaths.size() * (WARMUP_RUNS + MEASURE_RUNS);
            int[] completedRuns = {0};

            for (String path : inputPaths) {
                System.out.println("\n=== Benchmarking: " + path + " ===");

                // Warm-up
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    runSingle(path, false);
                    System.gc();
                    // update progress
                    completedRuns[0]++;
                    printProgress(completedRuns[0], totalRuns, harnessStartNano);
                }

                // Measure runs
                FileBenchmarkResult res = benchmarkFile(path, harnessStartNano, completedRuns, totalRuns);

                double parseMedian = res.parseMedian;
                double parseP90 = res.parseP90;
                double seqMedian = res.seqMedian;
                double seqP90 = res.seqP90;
                double parMedian = res.parMedian;
                double parP90 = res.parP90;

                // Derive a compact devices label (e.g. 1k, 10k) from the input filename
                String devicesLabel = extractSizeLabel(path);

                List<String> row = Arrays.asList(
                        path,
                        devicesLabel,
                        String.format(Locale.ROOT, "%.3f", parseMedian),
                        String.format(Locale.ROOT, "%.3f", parseP90),
                        String.format(Locale.ROOT, "%.3f", seqMedian),
                        String.format(Locale.ROOT, "%.3f", seqP90),
                        String.format(Locale.ROOT, "%.3f", parMedian),
                        String.format(Locale.ROOT, "%.3f", parP90)
                );

                out.write(String.join(",", row));
                out.newLine();
                out.flush();

                System.out.println(String.join(" | ", row));
            }

            System.out.println("\nCSV written to: " + outputCsvPath);

        } catch (IOException e) {
            System.err.println("I/O error while writing CSV: " + e.getMessage());
            return;
        }

        // End harness timer and report
        Instant endInstant = Instant.now();
        long harnessEndNano = System.nanoTime();
        double totalMs = (harnessEndNano - harnessStartNano) / 1_000_000.0;

        System.out.printf("\n=== Harness completed ===\nTotal wall-clock time: %s to %s%n",
                LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.printf("Total elapsed: %,.3f ms (%,.3f s)%n", totalMs, totalMs / 1000.0);

        // Write a small summary file next to the CSV
        try {
            String summary = String.format(Locale.ROOT,
                    "Harness start: %s%nHarness end:   %s%nTotal elapsed (ms): %,.3f%nFiles processed: %d%n",
                    LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    totalMs,
                    inputPaths == null ? 0 : inputPaths.size());

            Files.writeString(Paths.get(outputCsvPath).resolveSibling("bench_results_summary.txt"), summary);
            System.out.println("Wrote summary to: " + Paths.get(outputCsvPath).resolveSibling("bench_results_summary.txt").toString());
        } catch (IOException e) {
            System.err.println("Failed to write harness summary: " + e.getMessage());
        }
    }

    private static RunResult runSingle(String inputPath, boolean verbose) {
        try {
            long parseStart = System.nanoTime();

            String input = NetlistReader.openFile(inputPath);
            if (input == null) throw new IOException("Failed to read input file: " + inputPath);
            NetlistInterpreter interp = new NetlistInterpreter();
            Map<String, List<String>> info = interp.parseNetlist(input);

            NetFactory netFactory = new NetFactory();
            // Create nets in factory (return value intentionally ignored)
            netFactory.createNets(info.get("nets"));
            List<Device> devices = DeviceFactory.createDevicesFromLines(info.get("devices"));

            Graph graph = GraphFactory.buildGraph(devices, netFactory.getNetMap());

            long parseEnd = System.nanoTime();
            double parseMs = (parseEnd - parseStart) / 1_000_000.0;

            // Sequential
            long s1 = System.nanoTime();
            Graph seq = SequentialReducer.reduce(graph.deepCopy(graph.getNetMap()));
            long s2 = System.nanoTime();
            double seqMs = (s2 - s1) / 1_000_000.0;

            // Parallel
            long p1 = System.nanoTime();
            Graph par = ParallelReducer.reduce(graph.deepCopy(graph.getNetMap()));
            long p2 = System.nanoTime();
            double parMs = (p2 - p1) / 1_000_000.0;

            if (verbose) {
                System.out.printf("parse=%.3fms seq=%.3fms par=%.3fms\n", parseMs, seqMs, parMs);
            }

            return new RunResult(parseMs, seqMs, parMs);

        } catch (Exception e) {
            System.err.println("Error processing " + inputPath + ": " + e.getMessage());
            return new RunResult(0,0,0);
        }
    }

    private static double median(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        List<Double> s = new ArrayList<>(values);
        Collections.sort(s);
        int m = s.size() / 2;
        if (s.size() % 2 == 1) return s.get(m);
        return (s.get(m-1) + s.get(m)) / 2.0;
    }

    private static double percentile(List<Double> values, double p) {
        if (values.isEmpty()) return 0.0;
        List<Double> s = new ArrayList<>(values);
        Collections.sort(s);
        double rank = (p / 100.0) * (s.size() - 1);
        int low = (int)Math.floor(rank);
        int high = (int)Math.ceil(rank);
        if (low == high) return s.get(low);
        double frac = rank - low;
        return s.get(low) * (1 - frac) + s.get(high) * frac;
    }

    // Print progress: percent complete, elapsed and simple ETA
    private static void printProgress(int completed, int total, long harnessStartNano) {
        if (total <= 0) return;
        double frac = completed / (double) total;
        double percent = frac * 100.0;
        long elapsedNano = System.nanoTime() - harnessStartNano;
        double elapsedSec = elapsedNano / 1_000_000_000.0;
        String etaStr = "N/A";
        if (frac > 0.0) {
            double estTotalSec = elapsedSec / frac;
            double remSec = Math.max(0.0, estTotalSec - elapsedSec);
            etaStr = String.format(Locale.ROOT, "ETA: %,.1fs", remSec);
        }
        System.out.printf(Locale.ROOT, "[Progress] %d/%d (%.1f%%) Elapsed: %,.1fs %s\n", completed, total, percent, elapsedSec, etaStr);
    }

    private static class RunResult {
        double parseMs;
        double seqMs;
        double parMs;

        RunResult(double parseMs, double seqMs, double parMs) {
            this.parseMs = parseMs;
            this.seqMs = seqMs;
            this.parMs = parMs;
        }
    }

    private static class FileBenchmarkResult {
        double parseMedian;
        double parseP90;
        double seqMedian;
        double seqP90;
        double parMedian;
        double parP90;

        FileBenchmarkResult(double parseMedian, double parseP90, double seqMedian, double seqP90, double parMedian, double parP90) {
            this.parseMedian = parseMedian;
            this.parseP90 = parseP90;
            this.seqMedian = seqMedian;
            this.seqP90 = seqP90;
            this.parMedian = parMedian;
            this.parP90 = parP90;
        }
    }

    private static FileBenchmarkResult benchmarkFile(String path, long harnessStartNano, int[] completedRuns, int totalRuns) {
        List<Double> parseMs = new ArrayList<>();
        List<Double> seqMs = new ArrayList<>();
        List<Double> parMs = new ArrayList<>();

        // Measure runs
        for (int r = 0; r < MEASURE_RUNS; r++) {
            RunResult res = runSingle(path, true);
            parseMs.add(res.parseMs);
            seqMs.add(res.seqMs);
            parMs.add(res.parMs);
            System.gc();

            // update progress
            completedRuns[0]++;
            printProgress(completedRuns[0], totalRuns, harnessStartNano);
        }

        double parseMedian = median(parseMs);
        double parseP90 = percentile(parseMs, 90);
        double seqMedian = median(seqMs);
        double seqP90 = percentile(seqMs, 90);
        double parMedian = median(parMs);
        double parP90 = percentile(parMs, 90);

        return new FileBenchmarkResult(parseMedian, parseP90, seqMedian, seqP90, parMedian, parP90);
    }

    // Quick runner for manual testing
    public static void main(String[] args) {
        List<String> inputs = Arrays.asList(
                "E:\\ESD Checks\\input\\netlist_1k.cdl",
                "E:\\ESD Checks\\input\\netlist_10k.cdl",
                "E:\\ESD Checks\\input\\netlist_30k.cdl",
                "E:\\ESD Checks\\input\\netlist_60k.cdl",
                "E:\\ESD Checks\\input\\netlist_100k.cdl"
        );

        run(inputs, "e:\\ESD Checks\\bench_results.csv");
    }

    // Helper: attempt to extract a compact device-size label from the input path or filename.
    // Examples: 'netlist_1k.cdl' -> '1k', 'netlist_100000.cdl' -> '100k'
    private static String extractSizeLabel(String inputPath) {
        if (inputPath == null || inputPath.isEmpty()) return "";
        String name = Paths.get(inputPath).getFileName().toString().toLowerCase(Locale.ROOT);

        // First try to find a token like '1k', '10k', '100k'
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+k)").matcher(name);
        if (m.find()) return m.group(1);

        // Otherwise try to find a numeric token and convert to k if appropriate
        m = java.util.regex.Pattern.compile("(\\d+)").matcher(name);
        if (m.find()) {
            try {
                int v = Integer.parseInt(m.group(1));
                if (v >= 1000 && v % 1000 == 0) return (v / 1000) + "k";
                if (v >= 1000) return String.valueOf(v / 1000) + "k"; // approximate
                return String.valueOf(v);
            } catch (NumberFormatException ignored) {
            }
        }
        return "";
    }
}
