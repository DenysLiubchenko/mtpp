package edu.kpi.lab.lab1.ioBoundTasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class WordCounter {

  public long countWordsInDirectory(String directoryPath, int numberOfThreads) {
    File directory = new File(directoryPath);
    if (!directory.exists() || !directory.isDirectory()) {
      throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
    }

    List<File> textFiles = new ArrayList<>();
    collectTextFiles(directory, textFiles);

    if (textFiles.isEmpty()) {
      return 0;
    }

    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
    AtomicLong totalWords = new AtomicLong(0);

    try {
      int batchSize = textFiles.size() / numberOfThreads;
      int remainder = textFiles.size() % numberOfThreads;

      List<Future<Long>> futures = new ArrayList<>(numberOfThreads);
      int startIndex = 0;

      for (int i = 0; i < numberOfThreads; i++) {
        int extra = i < remainder ? 1 : 0;
        int endIndex = startIndex + batchSize + extra;

        List<File> batch = textFiles.subList(startIndex, endIndex);

        Callable<Long> task = () -> countWordsInFiles(batch);

        startIndex = endIndex;
        futures.add(executor.submit(task));
      }

      for (Future<Long> future : futures) {
        try {
          totalWords.addAndGet(future.get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Word counting interrupted", e);
        } catch (ExecutionException e) {
          throw new RuntimeException("Word counting failed", e);
        }
      }

      return totalWords.get();

    } finally {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  private void collectTextFiles(File directory, List<File> textFiles) {
    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }

    for (File file : files) {
      if (file.isFile() && file.getName().endsWith(".txt")) {
        textFiles.add(file);
      }
    }
  }

  private long countWordsInFiles(List<File> files) {
    long count = 0;
    for (File file : files) {
      count += countWordsInFile(file);
    }
    return count;
  }

  private long countWordsInFile(File file) {
    long count = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] words = line.trim().split("\\s+");
        if (words.length == 1 && words[0].isEmpty()) {
          continue;
        }
        count += words.length;
      }
    } catch (IOException e) {
      System.err.println("Error reading file: " + file.getAbsolutePath());
    }
    return count;
  }
}
