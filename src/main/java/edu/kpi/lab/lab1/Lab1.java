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
      if (matrix[0].length > maxCols) {
        System.out.print("...");
      }
      System.out.println();
    }
    if (matrix.length > maxRows) {
      System.out.println("...");
    }
    System.out.println("Matrix dimensions: " + matrix.length + " x " + matrix[0].length);
    System.out.println();
  }

  public static void main(String[] args) {
    System.out.println("CPU-bound tasks:");
    MonteCarloPiCalculator monteCarloPiCalculator = new MonteCarloPiCalculator();
    long startPiParallel = System.currentTimeMillis();
    double piParallel = monteCarloPiCalculator.calculatePi(1_000_000_000, 100);
    long endPiParallel = System.currentTimeMillis();

    long startPiIterative = System.currentTimeMillis();
    double piIterative = monteCarloPiCalculator.calculatePi(1_000_000_000, 1);
    long endPiIterative = System.currentTimeMillis();

    System.out.println("Parallel calculation of Pi: " + piParallel + " Time taken: " + (endPiParallel - startPiParallel) + " ms");
    System.out.println("Iterative calculation of Pi: " + piIterative + " Time taken: " + (endPiIterative - startPiIterative) + " ms");

    //    ---------------------------------------------------------------
    FactorialCalculator factorialCalculator = new FactorialCalculator();
    long startFactParallel = System.currentTimeMillis();
    BigInteger factorialParallel = factorialCalculator.calculateFactorial(50000, 5);
    long endFactParallel = System.currentTimeMillis();

    long startFactIterative = System.currentTimeMillis();
    BigInteger factorialIterative = factorialCalculator.calculateFactorial(50000, 1);
    long endFactIterative = System.currentTimeMillis();

    System.out.println(
        "Parallel calculation of Factorial: Time taken: " + (endFactParallel - startFactParallel) + " ms");
    System.out.println(
        "Iterative calculation of Factorial: Time taken: " + (endFactIterative - startFactIterative) + " ms");

    //    ---------------------------------------------------------------
    PrimeNumberCalculator primeCalculator = new PrimeNumberCalculator();
    long startPrimeParallel = System.currentTimeMillis();
    List<Integer> primesParallel = primeCalculator.calculatePrimes(1, 10_000_000, 8);
    long endPrimeParallel = System.currentTimeMillis();

    long startPrimeIterative = System.currentTimeMillis();
    List<Integer> primesIterative = primeCalculator.calculatePrimes(1, 10_000_000, 1);
    long endPrimeIterative = System.currentTimeMillis();

    System.out.println(
        "Parallel calculation of Primes: Found " + primesParallel.size() + " primes. Time taken: " + (endPrimeParallel -
            startPrimeParallel)
            + " ms");
    System.out.println(
        "Iterative calculation of Primes: Found " + primesIterative.size() + " primes. Time taken: " + (endPrimeIterative
            - startPrimeIterative) + " ms");

    //    ---------------------------------------------------------------
    System.out.println("\nIO-bound tasks:");
    Transposer transposer = new Transposer();

    // Create a test matrix
    int matrixSize = 10000;
    Integer[][] testMatrix = new Integer[matrixSize][matrixSize];
    for (int i = 0; i < matrixSize; i++) {
      for (int j = 0; j < matrixSize; j++) {
        testMatrix[i][j] = i * matrixSize + j;
      }
    }

    long startTransposeParallel = System.currentTimeMillis();
    Integer[][] transposedParallel = transposer.transpose(testMatrix, 8);
    long endTransposeParallel = System.currentTimeMillis();

    long startTransposeIterative = System.currentTimeMillis();
    Integer[][] transposedIterative = transposer.transpose(testMatrix, 1);
    long endTransposeIterative = System.currentTimeMillis();

    printMatrix(testMatrix, "Original Matrix (sample):");
    printMatrix(transposedParallel, "Transposed Matrix - Parallel (sample):");

    System.out.println(
        "Parallel matrix transposition: Time taken: " + (endTransposeParallel - startTransposeParallel) + " ms");
    System.out.println(
        "Iterative matrix transposition: Time taken: " + (endTransposeIterative - startTransposeIterative) + " ms");

    // ---------------------------------------------------------------
    System.out.println("\nWord counting in directory:");

    String testDirectoryPath = "test_data";
    int numberOfTestFiles = 1000;

    try {
      System.out.println("Generating " + numberOfTestFiles + " test files...");
      long startGeneration = System.currentTimeMillis();
      TestDataGenerator.generateTestFiles(testDirectoryPath, numberOfTestFiles);
      long endGeneration = System.currentTimeMillis();
      System.out.println("Test files generated in " + (endGeneration - startGeneration) + " ms");

      WordCounter wordCounter = new WordCounter();

      long startCountParallel = System.currentTimeMillis();
      long totalWordsParallel = wordCounter.countWordsInDirectory(testDirectoryPath, 4);
      long endCountParallel = System.currentTimeMillis();

      long startCountIterative = System.currentTimeMillis();
      long totalWordsIterative = wordCounter.countWordsInDirectory(testDirectoryPath, 1);
      long endCountIterative = System.currentTimeMillis();

      System.out.println(
          "Parallel word counting (8 threads): Total words = " + totalWordsParallel +
              ", Time taken: " + (endCountParallel - startCountParallel) + " ms");
      System.out.println(
          "Iterative word counting (1 thread): Total words = " + totalWordsIterative +
              ", Time taken: " + (endCountIterative - startCountIterative) + " ms");

      TestDataGenerator.cleanupTestFiles(testDirectoryPath);

    } catch (Exception e) {
      System.err.println("Error during word counting test: " + e.getMessage());
      e.printStackTrace();
    }

  }

}
