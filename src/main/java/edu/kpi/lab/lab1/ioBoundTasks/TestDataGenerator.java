package edu.kpi.lab.lab1.ioBoundTasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class TestDataGenerator {

  private static final String[] SAMPLE_WORDS = {
      "the", "quick", "brown", "fox", "jumps", "over", "lazy", "dog",
      "hello", "world", "java", "programming", "parallel", "processing",
      "thread", "executor", "file", "directory", "word", "count",
      "performance", "benchmark", "test", "data", "random", "text",
      "algorithm", "concurrent", "multithreading", "task", "future",
      "callable", "runnable", "pool", "synchronization", "atomic"
  };

  public static void generateTestFiles(String basePath, int numberOfFiles) throws IOException {
    Path baseDir = Paths.get(basePath);

    if (Files.exists(baseDir)) {
      deleteDirectory(baseDir.toFile());
    }

    Files.createDirectories(baseDir);
    Random random = new Random();

    int filesCreated = 0;
    int currentDepth = 0;
    String currentPath = basePath;

    while (filesCreated < numberOfFiles) {
      if (random.nextDouble() < 0.3 && currentDepth < 5 && filesCreated < numberOfFiles - 10) {
        String subDirName = "subdir_" + random.nextInt(10);
        currentPath = currentPath + File.separator + subDirName;
        Files.createDirectories(Paths.get(currentPath));
        currentDepth++;
      }

      int batchSize = Math.min(random.nextInt(95) + 5, numberOfFiles - filesCreated);
      for (int i = 0; i < batchSize; i++) {
        String fileName = "file_" + filesCreated + ".txt";
        File file = new File(currentPath, fileName);
        createTextFile(file, random);
        filesCreated++;
      }

      if (random.nextDouble() < 0.4 && currentDepth > 0) {
        currentPath = new File(currentPath).getParent();
        currentDepth--;
      }
    }

    System.out.println("Generated " + numberOfFiles + " files in " + basePath);
  }

  private static void createTextFile(File file, Random random) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      int numberOfLines = random.nextInt(990) + 10;

      for (int i = 0; i < numberOfLines; i++) {
        int wordsInLine = random.nextInt(990) + 10;
        StringBuilder line = new StringBuilder();

        for (int j = 0; j < wordsInLine; j++) {
          if (j > 0) {
            line.append(" ");
          }
          line.append(SAMPLE_WORDS[random.nextInt(SAMPLE_WORDS.length)]);
        }

        writer.write(line.toString());
        writer.newLine();
      }
    }
  }

  private static void deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    directory.delete();
  }

  public static void cleanupTestFiles(String basePath) {
    File directory = new File(basePath);
    if (directory.exists()) {
      deleteDirectory(directory);
      System.out.println("Cleaned up test directory: " + basePath);
    }
  }
}

