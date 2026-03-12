package edu.kpi.lab.lab2.transactionProcessing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SequentialProcessor {

  private static final double CASHBACK_RATE = 0.20;
  private static final long PREMIUM_USER_ID = 500;

  public static List<Transaction> generate(int total) {
    String[] currencies = {"USD", "EUR", "GBP", "UAH", "JPY"};
    String[] productTypes = {"Electronics", "Food", "Clothes", "Travel", "Software"};
    Random rnd = new Random(42);
    List<Transaction> list = new ArrayList<>(total);
    for (int i = 0; i < total; i++) {
      String cur = currencies[rnd.nextInt(currencies.length)];
      list.add(new Transaction(
        rnd.nextInt(1, 1001),
        Math.round(rnd.nextDouble(1, 10_000) * 100.0) / 100.0,
        cur,
        LocalDateTime.now().minusDays(rnd.nextInt(365)),
        productTypes[rnd.nextInt(productTypes.length)]
      ));
    }
    return list;
  }

  public static AggregationResult process(List<Transaction> transactions) {
    AggregationResult result = new AggregationResult();
    for (Transaction tx : transactions) {
      ProcessedTransaction ptx = new ProcessedTransaction(tx);
      ptx.amountUSD = CurrencyConversionStage.round(
        tx.amount() * TransactionProducer.fxRate(tx.currency()));

      if (tx.userId() > PREMIUM_USER_ID) {
        ptx.cashback = CurrencyConversionStage.round(ptx.amountUSD * CASHBACK_RATE);
      }

      result.add(ptx);
    }
    return result;
  }
}
