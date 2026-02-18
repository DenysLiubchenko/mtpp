package edu.kpi.lab.lab1.memoryBoundTasks;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Transposer {

  public <T> T[][] transpose(T[][] matrix, int numberOfThreads) {
    if (matrix == null || matrix.length == 0) {
      throw new IllegalArgumentException("Matrix cannot be null or empty");
    }

    int rows = matrix.length;
    int cols = matrix[0].length;

    T[][] transposed = (T[][]) Array.newInstance(matrix[0].getClass(), cols);
    for (int i = 0; i < cols; i++) {
      T[] row = (T[]) Array.newInstance(matrix[0][0].getClass(), rows);
      transposed[i] = row;
    }

    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    try {
      int batchSize = rows / numberOfThreads;
      int remainder = rows % numberOfThreads;

      List<Future<Void>> futures = new ArrayList<>(numberOfThreads);
      int startRow = 0;

      for (int i = 0; i < numberOfThreads; i++) {
        int extra = i < remainder ? 1 : 0;
        int endRow = startRow + batchSize + extra;

        int from = startRow;
        int to = endRow;

        Callable<Void> task = () -> {
          transposeBlock(matrix, transposed, from, to, 0, cols);
          return null;
        };

        startRow = endRow;
        futures.add(executor.submit(task));
      }

      for (Future<Void> future : futures) {
        try {
          future.get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Transposition interrupted", e);
        } catch (ExecutionException e) {
          throw new RuntimeException("Transposition failed", e);
        }
      }

      return transposed;

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

  private <T> void transposeBlock(T[][] matrix, T[][] transposed, int fromRow, int toRow, int fromCol, int toCol) {
    for (int i = fromRow; i < toRow; i++) {
      for (int j = fromCol; j < toCol; j++) {
        transposed[j][i] = matrix[i][j];
      }
    }
  }
}
