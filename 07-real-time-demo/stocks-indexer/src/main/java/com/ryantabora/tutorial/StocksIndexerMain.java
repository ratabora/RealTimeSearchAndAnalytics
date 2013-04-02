package com.ryantabora.tutorial;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 * Read file from filesystem and insert into Solr
 */
public class StocksIndexerMain {
  public static String solrserverurl = "http://127.0.0.1:8983/solr/demo.stocks";  
  public static String datafile = "data/NYSE_daily_prices_Q.csv";
  
  
  public static void main(String[] args) throws Exception {
    HttpSolrServer client = new HttpSolrServer(solrserverurl);
    deleteAll(client);
    
    Thread.sleep(1000);
    
    addDocs(client, "QTM");
    
  }
  
  public static void deleteAll(HttpSolrServer client) throws Exception {
    client.deleteByQuery("*:*");
  }
  
  public static void addDocs(HttpSolrServer client, String symbol) throws Exception {
    List<String> lines = IOUtils.readLines(new FileReader(datafile));
    lines.remove(0);
    
    List<String> stockLines = new ArrayList<String>();
    for (String l : lines) {
      String[] r = l.split(",");
      String s = r[1];
      if (s.equalsIgnoreCase(symbol)) {
        stockLines.add(l);
      }
    }
    
    Collections.sort(stockLines, new LineComparator());
    
    for (String l : stockLines) {
      add(l, client);
      Thread.sleep(60);
    }
    client.commit();
  }
  
  public static class LineComparator implements Comparator<String> {
    public int compare(String s1, String s2) {
      try {
        Date d1 = getDate(s1);
        Date d2 = getDate(s2);
        
        return d1.compareTo(d2);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    
    public Date getDate(String line) throws Exception {
      String[] r = line.split(",");
      String ds = r[2];
      return StocksIndexerMain.parseDate(ds);
    }
  }
  
  public static void add(String value, HttpSolrServer client) throws Exception {
    SolrInputDocument solrDoc = new SolrInputDocument();
    
    String[] record = value.split(",");
    String[] schema = null;
    // The record is either a dividends record or daily stocks record. We can
    // tell which one it is by the length of the record.
    if (record.length == TutorialConstants.STOCKS_DAILY_SCHEMA.length) {
      schema = TutorialConstants.STOCKS_DAILY_SCHEMA;
    } else if (record.length == TutorialConstants.STOCKS_DIVIDENDS_SCHEMA.length) {
      schema = TutorialConstants.STOCKS_DIVIDENDS_SCHEMA;
    }
    String rowkey = record[1] + TutorialConstants.KEY_DELIMITER + record[2];
    if (schema != null) {
      solrDoc.addField("id", rowkey);
      for (int i = 0; i < record.length && i < schema.length; i++) {
        String val = record[i];
        if (schema[i].contains("date")) {
          Date d = parseDate(val);
          solrDoc.addField(schema[i], d);
        } else {
          solrDoc.addField(schema[i], val);
        }
      }
      
    }
    client.add(solrDoc);
    client.commit(false, true, true);
    
  }
  
  public static Date parseDate(String dateString) throws Exception {
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    return inputFormat.parse(dateString);
  }
}
