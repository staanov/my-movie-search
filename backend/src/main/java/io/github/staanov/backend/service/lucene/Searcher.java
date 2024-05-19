package io.github.staanov.backend.service.lucene;

import io.github.staanov.backend.dto.FacetedSearchDto;
import io.github.staanov.backend.model.Movie;
import io.github.staanov.backend.model.MovieQuery;

import java.util.List;
import java.util.Map;

public interface Searcher {

  List<Movie> searchDocuments(MovieQuery movieQuery);

  FacetedSearchDto searchDocumentsWithFacets(MovieQuery movieQuery);
}
