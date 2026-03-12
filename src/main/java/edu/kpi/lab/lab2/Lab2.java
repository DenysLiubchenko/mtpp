package edu.kpi.lab.lab2;

import edu.kpi.lab.lab2.arrayStatistics.ArrayStatistics;
import edu.kpi.lab.lab2.arrayStatistics.ArrayStatisticsResult;
import edu.kpi.lab.lab2.matrixMultiplication.MatrixMultiplier;
import edu.kpi.lab.lab2.tagFrequency.TagFrequencyStatistics;
import edu.kpi.lab.lab2.transactionProcessing.AggregationResult;
import edu.kpi.lab.lab2.transactionProcessing.AggregationStage;
import edu.kpi.lab.lab2.transactionProcessing.SequentialProcessor;
import edu.kpi.lab.lab2.transactionProcessing.Transaction;
import edu.kpi.lab.lab2.transactionProcessing.TransactionPipeline;
import edu.kpi.lab.lab2.utils.HtmlDocsGenerator;
import edu.kpi.lab.lab2.utils.IntegerArrayGenerator;
import edu.kpi.lab.lab2.utils.MatrixGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Lab2 {

  private static final Path HTML_DOCS_DIR =
    Path.of("src/main/resources/html_docs");

  private static void printTableHeader() {
    System.out.println("  Approach       | Time (ms)    | Speedup    | Efficiency");
    System.out.println("  ---------------|--------------|------------|-------------");
  }

  private static void printRow(String approach, long timeMs, long baseTimeMs) {
    double speedup = baseTimeMs > 0 ? (double) baseTimeMs / timeMs : 1.0;
    double efficiency = speedup / 4 * 100;
    System.out.printf("  %-15s| %-12d | %-10.2f | %-12.1f%%%n",
      approach, timeMs, speedup, efficiency);
  }

  private static void benchmarkTagFrequency() throws Exception {
    System.out.println("\n[TASK 1] Tag Frequency (1,200 HTML documents)");
    printTableHeader();

    TagFrequencyStatistics counter = new TagFrequencyStatistics();
    long baseTime = -1;

    long t0 = System.currentTimeMillis();
    Map<String, Long> seqResult = counter.sequential(HTML_DOCS_DIR);
    long seqTime = System.currentTimeMillis() - t0;
    if (baseTime < 0) {
      baseTime = seqTime;
    }
    printRow("Sequential", seqTime, baseTime);

    t0 = System.currentTimeMillis();
    Map<String, Long> mrResult = counter.mapReduce(HTML_DOCS_DIR);
    long mrTime = System.currentTimeMillis() - t0;
    printRow("MapReduce", mrTime, baseTime);

    t0 = System.currentTimeMillis();
    Map<String, Long> fjResult = counter.forkJoin(HTML_DOCS_DIR);
    long fjTime = System.currentTimeMillis() - t0;
    printRow("ForkJoin", fjTime, baseTime);

    t0 = System.currentTimeMillis();
    Map<String, Long> wpResult = counter.workerPool(HTML_DOCS_DIR);
    long wpTime = System.currentTimeMillis() - t0;
    printRow("WorkerPool", wpTime, baseTime);

    System.out.println("\n  Top 10 tags (sequential result):");
    seqResult.entrySet().stream()
      .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
      .limit(10)
      .forEach(e -> System.out.printf("    %-15s %,d%n", e.getKey(), e.getValue()));
    System.out.printf("    ... (%d unique tags total)%n", seqResult.size());
  }

  private static void benchmarkArrayStatistics(int[] array) {
    System.out.println("\n[TASK 2] Array Statistics (" + String.format("%,d", array.length) + " elements)");
    printTableHeader();

    long baseTime = -1;

    long t0 = System.currentTimeMillis();
    ArrayStatisticsResult seqResult = ArrayStatistics.sequential(array);
    long seqTime = System.currentTimeMillis() - t0;
    if (baseTime < 0) {
      baseTime = seqTime;
    }
    printRow("Sequential", seqTime, baseTime);

    t0 = System.currentTimeMillis();
    ArrayStatisticsResult mrResult = ArrayStatistics.mapReduce(array);
    long mrTime = System.currentTimeMillis() - t0;
    printRow("MapReduce", mrTime, baseTime);

    t0 = System.currentTimeMillis();
    ArrayStatisticsResult fjResult = ArrayStatistics.forkJoin(array);
    long fjTime = System.currentTimeMillis() - t0;
    printRow("ForkJoin", fjTime, baseTime);

    t0 = System.currentTimeMillis();
    ArrayStatisticsResult wpResult = ArrayStatistics.workerPool(array);
    long wpTime = System.currentTimeMillis() - t0;
    printRow("WorkerPool", wpTime, baseTime);

    System.out.printf("%n  Result: min=%-10d max=%-10d mean=%-15.2f median=%.1f%n",
      seqResult.min(), seqResult.max(), seqResult.mean(), seqResult.median());
  }

  private static void benchmarkMatrixMultiplication(int[][] a, int[][] b) throws Exception {
    System.out.printf("%n[TASK 3] Matrix Multiplication (%d × %d)%n", a.length, a[0].length);
    printTableHeader();

    long baseTime = -1;

    long t0 = System.currentTimeMillis();
    long[][] seqResult = MatrixMultiplier.sequential(a, b);
    long seqTime = System.currentTimeMillis() - t0;
    if (baseTime < 0) {
      baseTime = seqTime;
    }
    printRow("Sequential", seqTime, baseTime);

    t0 = System.currentTimeMillis();
    long[][] mrResult = MatrixMultiplier.mapReduce(a, b);
    long mrTime = System.currentTimeMillis() - t0;
    printRow("MapReduce", mrTime, baseTime);

    t0 = System.currentTimeMillis();
    long[][] fjResult = MatrixMultiplier.forkJoin(a, b);
    long fjTime = System.currentTimeMillis() - t0;
    printRow("ForkJoin", fjTime, baseTime);

    t0 = System.currentTimeMillis();
    long[][] wpResult = MatrixMultiplier.workerPool(a, b);
    long wpTime = System.currentTimeMillis() - t0;
    printRow("WorkerPool", wpTime, baseTime);

    printMatrix(seqResult, "Result");
  }

  private static void benchmarkTransactionProcessing() throws Exception {
    int totalTransactions = 100_000;
    System.out.printf("%n[TASK 4] Transaction Processing (%,d transactions)%n", totalTransactions);
    printTableHeader();

    List<Transaction> dataset = SequentialProcessor.generate(totalTransactions);
    long baseTime = -1;

    long t0 = System.currentTimeMillis();
    AggregationResult seqResult = SequentialProcessor.process(dataset);
    long seqTime = System.currentTimeMillis() - t0;
    if (baseTime < 0) {
      baseTime = seqTime;
    }
    printRow("Sequential", seqTime, baseTime);

    t0 = System.currentTimeMillis();
    AggregationStage pipeResult = TransactionPipeline.runPipeline(dataset);
    long pipeTime = System.currentTimeMillis() - t0;
    printRow("Pipeline", pipeTime, baseTime);

    System.out.printf("%n  Transactions processed : %,d%n", seqResult.txCount);
    System.out.printf("  Total amount (USD)     : %,.2f%n", seqResult.totalUSD);
    System.out.printf("  Total cashback (USD)   : %,.2f%n", seqResult.totalCashback);
    System.out.printf("  Net amount (USD)       : %,.2f%n", seqResult.totalUSD - seqResult.totalCashback);

    System.out.println("  ── By Product Type ──");
    seqResult.byProduct.entrySet().stream()
      .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
      .forEach(e -> System.out.printf("    %-15s : %,14.2f USD%n", e.getKey(), e.getValue()));

    System.out.println("  ── Top 10 Users by Final Amount ──");
    seqResult.byUser.entrySet().stream()
      .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
      .limit(10)
      .forEach(e -> System.out.printf("    User %4d : %,14.2f USD%n", e.getKey(), e.getValue()));
  }

  public static void printMatrix(long[][] matrix, String title) {
    System.out.println(title);
    if (matrix == null || matrix.length == 0) {
      System.out.println("Empty matrix");
      return;
    }

    int maxRows = Math.min(10, matrix.length);
    int maxCols = Math.min(10, matrix[0].length);

    for (int i = 0; i < maxRows; i++) {
      for (int j = 0; j < maxCols; j++) {
        System.out.print(matrix[i][j] + "\t");
      }
      if (matrix[0].length > maxCols) System.out.print("...");
      System.out.println();
    }
    if (matrix.length > maxRows) System.out.println("...");
    System.out.println("Matrix dimensions: " + matrix.length + " x " + matrix[0].length);
    System.out.println();
  }

  public static void main(String[] args) throws Exception {

    if (!Files.exists(HTML_DOCS_DIR) || Files.list(HTML_DOCS_DIR).findAny().isEmpty()) {
      new HtmlDocsGenerator().generate(1_200, HTML_DOCS_DIR);
    }

    int[] array = new IntegerArrayGenerator().generate();

    int[][][] matrices = new MatrixGenerator().generate();
    int[][] matA = matrices[0];
    int[][] matB = matrices[1];

    System.out.println("\n=== TASK BENCHMARKS ===");

    benchmarkTagFrequency();
    benchmarkArrayStatistics(array);
    benchmarkMatrixMultiplication(matA, matB);
    benchmarkTransactionProcessing();
  }
}
