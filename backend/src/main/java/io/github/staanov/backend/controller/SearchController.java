package io.github.staanov.backend.controller;

import io.github.staanov.backend.dto.FacetedSearchDto;
import io.github.staanov.backend.model.MovieQuery;
import io.github.staanov.backend.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "*")
@RequestMapping("/api/v1/search")
public class SearchController {

  private final SearchService searchService;

  public SearchController(SearchService searchService) {
    this.searchService = searchService;
  }

  @PostMapping
  public ResponseEntity<FacetedSearchDto> searchInLuceneWithFacets(@RequestBody MovieQuery movieQuery) {
    if (movieQuery == null) {
      return ResponseEntity.ok().build();
    }
    if (movieQuery.getSearchString() == null) {
      movieQuery.setSearchString("");
    }
    return ResponseEntity.ok(searchService.searchDocumentsOnLuceneWithFacets(movieQuery));
  }
}
