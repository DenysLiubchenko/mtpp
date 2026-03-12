package edu.kpi.lab.lab2.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class HtmlDocsGenerator {

  private static final int DEFAULT_COUNT = 1200;
  private static final String DEFAULT_OUTPUT_DIR = "src/main/resources/html_docs";

  private static final String[] BLOCK_TAGS = {
    "div", "section", "article", "header", "footer", "nav",
    "main", "aside", "ul", "ol", "table", "form", "figure"
  };

  private static final String[] INLINE_TAGS = {
    "span", "a", "strong", "em", "b", "i", "code",
    "label", "abbr", "cite", "mark", "small", "sub", "sup"
  };

  private static final String[] LEAF_TAGS = {
    "p", "h1", "h2", "h3", "h4", "h5", "h6",
    "li", "td", "th", "dt", "dd", "figcaption", "caption",
    "button", "option", "textarea", "legend"
  };

  private static final String[] VOID_TAGS = {
    "img", "input", "br", "hr", "meta", "link"
  };

  private static final String[] WORDS = {
    "lorem", "ipsum", "dolor", "sit", "amet", "consectetur",
    "adipiscing", "elit", "sed", "eiusmod", "tempor", "incididunt",
    "labore", "dolore", "magna", "aliqua", "veniam", "quis",
    "nostrud", "exercitation", "ullamco", "laboris", "nisi",
    "aliquip", "commodo", "consequat", "duis", "aute", "irure",
    "reprehenderit", "voluptate", "velit", "esse", "cillum",
    "fugiat", "nulla", "pariatur", "java", "html", "web",
    "data", "code", "test", "lab", "kpi", "matrix", "array",
    "thread", "parallel", "async", "stream", "lambda", "class"
  };

  private final Random random = new Random();

  public void generate() throws IOException {
    generate(DEFAULT_COUNT, Path.of(DEFAULT_OUTPUT_DIR));
  }

  public void generate(int count, Path outputDir) throws IOException {
    Files.createDirectories(outputDir);
    for (int i = 0; i < count; i++) {
      String content = buildDocument(sampleSectionCount());
      Path file = outputDir.resolve("doc_" + i + ".html");
      Files.writeString(file, content);
    }
    System.out.println("Generated " + count + " HTML documents in: " + outputDir.toAbsolutePath());
  }

  private int sampleSectionCount() {
    double lambda = 0.15;
    double u = random.nextDouble();
    int sample = (int) (-Math.log(1.0 - u) / lambda);
    return Math.max(1, Math.min(40, sample));
  }

  private String buildDocument(int sectionCount) {
    StringBuilder sb = new StringBuilder(512);
    sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
    sb.append("  <meta charset=\"UTF-8\">\n");
    sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
    sb.append("  <title>").append(randomPhrase(3)).append("</title>\n");
    if (random.nextBoolean()) {
      sb.append("  <link rel=\"stylesheet\" href=\"style.css\">\n");
    }
    sb.append("</head>\n<body>\n");

    sb.append("<header>\n  <h1>").append(randomPhrase(4)).append("</h1>\n");
    if (random.nextBoolean()) {
      sb.append("  <nav>\n").append(buildNavList()).append("  </nav>\n");
    }
    sb.append("</header>\n");

    sb.append("<main>\n");
    for (int i = 0; i < sectionCount; i++) {
      sb.append(buildSection());
    }
    sb.append("</main>\n");

    if (random.nextInt(3) == 0) {
      sb.append(buildTable(2 + random.nextInt(5), 2 + random.nextInt(5)));
    }

    if (random.nextInt(4) == 0) {
      sb.append(buildForm());
    }

    sb.append("<footer>\n  <p>").append(randomPhrase(5)).append("</p>\n</footer>\n");
    sb.append("</body>\n</html>");
    return sb.toString();
  }

  private String buildSection() {
    String tag = BLOCK_TAGS[random.nextInt(BLOCK_TAGS.length)];
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(tag).append(">\n");

    String hTag = "h" + (2 + random.nextInt(4));
    sb.append("  <").append(hTag).append(">").append(randomPhrase(3))
      .append("</").append(hTag).append(">\n");

    int childCount = 2 + random.nextInt(6);
    for (int i = 0; i < childCount; i++) {
      sb.append(buildLeafElement());
    }

    if (random.nextInt(4) == 0) {
      String inner = BLOCK_TAGS[random.nextInt(BLOCK_TAGS.length)];
      sb.append("  <").append(inner).append(">\n");
      sb.append("    ").append(buildLeafElement());
      sb.append("  </").append(inner).append(">\n");
    }

    int voidCount = random.nextInt(3);
    for (int i = 0; i < voidCount; i++) {
      String vt = VOID_TAGS[random.nextInt(VOID_TAGS.length)];
      sb.append("  <").append(vt).append(" />\n");
    }

    sb.append("</").append(tag).append(">\n");
    return sb.toString();
  }

  private String buildLeafElement() {
    String tag = LEAF_TAGS[random.nextInt(LEAF_TAGS.length)];
    String content = buildInlineContent(3 + random.nextInt(10));
    return "  <" + tag + ">" + content + "</" + tag + ">\n";
  }

  private String buildInlineContent(int wordCount) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < wordCount; i++) {
      if (random.nextInt(5) == 0) {
        String it = INLINE_TAGS[random.nextInt(INLINE_TAGS.length)];
        sb.append("<").append(it).append(">")
          .append(randomWord())
          .append("</").append(it).append("> ");
      } else {
        sb.append(randomWord()).append(" ");
      }
    }
    return sb.toString().trim();
  }

  private String buildNavList() {
    StringBuilder sb = new StringBuilder("    <ul>\n");
    int items = 3 + random.nextInt(5);
    for (int i = 0; i < items; i++) {
      sb.append("      <li><a href=\"#\">").append(randomWord()).append("</a></li>\n");
    }
    return sb.append("    </ul>\n").toString();
  }

  private String buildTable(int rows, int cols) {
    StringBuilder sb = new StringBuilder("<table>\n  <thead>\n    <tr>\n");
    for (int c = 0; c < cols; c++) {
      sb.append("      <th>").append(randomWord()).append("</th>\n");
    }
    sb.append("    </tr>\n  </thead>\n  <tbody>\n");
    for (int r = 0; r < rows; r++) {
      sb.append("    <tr>\n");
      for (int c = 0; c < cols; c++) {
        sb.append("      <td>").append(randomWord()).append("</td>\n");
      }
      sb.append("    </tr>\n");
    }
    return sb.append("  </tbody>\n</table>\n").toString();
  }

  private String buildForm() {
    StringBuilder sb = new StringBuilder("<form>\n");
    int fields = 2 + random.nextInt(4);
    for (int i = 0; i < fields; i++) {
      String name = randomWord();
      sb.append("  <label for=\"").append(name).append("\">").append(name).append("</label>\n");
      sb.append("  <input id=\"").append(name).append("\" type=\"text\" />\n");
    }
    sb.append("  <button type=\"submit\">Submit</button>\n</form>\n");
    return sb.toString();
  }

  private String randomWord() {
    return WORDS[random.nextInt(WORDS.length)];
  }

  private String randomPhrase(int wordCount) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < wordCount; i++) {
      sb.append(randomWord());
      if (i < wordCount - 1) {
        sb.append(' ');
      }
    }
    return sb.toString();
  }
}
