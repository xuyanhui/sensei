package com.sensei.search.query;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.facets.FacetHandler;
import com.sensei.indexing.api.DefaultSenseiInterpreter;
import com.sensei.indexing.api.MetaType;

public class TimeRetentionFilter extends Filter {

  private final String _column;
  private final int _nDays;
  private final TimeUnit _dataUnit;
  
  static{
    DefaultSenseiInterpreter.DEFAULT_FORMAT_STRING_MAP.get(MetaType.Long);
  }
  
  public TimeRetentionFilter(String column,int nDays,TimeUnit dataUnit){
    _column = column;
    _nDays = nDays;
    _dataUnit = dataUnit;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    if (reader instanceof BoboIndexReader){
      final BoboIndexReader boboReader = (BoboIndexReader)reader;
      FacetHandler facetHandler = boboReader.getFacetHandler(_column);
      
      if (facetHandler!=null){
        DecimalFormat formatter = new DecimalFormat(DefaultSenseiInterpreter.DEFAULT_FORMAT_STRING_MAP.get(MetaType.Long), new DecimalFormatSymbols(Locale.US));
        BrowseSelection sel = new BrowseSelection(_column);
        long duration = _dataUnit.convert(_nDays, TimeUnit.DAYS);
        long now = _dataUnit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        long from = now - duration;
        sel.addValue("["+formatter.format(from)+" TO *]");
        return facetHandler.buildFilter(sel).getDocIdSet(boboReader);
      }
      throw new IllegalStateException("no facet handler defined with column: "+_column);
    }
    else{
      throw new IllegalStateException("reader not instance of "+BoboIndexReader.class);
    }
  }

}
