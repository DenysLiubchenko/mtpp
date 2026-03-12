package edu.kpi.lab.lab2.transactionProcessing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CurrencyConversionStage implements Runnable {
  private final BlockingQueue<Transaction> in;
  private final BlockingQueue<ProcessedTransaction> out;
  private final int producerCount;
  private final AtomicLong remaining;

  public CurrencyConversionStage(BlockingQueue<Transaction> in,
                                 BlockingQueue<ProcessedTransaction> out,
                                 int producerCount, AtomicLong remaining) {
    this.in = in;
    this.out = out;
    this.producerCount = producerCount;
    this.remaining = remaining;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Transaction tx = in.poll(200, TimeUnit.MILLISECONDS);
        if (tx == null) {
          if (remaining.get() == 0 && in.isEmpty()) {
            break;
          }
          continue;
        }
        ProcessedTransaction ptx = new ProcessedTransaction(tx);
        ptx.amountUSD = round(tx.amount() * TransactionProducer.fxRate(tx.currency()));
        out.put(ptx);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  static double round(double v) {
    return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
