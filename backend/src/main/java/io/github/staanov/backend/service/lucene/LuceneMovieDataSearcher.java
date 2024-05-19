package io.github.staanov.backend.service.lucene;

import io.github.staanov.backend.dto.FacetedSearchDto;
import io.github.staanov.backend.model.Movie;
import io.github.staanov.backend.model.MovieQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuceneMovieDataSearcher {
  public static final String RESULT = "result";
  public static final String FACETS = "facets";

  private final Directory dir;
  private final IndexSearcher searcher;
  private final int limit;
  private final int threshold;

  public LuceneMovieDataSearcher(String path, int limit, int threshold) throws IOException {
    this.dir = FSDirectory.open(Paths.get(path));
    DirectoryReader reader = DirectoryReader.open(dir);
    this.searcher = new IndexSearcher(reader);
    this.limit = limit;
    this.threshold = threshold;
  }

  public List<Movie> search(MovieQuery movieQuery) throws IOException {
    Query query = getQuery(movieQuery);

    TopDocs result = searcher.search(query, limit);

    if (result.totalHits.value == 0) {
      return new ArrayList<>();
    }

    return getMovies(result);
  }

  public FacetedSearchDto searchDocumentsWithFacets(MovieQuery movieQuery) throws IOException {
    BooleanQuery.Builder builder = buildQuery(movieQuery);

    List<String> genres = movieQuery.getGenres();

    if (genres != null && !genres.isEmpty()) {
      DrillDownMustQuery drillDownQuery = new DrillDownMustQuery(LuceneMovieDataIndexer.facetsConfig);
      for (String genre : genres) {
        drillDownQuery.add(LuceneMovieDataIndexer.GENRES, genre);
      }
      builder.add(drillDownQuery, BooleanClause.Occur.FILTER);
    }

    Query query = builder.build();

    FacetsCollector facetsCollector = new FacetsCollector(true);
    TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(limit, threshold);
    searcher.search(query, MultiCollector.wrap(topScoreDocCollector, facetsCollector));

    TopDocs result = topScoreDocCollector.topDocs();

    if (result.totalHits.value == 0) {
      return getEmptySearchResult();
    }

    List<Movie> movies = getMovies(result);
    SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader(),
        LuceneMovieDataIndexer.facetsConfig);

    Facets facets = new SortedSetDocValuesFacetCounts(state, facetsCollector);
    FacetResult facetResult = facets.getAllChildren(LuceneMovieDataIndexer.GENRES);

    FacetedSearchDto dto = new FacetedSearchDto();
    dto.setResult(movies);
    dto.setFacets(facetResult);
    return dto;
  }

  public FacetedSearchDto getEmptySearchResult() {
    FacetedSearchDto dto = new FacetedSearchDto();
    dto.setResult(new ArrayList<>());
    dto.setFacets(new FacetResult("", new String[]{}, 0, new LabelAndValue[]{}, 0));
    return dto;
  }

  private List<Movie> getMovies(TopDocs result) throws IOException {
    List<Movie> movies = new ArrayList<>();
    StoredFields fields = searcher.storedFields();

    for (ScoreDoc doc : result.scoreDocs) {
      Document document = fields.document(doc.doc);
      movies.add(getMovie(document));
    }
    return movies;
  }

  private Query getQuery(MovieQuery movieQuery) {
    if (movieQuery != null) {
      return buildQuery(movieQuery).build();
    }
    return new MatchAllDocsQuery();
  }

  private BooleanQuery.Builder buildQuery(MovieQuery movieQuery) {
    List<String> tokens = tokenizeQuery(new StandardAnalyzer(), movieQuery.getSearchString());
    BooleanQuery.Builder builder = new BooleanQuery.Builder();

    if (tokens.isEmpty()) {
      builder.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);
    }

    for (String token : tokens) {
      builder.add(new TermQuery(new Term(LuceneMovieDataIndexer.NAME, token)), BooleanClause.Occur.MUST);
    }

    return builder;
  }

  private List<String> tokenizeQuery(Analyzer analyzer, String query) {
    List<String> result = new ArrayList<>();
    try {
      TokenStream stream = analyzer.tokenStream(null, new StringReader(query));
      stream.reset();
      while (stream.incrementToken()) {
        result.add(stream.getAttribute(CharTermAttribute.class).toString());
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }
    return result;
  }

  private Movie getMovie(Document document) {
    Movie movie = new Movie();
    movie.setId(Integer.parseInt(document.get(LuceneMovieDataIndexer.ID)));
    movie.setName(document.get(LuceneMovieDataIndexer.NAME));
    movie.setReleasedDate(document.get(LuceneMovieDataIndexer.DATE));
    movie.setGenres(Arrays.asList(document.getValues(LuceneMovieDataIndexer.GENRES)));
    movie.setImdbId(Integer.parseInt(document.get(LuceneMovieDataIndexer.IMDBID)));
    movie.setPoster(document.get(LuceneMovieDataIndexer.POSTER));
    return movie;
  }
}
