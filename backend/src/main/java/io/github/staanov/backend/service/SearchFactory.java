package io.github.staanov.backend.service;

import io.github.staanov.backend.service.lucene.LuceneService;
import io.github.staanov.backend.service.lucene.Searcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SearchFactory {
  private final Map<String, Searcher> searches;

  public SearchFactory(@Value("${lucene.dir.path}") String path,
                       @Value("${lucene.result.limit}") int limit,
                       @Value("${lucene.result.threshold}") int threshold) throws IOException {
    searches = new HashMap<>();
    searches.put(LuceneService.class.getSimpleName(), new LuceneService(path, limit, threshold));
  }

  public boolean add(String key, Searcher search) {
    if (searches.containsKey(key)) {
      return false;
    }
    searches.put(key, search);
    return true;
  }

  public Searcher get(String key) {
    if (!searches.containsKey(key)) {
      throw new RuntimeException("Key not found");
    }
    return searches.get(key);
  }
}
