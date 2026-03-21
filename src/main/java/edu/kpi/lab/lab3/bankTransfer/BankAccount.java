package edu.kpi.lab.lab3.bankTransfer;

import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {

  public final int id;
  public double balance;
  public final ReentrantLock lock = new ReentrantLock();

  public BankAccount(int id, double initialBalance) {
    this.id = id;
    this.balance = initialBalance;
  }

  @Override
  public String toString() {
    return "Account#" + id + "(" + String.format("%.2f", balance) + ")";
  }
}
