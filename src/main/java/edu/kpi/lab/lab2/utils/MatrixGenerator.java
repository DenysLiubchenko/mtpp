package edu.kpi.lab.lab2.utils;

import java.util.Random;

public class MatrixGenerator {

  public static final int DEFAULT_SIZE = 1_001;

  private static final double LAMBDA = 10.0;

  private final Random random = new Random();

  public int[][][] generate() {
    return generate(DEFAULT_SIZE);
  }

  public int[][][] generate(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("size must be > 0");
    }

    int[][] a = buildMatrix(size);
    int[][] b = buildMatrix(size);

    return new int[][][] {a, b};
  }

  private int[][] buildMatrix(int size) {
    int[][] m = new int[size][size];
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        m[r][c] = poissonSample();
      }
    }
    return m;
  }

  private int poissonSample() {
    double limit = Math.exp(-LAMBDA);
    double product = random.nextDouble();
    int count = 0;
    while (product > limit) {
      product *= random.nextDouble();
      count++;
    }
    return count;
  }
}
