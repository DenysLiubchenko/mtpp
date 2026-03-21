package edu.kpi.lab.lab3.ipc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class SocketIpc {

  private static final int PORT = 19876;

  public IpcResult run(long value) throws Exception {
    CountDownLatch serverReady = new CountDownLatch(1);
    long[] loggedRef = {-1L};

    Thread server = Thread.ofVirtual().start(() -> {
      try (ServerSocket ss = new ServerSocket(PORT)) {
        serverReady.countDown();
        try (Socket conn = ss.accept();
             BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             PrintWriter out = new PrintWriter(conn.getOutputStream(), true)) {

          long received = Long.parseLong(in.readLine().trim());
          loggedRef[0] = received;
          out.println(received);
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    });

    serverReady.await();

    long t0 = System.nanoTime();

    long echoed;
    try (Socket sock = new Socket("localhost", PORT);
         PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {

      out.println(value);
      echoed = Long.parseLong(in.readLine().trim());
    }

    long elapsed = System.nanoTime() - t0;
    server.join();

    return new IpcResult("Socket(TCP)", value, echoed, elapsed, loggedRef[0]);
  }
}
