package edu.kpi.lab.lab3.bankTransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AccountFactory {

  private static final double MIN_BALANCE = 100.0;
  private static final double MAX_BALANCE = 10_000.0;

  public static List<BankAccount> create(int count, long seed) {
    Random rnd = new Random(seed);
    List<BankAccount> accounts = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      double balance = MIN_BALANCE + rnd.nextDouble() * (MAX_BALANCE - MIN_BALANCE);
      balance = Math.round(balance * 100.0) / 100.0;
      accounts.add(new BankAccount(i, balance));
    }
    return accounts;
  }
}
