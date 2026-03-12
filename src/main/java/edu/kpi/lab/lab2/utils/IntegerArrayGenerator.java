package edu.kpi.lab.lab2.utils;

import java.util.Random;

public class IntegerArrayGenerator {

  private static final int DEFAULT_SIZE = 10_000_000;

  private static final double LAMBDA = 0.0001;

  private final Random random = new Random();

  public int[] generate() {
    return generate(DEFAULT_SIZE);
  }

  public int[] generate(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("size must be > 0");
    }

    int[] array = new int[size];
    for (int i = 0; i < size; i++) {
      double u = random.nextDouble();
      array[i] = (int) (-Math.log(1.0 - u) / LAMBDA);
    }
    return array;
  }
}
