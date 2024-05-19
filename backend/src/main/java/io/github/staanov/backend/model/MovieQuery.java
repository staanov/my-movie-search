package io.github.staanov.backend.model;

import java.util.List;

public class MovieQuery {

  private String searchString;
  private List<String> genres;

  public MovieQuery() {
  }

  public MovieQuery(String searchString, List<String> genres) {
    this.searchString = searchString;
    this.genres = genres;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }
}
