package edu.kpi.lab.lab2.transactionProcessing;

public class ProcessedTransaction {
  final Transaction original;
  double amountUSD;
  double cashback;
  double finalAmount;

  ProcessedTransaction(Transaction tx) {
    this.original = tx;
    this.amountUSD = tx.amount();
  }

  @Override
  public String toString() {
    return "ProcessedTX[user=%d | orig=%.2f %s | USD=%.2f | cashback=%.2f | final=%.2f]"
      .formatted(original.userId(), original.amount(), original.currency(),
        amountUSD, cashback, finalAmount);
  }
}
