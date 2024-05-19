package io.github.staanov.backend.service.lucene;

import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DrillDownMustQuery extends Query {

  private final FacetsConfig config;
  private final Query query;
  private final List<BooleanQuery.Builder> dimQueryBuilders = new ArrayList<>();
  private final Map<String, Integer> drillDownDims = new LinkedHashMap<>();
  private final List<Query> dimQueries = new ArrayList<>();
  private final Set<Integer> dirtyDimQueryIndex = new HashSet<>();

  public DrillDownMustQuery(FacetsConfig config,
                            Query query,
                            List<BooleanQuery.Builder> dimQueryBuilders,
                            Map<String, Integer> drillDownDims) {
    this.config = config;
    this.query = query;
    this.dimQueryBuilders.addAll(dimQueryBuilders);
    this.drillDownDims.putAll(drillDownDims);
    for (int i = 0; i < this.dimQueryBuilders.size(); i++) {
      this.dimQueries.add(null);
      this.dirtyDimQueryIndex.add(i);
    }
  }

  public DrillDownMustQuery(FacetsConfig config, Query query) {
    this.config = config;
    this.query = query;
  }

  public DrillDownMustQuery(FacetsConfig config) {
    this(config, null);
  }

  public static Term term(String field, String dim, String... path) {
    return new Term(field, FacetsConfig.pathToString(dim, path));
  }

  public void add(String dim, Query subQuery) {
    assert dimQueryBuilders.size() == dimQueries.size();
    assert drillDownDims.size() == dimQueryBuilders.size();
    if (!drillDownDims.containsKey(dim)) {
      drillDownDims.put(dim, drillDownDims.size());
      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      dimQueryBuilders.add(builder);
      dimQueries.add(null);
    }
    int index = drillDownDims.get(dim);
    dimQueryBuilders.get(index).add(subQuery, BooleanClause.Occur.MUST);
    dirtyDimQueryIndex.add(index);
  }

  public void add(String dim, String... path) {
    String indexedField = config.getDimConfig(dim).indexFieldName;
    add(dim, new TermQuery(term(indexedField, dim, path)));
  }

  @Override
  public String toString(String s) {
    return null;
  }

  @Override
  public void visit(QueryVisitor queryVisitor) {

  }

  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
