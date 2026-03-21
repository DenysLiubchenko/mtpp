package edu.kpi.lab.lab3.bankTransfer;

public record BankTransferResult(double totalBefore, double totalAfter, long txCount) {

  public double discrepancy() {
    return Math.abs(totalAfter - totalBefore);
  }

  public boolean isConsistent() {
    return discrepancy() < 0.001;
  }
}
