package edu.kpi.lab.lab2.transactionProcessing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Transaction(
  long userId,
  double amount,
  String currency,
  LocalDateTime date,
  String productType
) {
  @Override
  public String toString() {
    return "TX[user=%d, %.2f %s, %s, %s]"
      .formatted(userId, amount, currency, productType,
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
  }
}