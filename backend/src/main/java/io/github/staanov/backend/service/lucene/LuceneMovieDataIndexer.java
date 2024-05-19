package io.github.staanov.backend.service.lucene;

import io.github.staanov.backend.model.Movie;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class LuceneMovieDataIndexer {
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String DATE = "date";
  public static final String GENRES = "genres";
  public static final String IMDBID = "imdbid";
  public static final String POSTER = "poster";

  private final Path pathToIndex;
  public static FacetsConfig facetsConfig = new FacetsConfig();

  public LuceneMovieDataIndexer(String directoryPath) {
    this.pathToIndex = Paths.get(directoryPath);
  }

  static {
    facetsConfig.setMultiValued(LuceneMovieDataIndexer.GENRES, true);
    facetsConfig.setRequireDimCount(LuceneMovieDataIndexer.GENRES, true);
  }

  public void index(List<Movie> movies) throws IOException {
    Directory dir = FSDirectory.open(pathToIndex);

    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    config.setRAMBufferSizeMB(256);
    IndexWriter writer = new IndexWriter(dir, config);

    addDocument(movies.iterator(), writer);
    writer.commit();
    writer.close();
  }

  private void addDocument(Iterator<Movie> movieIterator, IndexWriter writer) throws IOException {
    while (movieIterator.hasNext()) {
      writer.addDocument(createDocument(movieIterator.next()));
    }
  }

  private Document createDocument(Movie movie) throws IOException {
    IndexableField id = new StoredField(ID, String.valueOf(movie.getId()));
    IndexableField name = new TextField(NAME, movie.getName(), Field.Store.YES);
    IndexableField releasedDate = new StoredField(DATE, movie.getReleasedDate());
    IndexableField imdbId = new StoredField(IMDBID, String.valueOf(movie.getImdbId()));
    IndexableField posterLink = new StoredField(POSTER, movie.getPoster());

    Document doc = new Document();
    doc.add(id);
    doc.add(name);
    doc.add(releasedDate);
    doc.add(imdbId);
    doc.add(posterLink);

    for (String genre : movie.getGenres()) {
      if (genre != null && !genre.isEmpty()) {
        IndexableField genresFacets = new SortedSetDocValuesFacetField(GENRES, genre);
        IndexableField genres = new TextField(GENRES, genre, Field.Store.YES);
        doc.add(genresFacets);
        doc.add(genres);
      }
    }

    return facetsConfig.build(doc);
  }
}
