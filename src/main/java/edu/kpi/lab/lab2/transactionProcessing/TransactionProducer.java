package edu.kpi.lab.lab2.transactionProcessing;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class TransactionProducer implements Runnable {
  private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "UAH", "JPY"};
  private static final String[] PRODUCT_TYPES = {"Electronics", "Food", "Clothes", "Travel", "Software"};
  private static final Map<String, Double> FX = Map.of(
    "USD", 1.0, "EUR", 1.08, "GBP", 1.27, "UAH", 0.024, "JPY", 0.0067
  );

  private final BlockingQueue<Transaction> queue;
  private final int totalTransactions;
  private final Random rnd = new Random();

  TransactionProducer(BlockingQueue<Transaction> q, int total) {
    this.queue = q;
    this.totalTransactions = total;
  }

  @Override
  public void run() {
    for (int i = 0; i < totalTransactions; i++) {
      String cur = CURRENCIES[rnd.nextInt(CURRENCIES.length)];
      Transaction tx = new Transaction(
        rnd.nextInt(1, 1001),
        Math.round(rnd.nextDouble(1, 10_000) * 100.0) / 100.0,
        cur,
        LocalDateTime.now().minusDays(rnd.nextInt(365)),
        PRODUCT_TYPES[rnd.nextInt(PRODUCT_TYPES.length)]
      );
      try {
        queue.put(tx);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  static double fxRate(String currency) {
    return FX.getOrDefault(currency, 1.0);
  }
}
