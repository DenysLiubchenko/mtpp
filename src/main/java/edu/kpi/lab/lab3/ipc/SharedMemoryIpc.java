package edu.kpi.lab.lab3.ipc;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SharedMemoryIpc {

  private final SynchronousQueue<Long> toWorker = new SynchronousQueue<>();
  private final SynchronousQueue<Long> toMain = new SynchronousQueue<>();
  private final AtomicLong loggedValue = new AtomicLong(-1);

  public IpcResult run(long value) throws InterruptedException {
    long t0 = System.nanoTime();

    Thread worker = Thread.ofVirtual().start(() -> {
      try {
        long received = toWorker.take();
        loggedValue.set(received);
        toMain.put(received);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    toWorker.put(value);
    long echoed = toMain.take();

    worker.join();
    long elapsed = System.nanoTime() - t0;

    return new IpcResult("SharedMemory", value, echoed, elapsed, loggedValue.get());
  }
}
