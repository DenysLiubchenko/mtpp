package edu.kpi.lab.lab3.ipc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonIpc {

  private static final String SCRIPT_PATH = "src/main/python/ipc_benchmark.py";

  public PythonIpcResult run(int rounds) throws Exception {
    ProcessBuilder pb = new ProcessBuilder("python", SCRIPT_PATH, String.valueOf(rounds));
    pb.redirectErrorStream(false);
    Process p = pb.start();

    double queueMs = 0, pipeMs = 0, sharedMemoryMs = 0;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("Queue:")) {
          queueMs = Double.parseDouble(line.split(":")[1].trim());
        } else if (line.startsWith("Pipe:")) {
          pipeMs = Double.parseDouble(line.split(":")[1].trim());
        } else if (line.startsWith("SharedMemory:")) {
          sharedMemoryMs = Double.parseDouble(line.split(":")[1].trim());
        }
      }
    }
    p.waitFor();
    return new PythonIpcResult(queueMs, pipeMs, sharedMemoryMs);
  }
}

