package edu.kpi.lab.lab1.cpuBoundTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MonteCarloPiCalculator {

  public double calculatePi(int numberOfPoints, int numberOfThreads) {
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    try {
      int batchSize = numberOfPoints / numberOfThreads;
      int remainder = numberOfPoints % numberOfThreads;

      List<Future<Long>> futures = new ArrayList<>(numberOfThreads);

      for (int i = 0; i < numberOfThreads; i++) {
        int pointsForThisThread = batchSize + (i < remainder ? 1 : 0);

        Callable<Long> task = () -> calculatePointsInCircle(pointsForThisThread);
        futures.add(executor.submit(task));
      }

      long totalPointsInsideCircle = 0;
      for (Future<Long> future : futures) {
        try {
          totalPointsInsideCircle += future.get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Calculation interrupted", e);
        } catch (ExecutionException e) {
          throw new RuntimeException("Calculation failed", e);
        }
      }

      return (double) totalPointsInsideCircle / numberOfPoints * 4.0;

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

  private long calculatePointsInCircle(int numberOfPoints) {
    long pointsInsideCircle = 0;
    ThreadLocalRandom random = ThreadLocalRandom.current();

    for (int i = 0; i < numberOfPoints; i++) {
      double x = random.nextDouble();
      double y = random.nextDouble();

      if (x * x + y * y <= 1.0) {
        pointsInsideCircle++;
      }
    }

    return pointsInsideCircle;
  }
}

