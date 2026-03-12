package edu.kpi.lab.lab2.transactionProcessing;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionPipeline {

  private static final int QUEUE_CAPACITY = 5_000;

  public static AggregationStage runPipeline(List<Transaction> dataset) throws InterruptedException {
    BlockingQueue<Transaction> rawQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    BlockingQueue<ProcessedTransaction> convertedQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    BlockingQueue<ProcessedTransaction> cashbackQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    AtomicLong producersActive = new AtomicLong(1);
    AtomicLong stage1Active = new AtomicLong(2);
    AtomicLong stage2Active = new AtomicLong(2);

    ExecutorService pool = Executors.newCachedThreadPool();

    AggregationStage aggregation = new AggregationStage(cashbackQueue, stage2Active);

    for (int i = 0; i < 2; i++) {
      pool.submit(() -> {
        new CashbackStage(convertedQueue, cashbackQueue, stage1Active).run();
        stage2Active.decrementAndGet();
      });
    }

    for (int i = 0; i < 2; i++) {
      pool.submit(() -> {
        new CurrencyConversionStage(rawQueue, convertedQueue, 1, producersActive).run();
        stage1Active.decrementAndGet();
      });
    }

    pool.submit(() -> {
      for (Transaction tx : dataset) {
        try {
          rawQueue.put(tx);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      producersActive.decrementAndGet();
    });

    aggregation.run();

    pool.shutdown();
    pool.awaitTermination(30, TimeUnit.SECONDS);

    return aggregation;
  }
}