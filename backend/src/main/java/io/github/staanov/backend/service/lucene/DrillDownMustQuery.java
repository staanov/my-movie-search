package io.github.staanov.backend.service.lucene;

import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DrillDownMustQuery extends Query {

  private final FacetsConfig config;
  private final Query baseQuery;
  private final List<BooleanQuery.Builder> dimQueryBuilders = new ArrayList<>();
  private final Map<String, Integer> drillDownDims = new LinkedHashMap<>();
  private final List<Query> dimQueries = new ArrayList<>();
  private final Set<Integer> dirtyDimQueryIndex = new HashSet<>();

  public DrillDownMustQuery(FacetsConfig config,
                            Query baseQuery,
                            List<BooleanQuery.Builder> dimQueryBuilders,
                            Map<String, Integer> drillDownDims) {
    this.config = config;
    this.baseQuery = baseQuery;
    this.dimQueryBuilders.addAll(dimQueryBuilders);
    this.drillDownDims.putAll(drillDownDims);
    for (int i = 0; i < this.dimQueryBuilders.size(); i++) {
      this.dimQueries.add(null);
      this.dirtyDimQueryIndex.add(i);
    }
  }

  public DrillDownMustQuery(FacetsConfig config, Query baseQuery) {
    this.config = config;
    this.baseQuery = baseQuery;
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
    return getBooleanQuery().toString(s);
  }

  @Override
  public void visit(QueryVisitor queryVisitor) {
    queryVisitor.visitLeaf(this);
  }

  @Override
  public boolean equals(Object o) {
    return sameClassAs(o) && equalsTo(getClass().cast(o));
  }

  private boolean equalsTo(DrillDownMustQuery other) {
    return Objects.equals(baseQuery, other.baseQuery) && dimQueryBuilders.equals(other.dimQueryBuilders);
  }

  @Override
  public int hashCode() {
    return classHash() + Objects.hash(baseQuery, dimQueryBuilders);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new DrillDownMustQuery(config, baseQuery, dimQueryBuilders, drillDownDims);
  }

  @Override
  public Query rewrite(IndexReader reader) {
    BooleanQuery rewritten = getBooleanQuery();
    if (rewritten.clauses().isEmpty()) {
      return new MatchAllDocsQuery();
    }
    return rewritten;
  }

  private BooleanQuery getBooleanQuery() {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    if (baseQuery != null) {
      builder.add(baseQuery, BooleanClause.Occur.MUST);
    }

    for (Query query : getDrillDownQueries()) {
      builder.add(query, BooleanClause.Occur.FILTER);
    }

    return builder.build();
  }

  public Query[] getDrillDownQueries() {
    for (Integer dirtyDimIndex : dirtyDimQueryIndex) {
      dimQueries.set(dirtyDimIndex, this.dimQueryBuilders.get(dirtyDimIndex).build());
    }
    dirtyDimQueryIndex.clear();

    return dimQueries.toArray(new Query[0]);
  }
}
