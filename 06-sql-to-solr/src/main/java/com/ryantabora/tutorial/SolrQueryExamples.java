package com.ryantabora.tutorial;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.servlet.SolrRequestParsers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SolrQueryExamples {
  private static final HttpSolrServer solrServer = new HttpSolrServer(
      "http://localhost:8080/solr/stocks");
  private final static String rowKeyPrefix = "test-query-";
  private final static String uniqueField = "rowkey";
  
  @BeforeClass
  public static void setup() throws ParseException, SolrServerException,
      IOException {
    indexDocuments(100);
  }
  
  @AfterClass
  public static void cleanup() throws SolrServerException, IOException {
    solrServer.deleteByQuery(uniqueField + ":" + rowKeyPrefix + "*");
    solrServer.commit();
  }
  
  @Test
  public void testStringQuery() throws SolrServerException {
    StringBuilder query = new StringBuilder();
    query.append("q=*:*");
    query.append("&rows=10");
    query.append("&sort=stock_price_low asc");
    query.append("&fq=stock_price_high:[0 TO 500]");
    query.append("&fq=stock_price_high:[0 TO 200]");
    SolrParams solrParameters = SolrRequestParsers.parseQueryString(query
        .toString());
    QueryResponse solrResponse = solrServer.query(solrParameters);
    SolrDocumentList solrDocList = solrResponse.getResults();
    for (SolrDocument solrDoc : solrDocList) {
      for (Entry<String,Object> kv : solrDoc.entrySet()) {
        System.out.println("The " + kv.getKey() + " is "
            + kv.getValue().toString());
      }
    }
  }
  
  @Test
  public void testSolrQuery() throws SolrServerException {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setRows(10);
    solrQuery.setQuery("*:*");
    solrQuery.setSortField("stock_price_low", ORDER.asc);
    solrQuery.setFilterQueries("stock_price_high:[0 TO 500]");
    solrQuery.addFilterQuery("stock_price_low:[0 TO 200]");
    
    QueryResponse solrResponse = solrServer.query(solrQuery);
    SolrDocumentList solrDocList = solrResponse.getResults();
    for (SolrDocument solrDoc : solrDocList) {
      for (Entry<String,Object> kv : solrDoc.entrySet()) {
        System.out.println("The " + kv.getKey() + " is "
            + kv.getValue().toString());
      }
    }
  }
  
  @Test
  public void testModParamsQuery() throws SolrServerException {
    ModifiableSolrParams modifiableParameters = new ModifiableSolrParams();
    modifiableParameters.set("rows", 10);
    modifiableParameters.set("q", "*:*");
    modifiableParameters.set("sort", "stock_price_low asc");
    modifiableParameters.set("fq", "stock_price_high:[0 TO 500]");
    modifiableParameters.add("fq", "stock_price_low:[0 TO 200]");
    System.out.println(modifiableParameters);
    
    QueryResponse solrResponse = solrServer.query(modifiableParameters);
    SolrDocumentList solrDocList = solrResponse.getResults();
    for (SolrDocument solrDoc : solrDocList) {
      for (Entry<String,Object> kv : solrDoc.entrySet()) {
        System.out.println("The " + kv.getKey() + " is "
            + kv.getValue().toString());
      }
    }
  }
  
  @Test
  public void otherTest() throws ParseException, SolrServerException,
      IOException {
    
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("rows", 10);
    params.set("q", "*:*");
    params.set("group", "true");
    params.set("group.field", "stock_symbol");
    QueryResponse response = solrServer.query(params);
    System.out.println(response);
    
    for (Group group : response.getGroupResponse().getValues().get(0)
        .getValues()) {
      System.out.println("********************** Start of "
          + group.getGroupValue() + " **********************");
      for (SolrDocument solrDoc : group.getResult()) {
        for (Entry<String,Object> kv : solrDoc.entrySet()) {
          System.out.println("The " + kv.getKey() + " is "
              + kv.getValue().toString());
        }
      }
      System.out.println("********************** End of "
          + group.getGroupValue() + " **********************");
    }
    
    ModifiableSolrParams modParams = new ModifiableSolrParams();
    modParams.set("rows", 10);
    modParams.set("q", "*:*");
    modParams.set("group", "true");
    modParams.set("group.field", "stock_symbol");
    modParams.set("sort", "stock_price_low asc");
    modParams.set("fq", "stock_price_high:[14 TO 15]");
    modParams.add("fq", "stock_price_low:[12 TO 13]");
    QueryResponse modParamsResponse = solrServer.query(modParams);
    System.out.println(modParamsResponse);
    
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setRows(10);
    solrQuery.setQuery("*:*");
    solrQuery.set("group", "true");
    solrQuery.set("group.field", "stock_symbol");
    solrQuery.setSortField("stock_price_low", ORDER.asc);
    solrQuery.setFilterQueries("stock_price_high:[14 TO 15]");
    solrQuery.addFilterQuery("stock_price_low:[12 TO 13]");
    QueryResponse solrQueryResponse = solrServer.query(solrQuery);
    System.out.println(solrQueryResponse);
    
    Assert.assertEquals(modParamsResponse.toString().substring(35),
        solrQueryResponse.toString().substring(35));
    
    modParams.remove("group");
    modParams.remove("group.field");
    
    response = solrServer.query(modParams);
    for (SolrDocument solrDoc : response.getResults()) {
      for (Entry<String,Object> kv : solrDoc.entrySet()) {
        System.out.println("The " + kv.getKey() + " is "
            + kv.getValue().toString());
      }
    }
    
    solrQuery.remove("group");
    solrQuery.remove("group.field");
    solrQuery.setFacet(true);
    solrQuery.addFacetField("stock_symbol");
    response = solrServer.query(solrQuery);
    System.out.println(response);
    
  }
  
  @Test
  public void testGroupQuery() throws SolrServerException {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("rows", 10);
    params.set("q", "*:*");
    params.set("group", "true");
    params.set("group.field", "stock_symbol");
    QueryResponse response = solrServer.query(params);
    System.out.println(response);
    
    for (GroupCommand gr : response.getGroupResponse().getValues()) {
      for (Group g : gr.getValues()) {
        System.out.println("********************** Start of "
            + g.getGroupValue() + " **********************");
        for (SolrDocument solrDoc : g.getResult()) {
          for (Entry<String,Object> kv : solrDoc.entrySet()) {
            System.out.println("The " + kv.getKey() + " is "
                + kv.getValue().toString());
          }
        }
        System.out.println("********************** End of " + g.getGroupValue()
            + " **********************");
      }
    }
  }
  
  @Test
  public void testHighlightingQuery() throws SolrServerException {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery("stock_symbol:RAT");
    solrQuery.setHighlight(true);
    solrQuery.set("hl.q", "test");
    QueryResponse response = solrServer.query(solrQuery);
    for (Entry<String,Map<String,List<String>>> highlights : response
        .getHighlighting().entrySet()) {
      for (Entry<String,List<String>> highlightField : highlights.getValue()
          .entrySet()) {
        System.out.println(uniqueField + " " + highlights.getKey()
            + ": In field " + highlightField.getKey()
            + " we found the highlight(s) " + highlightField.getValue());
      }
    }
    
  }
  
  @Test
  public void testFacetQuery() throws SolrServerException {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery("*:*");
    solrQuery.setFacet(true);
    solrQuery.set("facet.field", "stock_symbol");
    QueryResponse response = solrServer.query(solrQuery);
    for (FacetField field : response.getFacetFields()) {
      for (Count count : field.getValues()) {
        System.out.println(count.getName() + ": " + count.getCount());
      }
    }
  }
  
  @Test
  public void testCompareModParamsToSolrQuery() throws SolrServerException {
    ModifiableSolrParams modParams = new ModifiableSolrParams();
    modParams.set("rows", 10);
    modParams.set("q", "*:*");
    modParams.set("group", "true");
    modParams.set("group.field", "stock_symbol");
    modParams.set("sort", "stock_price_low asc");
    modParams.set("fq", "stock_price_high:[14 TO 15]");
    modParams.add("fq", "stock_price_low:[12 TO 13]");
    QueryResponse modParamsResponse = solrServer.query(modParams);
    System.out.println(modParamsResponse);
    
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setRows(10);
    solrQuery.setQuery("*:*");
    solrQuery.set("group", "true");
    solrQuery.set("group.field", "stock_symbol");
    solrQuery.setSortField("stock_price_low", ORDER.asc);
    solrQuery.setFilterQueries("stock_price_high:[14 TO 15]");
    solrQuery.addFilterQuery("stock_price_low:[12 TO 13]");
    QueryResponse solrQueryResponse = solrServer.query(solrQuery);
    System.out.println(solrQueryResponse);
    
    Assert.assertEquals(modParamsResponse.toString().substring(35),
        solrQueryResponse.toString().substring(35));
  }
  
  /**
   * This test covers how to escape characters in a query string.
   * 
   * @throws SolrServerException
   */
  @Test
  public void testEscapeQueryCharacters() throws SolrServerException {
    ModifiableSolrParams params = new ModifiableSolrParams();
    SolrInputDocument solrInDoc = new SolrInputDocument();
    String rowkey = "This + [ is ] my string! with-{special}{characters}";
    solrInDoc.addField("rowkey", rowkey);
    String escapedString = ClientUtils.escapeQueryChars(rowkey);
    System.out.println(escapedString);
    params.add("q", "rowkey:" + escapedString);
    QueryResponse response = solrServer.query(params);
    for (SolrDocument solrDoc : response.getResults()) {
      String dockey = solrDoc.get("rowkey").toString();
      Assert.assertEquals(rowkey, dockey);
    }
  }
  
  private static void indexDocuments(int numberOfSampleDocuments)
      throws ParseException, SolrServerException, IOException {
    for (int i = 0; i < numberOfSampleDocuments + 1; i++) {
      SolrInputDocument solrDoc = new SolrInputDocument();
      String rowkey = rowKeyPrefix + i;
      solrDoc.addField(uniqueField, rowkey);
      solrDoc.addField("exchange", "NYSE");
      if (i % 2 == 0) {
        solrDoc.addField("stock_symbol", "RAT");
      } else {
        solrDoc.addField("stock_symbol", "ABC");
      }
      solrDoc.addField("date", DateUtil.parseDate("2012-10-18"));
      solrDoc.addField("stock_price_open",
          Double.toString(Math.random() * 100 * i));
      solrDoc.addField("stock_price_high",
          Double.toString(Math.random() * 100 * i));
      solrDoc.addField("stock_price_low",
          Double.toString(Math.random() * 100 * i));
      solrDoc.addField("stock_price_close",
          Double.toString(Math.random() * 100 * i));
      solrDoc.addField("stock_price_adj_close",
          Double.toString(Math.random() * 100 * i));
      solrDoc.addField("dividends", Double.toString(Math.random() * 10000 * i));
      solrServer.add(solrDoc);
    }
    solrServer.commit();
  }
}
