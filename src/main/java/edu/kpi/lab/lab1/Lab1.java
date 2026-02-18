package edu.kpi.lab.lab1;

import java.math.BigInteger;
import java.util.List;

import edu.kpi.lab.lab1.cpuBoundTasks.FactorialCalculator;
import edu.kpi.lab.lab1.cpuBoundTasks.MonteCarloPiCalculator;
import edu.kpi.lab.lab1.cpuBoundTasks.PrimeNumberCalculator;
import edu.kpi.lab.lab1.ioBoundTasks.TestDataGenerator;
import edu.kpi.lab.lab1.ioBoundTasks.WordCounter;
import edu.kpi.lab.lab1.memoryBoundTasks.Transposer;

public class Lab1 {

  private static final int[] THREAD_COUNTS = {1, 2, 4, 8, 16};

  public static <T> void printMatrix(T[][] matrix, String title) {
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

  private static void printRow(int threads, long timeMs, long baseTimeMs) {
    double speedup = baseTimeMs > 0 ? (double) baseTimeMs / timeMs : 1.0;
    double efficiency = speedup / threads * 100;
    System.out.printf("  %-8d | %-12d | %-10.2f | %-12.1f%%%n",
      threads, timeMs, speedup, efficiency);
  }

  private static void printTableHeader() {
    System.out.println("  Threads  | Time (ms)    | Speedup    | Efficiency");
    System.out.println("  ---------|--------------|------------|-------------");
  }

  private static void benchmarkMonteCarloPi() {
    System.out.println("\n[CPU-BOUND] Monte Carlo Pi (1,000,000,000 samples)");
    printTableHeader();

    MonteCarloPiCalculator calc = new MonteCarloPiCalculator();
    long baseTime = -1;

    for (int threads : THREAD_COUNTS) {
      long start = System.currentTimeMillis();
      double pi = calc.calculatePi(1_000_000_000, threads);
      long elapsed = System.currentTimeMillis() - start;

      if (baseTime < 0) baseTime = elapsed;
      printRow(threads, elapsed, baseTime);
    }
  }

  private static void benchmarkFactorial() {
    System.out.println("\n[CPU-BOUND] Factorial(50000)");
    printTableHeader();

    FactorialCalculator calc = new FactorialCalculator();
    long baseTime = -1;

    for (int threads : THREAD_COUNTS) {
      long start = System.currentTimeMillis();
      BigInteger result = calc.calculateFactorial(50000, threads);
      long elapsed = System.currentTimeMillis() - start;

      if (baseTime < 0) baseTime = elapsed;
      printRow(threads, elapsed, baseTime);
    }
  }

  private static void benchmarkPrimes() {
    System.out.println("\n[CPU-BOUND] Prime numbers in [1, 10,000,000]");
    printTableHeader();

    PrimeNumberCalculator calc = new PrimeNumberCalculator();
    long baseTime = -1;

    for (int threads : THREAD_COUNTS) {
      long start = System.currentTimeMillis();
      List<Integer> primes = calc.calculatePrimes(1, 10_000_000, threads);
      long elapsed = System.currentTimeMillis() - start;

      if (baseTime < 0) baseTime = elapsed;
      System.out.printf("  %-8d | %-12d | %-10.2f | %-12.1f%%  (found %d primes)%n",
        threads, elapsed,
        baseTime > 0 ? (double) baseTime / elapsed : 1.0,
        baseTime > 0 ? ((double) baseTime / elapsed / threads * 100) : 100.0,
        primes.size());
      if (baseTime < 0) baseTime = elapsed;
    }
  }

  private static void benchmarkTranspose() {
    System.out.println("\n[MEMORY-BOUND] Matrix transposition (10000 x 10000)");
    printTableHeader();

    Transposer transposer = new Transposer();
    int matrixSize = 10000;
    Integer[][] testMatrix = new Integer[matrixSize][matrixSize];
    for (int i = 0; i < matrixSize; i++)
      for (int j = 0; j < matrixSize; j++)
        testMatrix[i][j] = i * matrixSize + j;

    long baseTime = -1;
    Integer[][] lastTransposed = null;

    for (int threads : THREAD_COUNTS) {
      long start = System.currentTimeMillis();
      lastTransposed = transposer.transpose(testMatrix, threads);
      long elapsed = System.currentTimeMillis() - start;

      if (baseTime < 0) baseTime = elapsed;
      printRow(threads, elapsed, baseTime);
    }

    printMatrix(testMatrix,       "Original Matrix (sample):");
    printMatrix(lastTransposed,   "Transposed Matrix (sample):");
  }

  private static void benchmarkWordCount(String testDir, int fileCount) {
    System.out.println("\n[IO-BOUND] Word counting (" + fileCount + " files)");
    printTableHeader();

    WordCounter wordCounter = new WordCounter();
    long baseTime = -1;

    for (int threads : THREAD_COUNTS) {
      try {
        long start = System.currentTimeMillis();
        long totalWords = wordCounter.countWordsInDirectory(testDir, threads);
        long elapsed = System.currentTimeMillis() - start;

        if (baseTime < 0) baseTime = elapsed;
        System.out.printf("  %-8d | %-12d | %-10.2f | %-12.1f%%  (words: %d)%n",
          threads, elapsed,
          baseTime > 0 ? (double) baseTime / elapsed : 1.0,
          baseTime > 0 ? ((double) baseTime / elapsed / threads * 100) : 100.0,
          totalWords);
        if (baseTime < 0) baseTime = elapsed;
      } catch (Exception e) {
        System.err.println("  Error for " + threads + " threads: " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    System.out.println("=================================================================");

    System.out.println("\n=== CPU-BOUND TASKS ===");
    benchmarkMonteCarloPi();
    benchmarkFactorial();
    benchmarkPrimes();

    System.out.println("\n=== MEMORY-BOUND TASKS ===");
    benchmarkTranspose();

    System.out.println("\n=== IO-BOUND TASKS ===");
    String testDir = "test_data";
    int fileCount = 1000;

    try {
      System.out.println("Generating " + fileCount + " test files...");
      long genStart = System.currentTimeMillis();
      TestDataGenerator.generateTestFiles(testDir, fileCount);
      System.out.println("Generated in " + (System.currentTimeMillis() - genStart) + " ms");

      benchmarkWordCount(testDir, fileCount);

    } catch (Exception e) {
      System.err.println("Error generating test files: " + e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        TestDataGenerator.cleanupTestFiles(testDir);
      } catch (Exception ignored) {}
    }

    System.out.println("\n=================================================================");
  }
}