package edu.kpi.lab.lab2.arrayStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ArrayStatistics {

  private static final Random rand = new Random();
  private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
  private static final int THRESHOLD = 10_000;

  public static ArrayStatisticsResult sequential(int[] array) {
    int min = array[0];
    int max = array[0];
    long mean = 0;
    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
      if (array[i] > max) {
        max = array[i];
      }

      mean += array[i];
    }
    return new ArrayStatisticsResult(min, max, findMedian(array), (double) mean / array.length);
  }

  public static double findMedian(int[] arr) {
    int n = arr.length;
    if (n % 2 != 0) {
      return select(arr, n / 2 + 1);
    } else {
      int mid1 = select(arr, n / 2);
      int mid2 = select(arr, n / 2 + 1);
      return (mid1 + mid2) / 2.0;
    }
  }

  private static int select(int[] arr, int k) {
    return quickSelect(arr, 0, arr.length - 1, k - 1);
  }


  private static int quickSelect(int[] arr, int left, int right, int k) {
    if (left == right) {
      return arr[left];
    }

    int pivotIndex = left + rand.nextInt(right - left + 1);

    pivotIndex = partition(arr, left, right, pivotIndex);

    if (k == pivotIndex) {
      return arr[k];
    } else if (k < pivotIndex) {
      return quickSelect(arr, left, pivotIndex - 1, k);
    } else {
      return quickSelect(arr, pivotIndex + 1, right, k);
    }
  }

  private static int partition(int[] arr, int left, int right, int pivotIndex) {
    int pivotValue = arr[pivotIndex];
    swap(arr, pivotIndex, right);
    int storeIndex = left;

    for (int i = left; i < right; i++) {
      if (arr[i] < pivotValue) {
        swap(arr, storeIndex, i);
        storeIndex++;
      }
    }
    swap(arr, storeIndex, right);
    return storeIndex;
  }

  private static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

  public static ArrayStatisticsResult mapReduce(int[] array) {
    int chunkSize = Math.max(1, array.length / NUM_THREADS);
    List<int[]> chunks = new ArrayList<>();
    for (int i = 0; i < array.length; i += chunkSize) {
      chunks.add(Arrays.copyOfRange(array, i, Math.min(i + chunkSize, array.length)));
    }

    record Partial(int min, int max, long sum, int count) {
    }

    Partial reduced = chunks.parallelStream()
      .map(chunk -> {
        int localMin = chunk[0], localMax = chunk[0];
        long localSum = 0;
        for (int v : chunk) {
          if (v < localMin) {
            localMin = v;
          }
          if (v > localMax) {
            localMax = v;
          }
          localSum += v;
        }
        return new Partial(localMin, localMax, localSum, chunk.length);
      })
      .reduce(
        new Partial(Integer.MAX_VALUE, Integer.MIN_VALUE, 0L, 0),
        (a, b) -> new Partial(
          Math.min(a.min(), b.min()),
          Math.max(a.max(), b.max()),
          a.sum() + b.sum(),
          a.count() + b.count()
        )
      );

    double mean = (double) reduced.sum() / reduced.count();
    double median = findMedian(array.clone());
    return new ArrayStatisticsResult(reduced.min(), reduced.max(), median, mean);
  }

  public static ArrayStatisticsResult workerPool(int[] array) {
    ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
    int chunkSize = Math.max(1, array.length / NUM_THREADS);

    record Partial(int min, int max, long sum, int count) {
    }

    List<Future<Partial>> futures = new ArrayList<>();
    for (int i = 0; i < array.length; i += chunkSize) {
      final int start = i;
      final int end = Math.min(i + chunkSize, array.length);
      futures.add(pool.submit(() -> {
        int localMin = array[start], localMax = array[start];
        long localSum = 0;
        for (int j = start; j < end; j++) {
          if (array[j] < localMin) {
            localMin = array[j];
          }
          if (array[j] > localMax) {
            localMax = array[j];
          }
          localSum += array[j];
        }
        return new Partial(localMin, localMax, localSum, end - start);
      }));
    }

    pool.shutdown();

    int globalMin = Integer.MAX_VALUE, globalMax = Integer.MIN_VALUE;
    long globalSum = 0;
    int globalCount = 0;

    try {
      for (Future<Partial> f : futures) {
        Partial p = f.get();
        if (p.min() < globalMin) {
          globalMin = p.min();
        }
        if (p.max() > globalMax) {
          globalMax = p.max();
        }
        globalSum += p.sum();
        globalCount += p.count();
      }
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Worker pool computation failed", e);
    }

    double mean = (double) globalSum / globalCount;
    double median = findMedian(array.clone());
    return new ArrayStatisticsResult(globalMin, globalMax, median, mean);
  }

  private static class StatTask extends RecursiveTask<long[]> {
    private final int[] array;
    private final int from;
    private final int to;

    StatTask(int[] array, int from, int to) {
      this.array = array;
      this.from = from;
      this.to = to;
    }

    @Override
    protected long[] compute() {
      int size = to - from;
      if (size <= THRESHOLD) {
        int localMin = array[from], localMax = array[from];
        long localSum = 0;
        for (int i = from; i < to; i++) {
          if (array[i] < localMin) {
            localMin = array[i];
          }
          if (array[i] > localMax) {
            localMax = array[i];
          }
          localSum += array[i];
        }
        return new long[] {localMin, localMax, localSum, size};
      }
      int mid = from + size / 2;
      StatTask left = new StatTask(array, from, mid);
      StatTask right = new StatTask(array, mid, to);
      left.fork();
      long[] r = right.compute();
      long[] l = left.join();
      return new long[] {
        Math.min(l[0], r[0]),
        Math.max(l[1], r[1]),
        l[2] + r[2],
        l[3] + r[3]
      };
    }
  }

  private static class SortTask extends RecursiveTask<int[]> {
    private final int[] array;
    private final int from;
    private final int to;

    SortTask(int[] array, int from, int to) {
      this.array = array;
      this.from = from;
      this.to = to;
    }

    @Override
    protected int[] compute() {
      int size = to - from;
      if (size <= THRESHOLD) {
        int[] chunk = Arrays.copyOfRange(array, from, to);
        Arrays.sort(chunk);
        return chunk;
      }
      int mid = from + size / 2;
      SortTask left = new SortTask(array, from, mid);
      SortTask right = new SortTask(array, mid, to);
      left.fork();
      int[] r = right.compute();
      int[] l = left.join();
      return merge(l, r);
    }

    private static int[] merge(int[] a, int[] b) {
      int[] result = new int[a.length + b.length];
      int i = 0, j = 0, k = 0;
      while (i < a.length && j < b.length) {
        result[k++] = a[i] <= b[j] ? a[i++] : b[j++];
      }
      while (i < a.length) {
        result[k++] = a[i++];
      }
      while (j < b.length) {
        result[k++] = b[j++];
      }
      return result;
    }
  }

  private static double parallelMedian(int[] array) {
    int[] sorted = ForkJoinPool.commonPool().invoke(new SortTask(array, 0, array.length));
    int n = sorted.length;
    if (n % 2 != 0) {
      return sorted[n / 2];
    } else {
      return (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
    }
  }

  public static ArrayStatisticsResult forkJoin(int[] array) {
    int[] cloned = array.clone();
    long[] result = ForkJoinPool.commonPool().invoke(new StatTask(array, 0, array.length));
    double mean = (double) result[2] / result[3];
    double median = parallelMedian(cloned);
    return new ArrayStatisticsResult((int) result[0], (int) result[1], median, mean);
  }
}
