package io.github.staanov.backend.model;

import java.util.List;

public class Movie {
  private int id;
  private String releasedDate;
  private String name;
  private List<String> genres;
  private int imdbId;
  private String poster;

  public Movie() {
  }

  public Movie(int id, String releasedDate, String name, List<String> genres, int imdbId, String poster) {
    this.id = id;
    this.releasedDate = releasedDate;
    this.name = name;
    this.genres = genres;
    this.imdbId = imdbId;
    this.poster = poster;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getReleasedDate() {
    return releasedDate;
  }

  public void setReleasedDate(String releasedDate) {
    this.releasedDate = releasedDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  public int getImdbId() {
    return imdbId;
  }

  public void setImdbId(int imdbId) {
    this.imdbId = imdbId;
  }

  public String getPoster() {
    return poster;
  }

  public void setPoster(String poster) {
    this.poster = poster;
  }
}
