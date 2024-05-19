package io.github.staanov.backend.load;

import io.github.staanov.backend.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MovieDataLoader {
  private static final String NO_POSTER = "https://movienewsletters.net/photos/000000h1.jpg";
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieDataLoader.class);

  @Value("${search.movie.list}")
  private Resource movieResource;
  private Map<Integer, Movie> moviesMap;
  private Pattern releasedDatePattern;

  public MovieDataLoader() {
    this.moviesMap = new HashMap<>();
    this.releasedDatePattern = Pattern.compile("[(0-9)]{6}$");
  }

  public List<Movie> getMovies() throws IOException {
    if (moviesMap != null && !moviesMap.isEmpty()) {
      return new ArrayList<>(moviesMap.values());
    }
    load();
    return new ArrayList<>(moviesMap.values());
  }

  private void load() throws IOException {
    loadMovieFromFile(movieResource.getFile());
  }

  public void setMovieFile(String movieFile) {
    this.movieResource = new ClassPathResource(movieFile);
  }

  protected void loadMovieFromFile(File file) {
    BufferedReader reader;
    Map<Integer, Movie> newMovies = new HashMap<>();
    try {
      reader = new BufferedReader(new FileReader(file));
      String line = reader.readLine();
      while (line != null) {
        try {
          Movie movie = extractMovie(line);
          newMovies.put(movie.getId(), movie);
          line = reader.readLine();
        } catch (Exception e) {
          LOGGER.error("Can't extract movie");
          line = reader.readLine();
        }
      }
      reader.close();
    } catch (Exception e) {
      LOGGER.error("Can't read file: {}", e.getMessage());
    }
    moviesMap.clear();
    moviesMap.putAll(newMovies);
  }

  protected Movie extractMovie(String movieString) {
    String quoteReplace = "(^\")|(\"$)";
    String[] split = split(movieString);
    String[] genres = split[4].split("[|]");
    String id = split[0];
    String poster = NO_POSTER;
    if (split.length > 5) {
      poster = split[5];
    }
    String releasedDate = getReleasedDate(split[2].replaceAll(quoteReplace, ""));
    String movieName = split[2].replaceAll(quoteReplace, "");
    if (!releasedDate.isEmpty()) {
      try {
        movieName = movieName.substring(0, movieName.indexOf("(" + releasedDate + ")")).trim();
      } catch (Exception e) {
        LOGGER.error("Can't parse: " + movieName);
      }
    }

    Movie movie = new Movie();
    movie.setId(Integer.parseInt(id));
    movie.setName(movieName);
    movie.setReleasedDate(releasedDate);
    movie.setGenres(Arrays.stream(genres).map(s -> s.replace("\r", "")).collect(Collectors.toList()));
    movie.setImdbId(movie.getId());
    movie.setPoster(poster);
    return movie;
  }

  protected String[] split(String name) {
    List<String> tokens = new ArrayList<>();
    boolean quotes = false;
    StringBuilder builder = new StringBuilder();
    for (char ch : name.toCharArray()) {
      switch (ch) {
        case ',':
          if (quotes) {
            builder.append(ch);
          } else {
            tokens.add(builder.toString());
            builder = new StringBuilder();
          }
          break;
        case '\"':
          quotes = !quotes;
          break;
        default:
          builder.append(ch);
          break;
      }
    }
    if (!builder.isEmpty())
      tokens.add(builder.toString());
    return tokens.toArray(new String[0]);
  }

  private String getReleasedDate(String movieNameWithReleasedDate) {
    Matcher matcher = releasedDatePattern.matcher(movieNameWithReleasedDate);
    if (matcher.find()) {
      String matched = matcher.group();
      return matched.substring(1, matched.length() - 1);
    }
    return "";
  }
}
