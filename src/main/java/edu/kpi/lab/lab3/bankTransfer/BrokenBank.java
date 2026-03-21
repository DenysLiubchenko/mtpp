package edu.kpi.lab.lab3.bankTransfer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BrokenBank {

  private static final boolean ENABLE_DEADLOCK = false;

  public static BankTransferResult run(List<BankAccount> accounts, int threadCount,
                                       long durationMs) throws InterruptedException {

    double totalBefore = accounts.stream().mapToDouble(a -> a.balance).sum();

    ExecutorService pool = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startGate = new CountDownLatch(1);
    long deadline = System.currentTimeMillis() + durationMs;
    long[] txCounter = {0};
    Object txLock = new Object();

    for (int t = 0; t < threadCount; t++) {
      pool.submit(() -> {
        Random rnd = new Random();
        try {
          startGate.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        while (System.currentTimeMillis() < deadline) {
          int i = rnd.nextInt(accounts.size());
          int j = rnd.nextInt(accounts.size());
          if (i == j) {
            continue;
          }

          BankAccount from = accounts.get(i);
          BankAccount to = accounts.get(j);
          double amount = 1 + rnd.nextInt(100);

          if (ENABLE_DEADLOCK) {
            from.lock.lock();
            try {
              Thread.sleep(1);
              to.lock.lock();
              try {
                if (from.balance >= amount) {
                  from.balance -= amount;
                  to.balance += amount;
                }
              } finally {
                to.lock.unlock();
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              from.lock.unlock();
            }
          } else {
            if (from.balance >= amount) {
              from.balance -= amount;
              to.balance += amount;
            }
          }

          synchronized (txLock) {
            txCounter[0]++;
          }
        }
      });
    }

    startGate.countDown();
    pool.shutdown();
    pool.awaitTermination(durationMs + 5_000, TimeUnit.MILLISECONDS);

    double totalAfter = accounts.stream().mapToDouble(a -> a.balance).sum();
    return new BankTransferResult(totalBefore, totalAfter, txCounter[0]);
  }
}
