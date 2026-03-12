package edu.kpi.lab.lab2.tagFrequency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TagFrequencyStatistics {

  public Map<String, Long> sequential(Path htmlDir) throws IOException {
    List<Path> files = listHtmlFiles(htmlDir);
    Map<String, Long> result = new HashMap<>();
    for (Path file : files) {
      countTagsInFile(file).forEach((tag, cnt) -> result.merge(tag, cnt, Long::sum));
    }
    return result;
  }

  public Map<String, Long> mapReduce(Path htmlDir) throws IOException {
    List<Path> files = listHtmlFiles(htmlDir);

    return files.parallelStream()
      .map(TagFrequencyStatistics::countTagsInFile)
      .reduce(new HashMap<>(), (a, b) -> {
        Map<String, Long> merged = new HashMap<>(a);
        b.forEach((tag, cnt) -> merged.merge(tag, cnt, Long::sum));
        return merged;
      });
  }

  public Map<String, Long> forkJoin(Path htmlDir) throws IOException {
    List<Path> files = listHtmlFiles(htmlDir);
    ForkJoinPool pool = ForkJoinPool.commonPool();
    return pool.invoke(new TagCountTask(files, 0, files.size()));

  }

  private static final int THRESHOLD = 50;

  private static class TagCountTask extends RecursiveTask<Map<String, Long>> {

    private final List<Path> files;
    private final int from;
    private final int to;

    TagCountTask(List<Path> files, int from, int to) {
      this.files = files;
      this.from = from;
      this.to = to;
    }

    @Override
    protected Map<String, Long> compute() {
      int size = to - from;

      if (size <= THRESHOLD) {
        Map<String, Long> result = new HashMap<>();
        for (int i = from; i < to; i++) {
          countTagsInFile(files.get(i))
            .forEach((tag, cnt) -> result.merge(tag, cnt, Long::sum));
        }
        return result;
      }

      int mid = from + size / 2;
      TagCountTask left = new TagCountTask(files, from, mid);
      TagCountTask right = new TagCountTask(files, mid, to);

      left.fork();
      Map<String, Long> rightResult = right.compute();
      Map<String, Long> leftResult = left.join();

      Map<String, Long> merged = new HashMap<>(leftResult);
      rightResult.forEach((tag, cnt) -> merged.merge(tag, cnt, Long::sum));
      return merged;
    }
  }

  public Map<String, Long> workerPool(Path htmlDir) throws IOException, InterruptedException {
    List<Path> files = listHtmlFiles(htmlDir);
    int threads = Runtime.getRuntime().availableProcessors();
    ExecutorService pool = Executors.newFixedThreadPool(threads);

    List<Future<Map<String, Long>>> futures = new ArrayList<>(files.size());
    for (Path file : files) {
      futures.add(pool.submit(() -> countTagsInFile(file)));
    }

    pool.shutdown();

    Map<String, Long> result = new ConcurrentHashMap<>();
    for (Future<Map<String, Long>> future : futures) {
      try {
        future.get().forEach((tag, cnt) -> result.merge(tag, cnt, Long::sum));
      } catch (Exception e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Worker pool task failed", e);
      }
    }
    return result;

  }

  static Map<String, Long> countTagsInFile(Path file) {
    Map<String, Long> freq = new HashMap<>();
    try {
      Document doc = Jsoup.parse(file.toFile(), "UTF-8");
      for (Element el : doc.getAllElements()) {
        String tag = el.tagName().toLowerCase();
        freq.merge(tag, 1L, Long::sum);
      }
    } catch (IOException e) {
      System.err.println("Cannot read file: " + file + " — " + e.getMessage());
    }
    return freq;
  }

  static List<Path> listHtmlFiles(Path dir) throws IOException {
    try (Stream<Path> stream = Files.list(dir)) {
      return stream
        .filter(p -> p.toString().endsWith(".html"))
        .sorted()
        .collect(Collectors.toList());
    }
  }
}
