package edu.kpi.lab.lab2.matrixMultiplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class MatrixMultiplier {
  private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

  public static long[][] sequential(int[][] a, int[][] b) {
    long[][] result = new long[a.length][b[0].length];

    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < b[0].length; j++) {
        for (int k = 0; k < a[0].length; k++) {
          result[i][j] += (long) a[i][k] * b[k][j];
        }
      }
    }

    return result;
  }

  public static long[][] mapReduce(int[][] a, int[][] b) {
    int rows = a.length;
    int cols = b[0].length;
    int inner = a[0].length;

    long[][] result = new long[rows][cols];

    // Map: each row index is an independent unit of work, processed in parallel
    // Reduce: for each (i, j), sum partial products a[i][k] * b[k][j] over all k
    IntStream.range(0, rows)
      .parallel()
      .forEach(i ->
        IntStream.range(0, cols).forEach(j -> {
          long sum = IntStream.range(0, inner)
            .mapToLong(k -> (long) a[i][k] * b[k][j])
            .sum();
          result[i][j] = sum;
        })
      );

    return result;
  }

  public static long[][] forkJoin(int[][] a, int[][] b) {
    long[][] result = new long[a.length][b[0].length];
    ForkJoinPool.commonPool().invoke(new MatrixRowTask(a, b, result, 0, a.length));
    return result;
  }

  private static class MatrixRowTask extends RecursiveAction {
    private static final int THRESHOLD = 50;
    private final int[][] a;
    private final int[][] b;
    private final long[][] result;
    private final int fromRow;
    private final int toRow;

    MatrixRowTask(int[][] a, int[][] b, long[][] result, int fromRow, int toRow) {
      this.a = a;
      this.b = b;
      this.result = result;
      this.fromRow = fromRow;
      this.toRow = toRow;
    }

    @Override
    protected void compute() {
      if (toRow - fromRow <= THRESHOLD) {
        for (int i = fromRow; i < toRow; i++) {
          for (int j = 0; j < b[0].length; j++) {
            long sum = 0;
            for (int k = 0; k < a[0].length; k++) {
              sum += (long) a[i][k] * b[k][j];
            }
            result[i][j] = sum;
          }
        }
      } else {
        int mid = (fromRow + toRow) / 2;
        MatrixRowTask left = new MatrixRowTask(a, b, result, fromRow, mid);
        MatrixRowTask right = new MatrixRowTask(a, b, result, mid, toRow);
        left.fork();
        right.compute();
        left.join();
      }
    }
  }

  public static long[][] workerPool(int[][] a, int[][] b) throws ExecutionException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    long[][] c = new long[a.length][b[0].length];
    List<Future<?>> futures = new ArrayList<>(a.length);

    for (int i = 0; i < a.length; i++) {
      final int row = i;
      futures.add(executor.submit(() -> {
        for (int j = 0; j < b[0].length; j++) {
          long sum = 0;
          for (int k = 0; k < a[0].length; k++) {
            sum += (long) a[row][k] * b[k][j];
          }
          c[row][j] = sum;
        }
      }));
    }

    executor.shutdown();

    for (Future<?> f : futures) {
      f.get();
    }

    return c;
  }
}