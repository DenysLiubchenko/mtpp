package edu.kpi.lab.lab1.cpuBoundTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PrimeNumberCalculator {

  public List<Integer> calculatePrimes(int start, int end, int numberOfThreads) {
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    try {
      int range = end - start + 1;
      int batchSize = range / numberOfThreads;
      int remainder = range % numberOfThreads;

      List<Future<List<Integer>>> futures = new ArrayList<>(numberOfThreads);
      int currentStart = start;
      for (int i = 0; i < numberOfThreads; i++) {
        int extra = i < remainder ? 1 : 0;
        int currentEnd = currentStart + batchSize + extra - 1;

        int from = currentStart;
        int to = currentEnd;
        Callable<List<Integer>> task = () -> calculatePrimesInRange(from, to);

        currentStart = currentEnd + 1;
        futures.add(executor.submit(task));
      }

      List<Integer> allPrimes = new ArrayList<>();
      for (Future<List<Integer>> future : futures) {
        try {
          allPrimes.addAll(future.get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Calculation interrupted", e);
        } catch (ExecutionException e) {
          throw new RuntimeException("Calculation failed", e);
        }
      }

      return allPrimes;

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

  private List<Integer> calculatePrimesInRange(int from, int to) {
    List<Integer> primes = new ArrayList<>();
    for (int i = from; i <= to; i++) {
      if (isPrime(i)) {
        primes.add(i);
      }
    }
    return primes;
  }

  private boolean isPrime(int number) {
    if (number <= 1) {
      return false;
    }
    if (number == 2) {
      return true;
    }
    if (number % 2 == 0) {
      return false;
    }
    int sqrt = (int) Math.sqrt(number);
    for (int i = 3; i <= sqrt; i += 2) {
      if (number % i == 0) {
        return false;
      }
    }
    return true;
  }
}

