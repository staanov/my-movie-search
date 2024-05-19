package io.github.staanov.backend.service.lucene;

import io.github.staanov.backend.dto.FacetedSearchDto;
import io.github.staanov.backend.model.Movie;
import io.github.staanov.backend.model.MovieQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuceneService implements Searcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneService.class);

  private final LuceneMovieDataSearcher searcher;

  public LuceneService(String path, int limit, int threshold) throws IOException {
    this.searcher = new LuceneMovieDataSearcher(path, limit, threshold);
  }

  @Override
  public List<Movie> searchDocuments(MovieQuery movieQuery) {
    try {
      return searcher.search(movieQuery);
    } catch (IOException e) {
      LOGGER.error("Can't search documents: {}", e.getMessage());
    }
    return new ArrayList<>();
  }

  @Override
  public FacetedSearchDto searchDocumentsWithFacets(MovieQuery movieQuery) {
    try {
      return searcher.searchDocumentsWithFacets(movieQuery);
    } catch (IOException e) {
      LOGGER.error("Can't search documents with facets: {}", e.getMessage());
    }
    return new FacetedSearchDto();
  }
}
