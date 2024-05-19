package io.github.staanov.backend;

import io.github.staanov.backend.load.MovieDataLoader;
import io.github.staanov.backend.model.Movie;
import io.github.staanov.backend.service.lucene.LuceneMovieDataIndexer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

public class IndexHelperTest {
  private static final String PATH = "/home/stanislav/Documents/index";
  private static final String FILE = "movie_genre_poster.csv";

  @Test
  public void prepareIndex() throws IOException {
    LuceneMovieDataIndexer indexer = new LuceneMovieDataIndexer(PATH);
    MovieDataLoader loader = new MovieDataLoader();
    loader.setMovieFile(FILE);
    List<Movie> movies = loader.getMovies();
    indexer.index(movies);
  }
}
