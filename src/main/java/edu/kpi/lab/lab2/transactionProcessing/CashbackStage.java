package edu.kpi.lab.lab2.transactionProcessing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CashbackStage implements Runnable {
  private static final double CASHBACK_RATE = 0.20;
  private static final long PREMIUM_USER_ID = 500;

  private final BlockingQueue<ProcessedTransaction> in, out;
  private final AtomicLong stage1Active;

  public CashbackStage(BlockingQueue<ProcessedTransaction> in,
                       BlockingQueue<ProcessedTransaction> out,
                       AtomicLong stage1Active) {
    this.in = in;
    this.out = out;
    this.stage1Active = stage1Active;
  }

  @Override
  public void run() {
    while (true) {
      try {
        ProcessedTransaction ptx = in.poll(200, TimeUnit.MILLISECONDS);
        if (ptx == null) {
          if (stage1Active.get() == 0 && in.isEmpty()) {
            break;
          }
          continue;
        }
        if (ptx.original.userId() > PREMIUM_USER_ID) {
          ptx.cashback = CurrencyConversionStage.round(ptx.amountUSD * CASHBACK_RATE);
        }
        out.put(ptx);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
