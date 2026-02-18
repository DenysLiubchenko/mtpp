package edu.kpi.lab.lab1.cpuBoundTasks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FactorialCalculator {

  public BigInteger calculateFactorial(int number, int numberOfThreads) {
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    try {
      int batchSize = number / numberOfThreads;
      int remainder = number % numberOfThreads;

      List<Future<BigInteger>> futures = new ArrayList<>(numberOfThreads);
      int start = 1;
      for (int i = 0; i < numberOfThreads; i++) {
        int extra = i < remainder ? 1 : 0;
        int end = start + batchSize + extra - 1;

        int from = start;
        int to = end;
        Callable<BigInteger> task = () -> calculateFactorialRunnable(from, to);

        start = end + 1;
        futures.add(executor.submit(task));
      }

      BigInteger factorialResult = BigInteger.valueOf(1L);
      for (Future<BigInteger> future : futures) {
        try {
          factorialResult = factorialResult.multiply(future.get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Calculation interrupted", e);
        } catch (ExecutionException e) {
          throw new RuntimeException("Calculation failed", e);
        }
      }

      return factorialResult;

    } finally {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  private BigInteger calculateFactorialRunnable(int from, int to) {
    //    System.out.println("Thread-" + Thread.currentThread().getName() + " started calculating Pi with " + from + " points.");
    BigInteger factorialResult = BigInteger.valueOf(1L);
    for (int i = from; i <= to; i++) {
      factorialResult = factorialResult.multiply(BigInteger.valueOf(i));
    }
    //    System.out.println("Thread-" + Thread.currentThread().getName() + " finished calculating Pi with " + from + " points.");
    return factorialResult;
  }
}
