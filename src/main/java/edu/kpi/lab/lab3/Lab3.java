package edu.kpi.lab.lab3;

import edu.kpi.lab.lab3.bankTransfer.AccountFactory;
import edu.kpi.lab.lab3.bankTransfer.BankAccount;
import edu.kpi.lab.lab3.bankTransfer.BankTransferResult;
import edu.kpi.lab.lab3.bankTransfer.BrokenBank;
import edu.kpi.lab.lab3.bankTransfer.FixedBank;
import edu.kpi.lab.lab3.ipc.IpcResult;
import edu.kpi.lab.lab3.ipc.PythonIpc;
import edu.kpi.lab.lab3.ipc.PythonIpcResult;
import edu.kpi.lab.lab3.ipc.SharedMemoryIpc;
import edu.kpi.lab.lab3.ipc.SocketIpc;
import java.util.List;
import java.util.Random;

public class Lab3 {

  private static final int ACCOUNT_COUNT = 150;
  private static final int THREAD_COUNT = 1_200;
  private static final long DURATION_MS = 2_000;
  private static final long SEED = 42L;
  private static final int IPC_ROUNDS = 5;

  private static void benchmarkBankTransfer() throws Exception {
    System.out.println("\n[TASK 1] Bank Transfer – Race Condition & Deadlock"
                       + " (" + ACCOUNT_COUNT + " accounts, " + THREAD_COUNT + " threads, " + DURATION_MS + " ms)");

    List<BankAccount> brokenAccounts = AccountFactory.create(ACCOUNT_COUNT, SEED);
    BankTransferResult broken = BrokenBank.run(brokenAccounts, THREAD_COUNT, DURATION_MS);
    System.out.println("  Broken (no lock):");
    printTransferResult(broken);

    List<BankAccount> fixedAccounts = AccountFactory.create(ACCOUNT_COUNT, SEED);
    BankTransferResult fixed = FixedBank.run(fixedAccounts, THREAD_COUNT, DURATION_MS);
    System.out.println("  Fixed (ReentrantLock):");
    printTransferResult(fixed);
  }

  private static void printTransferResult(BankTransferResult r) {
    System.out.printf("    transactions : %,d%n", r.txCount());
    System.out.printf("    total before : %,.2f%n", r.totalBefore());
    System.out.printf("    total after  : %,.2f%n", r.totalAfter());
    System.out.printf("    consistent   : %s%n", r.isConsistent() ? "YES" : "NO (race condition)");
  }

  private static void benchmarkIpc() throws Exception {
    System.out.println("\n[TASK 2] IPC Benchmark (" + IPC_ROUNDS + " rounds)");

    Random rnd = new Random(99L);
    SharedMemoryIpc smIpc = new SharedMemoryIpc();
    SocketIpc sIpc = new SocketIpc();

    long totalSmNs = 0, totalSockNs = 0;
    boolean smOk = true, sockOk = true;

    for (int i = 0; i < IPC_ROUNDS; i++) {
      long val = Math.abs(rnd.nextLong() % 1_000_000);
      IpcResult sm = smIpc.run(val);
      IpcResult sock = sIpc.run(val);
      totalSmNs += sm.elapsedNs();
      totalSockNs += sock.elapsedNs();
      if (!sm.isCorrect()) {
        smOk = false;
      }
      if (!sock.isCorrect()) {
        sockOk = false;
      }
    }

    System.out.println("  Java (java.util.concurrent + java.net):");
    printIpcResult("Shared Memory (SynchronousQueue)", totalSmNs / IPC_ROUNDS, smOk);
    printIpcResult("Socket (TCP)", totalSockNs / IPC_ROUNDS, sockOk);

    System.out.println("  Python (multiprocessing):");
    PythonIpcResult py = new PythonIpc().run(IPC_ROUNDS);
    printPythonIpcResult("Queue", py.queueMs());
    printPythonIpcResult("Pipe", py.pipeMs());
    printPythonIpcResult("Shared Memory", py.sharedMemoryMs());
  }

  private static void printIpcResult(String method, long avgNs, boolean correct) {
    System.out.println("    " + method + ":");
    System.out.printf("      avg latency : %.3f ms%n", avgNs / 1_000_000.0);
    System.out.printf("      correct     : %s%n", correct ? "YES" : "NO");
  }

  private static void printPythonIpcResult(String method, double avgMs) {
    System.out.println("    " + method + ":");
    System.out.printf("      avg latency : %.3f ms%n", avgMs);
  }

  public static void main(String[] args) throws Exception {
    benchmarkBankTransfer();
    benchmarkIpc();
  }
}
