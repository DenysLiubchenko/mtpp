package edu.kpi.lab.lab2.transactionProcessing;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AggregationStage implements Runnable {
  private final BlockingQueue<ProcessedTransaction> in;
  private final AtomicLong stage2Active;

  public final AtomicReference<Double> totalUSD = new AtomicReference<>(0.0);
  public final AtomicReference<Double> totalCashback = new AtomicReference<>(0.0);
  public final AtomicLong txCount = new AtomicLong(0);
  public final Map<String, Double> byProduct = new ConcurrentHashMap<>();
  public final Map<Long, Double> byUser = new ConcurrentHashMap<>();

  public AggregationStage(BlockingQueue<ProcessedTransaction> in, AtomicLong stage2Active) {
    this.in = in;
    this.stage2Active = stage2Active;
  }

  @Override
  public void run() {
    while (true) {
      try {
        ProcessedTransaction ptx = in.poll(200, TimeUnit.MILLISECONDS);
        if (ptx == null) {
          if (stage2Active.get() == 0 && in.isEmpty()) {
            break;
          }
          continue;
        }
        ptx.finalAmount = CurrencyConversionStage.round(ptx.amountUSD - ptx.cashback);

        totalUSD.updateAndGet(v -> CurrencyConversionStage.round(v + ptx.amountUSD));
        totalCashback.updateAndGet(v -> CurrencyConversionStage.round(v + ptx.cashback));
        txCount.incrementAndGet();

        byProduct.merge(ptx.original.productType(), ptx.finalAmount, Double::sum);
        byUser.merge(ptx.original.userId(), ptx.finalAmount, Double::sum);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
