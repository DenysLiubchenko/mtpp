package edu.kpi.lab.lab3.ipc;

public record IpcResult(
  String method,
  long sent,
  long received,
  long elapsedNs,
  long loggedValue
) {
  public double elapsedMs() {
    return elapsedNs / 1_000_000.0;
  }

  public boolean isCorrect() {
    return sent == received && sent == loggedValue;
  }
}
