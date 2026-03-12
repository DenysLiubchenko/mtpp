package edu.kpi.lab.lab2.transactionProcessing;

import java.util.HashMap;
import java.util.Map;

public class AggregationResult {
  public double totalUSD = 0;
  public double totalCashback = 0;
  public long txCount = 0;
  public final Map<String, Double> byProduct = new HashMap<>();
  public final Map<Long, Double> byUser = new HashMap<>();

  void add(ProcessedTransaction ptx) {
    ptx.finalAmount = CurrencyConversionStage.round(ptx.amountUSD - ptx.cashback);
    totalUSD = CurrencyConversionStage.round(totalUSD + ptx.amountUSD);
    totalCashback = CurrencyConversionStage.round(totalCashback + ptx.cashback);
    txCount++;
    byProduct.merge(ptx.original.productType(), ptx.finalAmount, Double::sum);
    byUser.merge(ptx.original.userId(), ptx.finalAmount, Double::sum);
  }
}