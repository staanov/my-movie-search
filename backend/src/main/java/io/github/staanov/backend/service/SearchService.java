package io.github.staanov.backend.service;


import io.github.staanov.backend.model.Movie;
import io.github.staanov.backend.model.MovieQuery;
import io.github.staanov.backend.service.lucene.Searcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

  public static final String LUCENE_SERVICE = "LuceneService";

  private SearchFactory searchFactory;

  public SearchService(SearchFactory searchFactory) {
    this.searchFactory = searchFactory;
  }

  public List<Movie> searchDocumentsOnLucene(MovieQuery movieQuery) {
    Searcher searcher = searchFactory.get(LUCENE_SERVICE);
    return searcher.searchDocuments(movieQuery);
  }

  public Map<String, Object> searchDocumentsOnLuceneWithFacets(MovieQuery movieQuery) {
    Searcher searcher = searchFactory.get(LUCENE_SERVICE);
    return searcher.searchDocumentsWithFacets(movieQuery);
  }
}
