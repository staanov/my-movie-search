package io.github.staanov.backend.dto;

import io.github.staanov.backend.model.Movie;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.LabelAndValue;

import java.util.ArrayList;
import java.util.List;

public class FacetedSearchDto {
  private List<Movie> result;
  private FacetResult facets;

  public FacetedSearchDto() {
    this.result = new ArrayList<>();
    this.facets = new FacetResult("", new String[]{}, 0, new LabelAndValue[]{}, 0);
  }

  public FacetedSearchDto(List<Movie> result, FacetResult facets) {
    this.result = result;
    this.facets = facets;
  }

  public List<Movie> getResult() {
    return result;
  }

  public void setResult(List<Movie> result) {
    this.result = result;
  }

  public FacetResult getFacets() {
    return facets;
  }

  public void setFacets(FacetResult facets) {
    this.facets = facets;
  }
}
