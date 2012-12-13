package com.ryantabora.tutorial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class StocksIndexer {
  
  private static final String DATA_DIR_ARG = "data";
  private static String dataDir = null;
  private static final String SOLR_SERVER_ARG = "solr";
  private static String solrServerName = null;
  private static final String KEY_FIELD = "rowkey";
  private static int count = 0;
  
  /**
   * Tells the user how to run the jar.
   */
  private static void usage() {
    System.out.println("usage: --" + DATA_DIR_ARG
        + " {input data directory path} --" + SOLR_SERVER_ARG
        + " {solr server url}");
    System.exit(1);
  }
  
  /**
   * Handles user arguments.
   * 
   * @param args
   */
  private static void userArgs(String[] args) {
    if (args.length == 0) usage();
    System.out.println("args: " + Arrays.asList(args));
    for (int i = 0; i < args.length; i = i + 2) {
      if (args[i].startsWith("--")) {
        String arg = args[i].substring(2);
        String value = args[i + 1];
        try {
          if (arg.equalsIgnoreCase(DATA_DIR_ARG)) dataDir = value;
          if (arg.equalsIgnoreCase(SOLR_SERVER_ARG)) solrServerName = value;
        } catch (Throwable t) {
          usage();
        }
      }
    }
    if (dataDir == null || solrServerName == null) usage();
  }
  
  /**
   * Iterates through a list of files and indexes them in Solr.
   * 
   * @param args
   * @throws SolrServerException
   * @throws IOException
   * @throws ParseException
   */
  public static void main(String[] args) throws SolrServerException,
      IOException, ParseException {
    userArgs(args);
    File folder = new File(dataDir);
    for (final File fileEntry : folder.listFiles()) {
      if (!fileEntry.isDirectory()) {
        FileInputStream is = new FileInputStream(fileEntry);
        writeIndex(is);
      } else {
        // Only support one level of dirs
      }
    }
    System.out.println("Indexed " + count + " documents");
  }
  
  /**
   * Writes an index to Solr from a known CSV input file.
   * 
   * @param inputStream
   * @throws SolrServerException
   * @throws IOException
   * @throws ParseException
   */
  public static void writeIndex(InputStream inputStream)
      throws SolrServerException, IOException, ParseException {
    
    // We assume we know the schema
    String dailyStocksSchema[] = {"exchange", "stock_symbol", "date",
        "stock_price_open", "stock_price_high", "stock_price_low",
        "stock_price_close", "stock_volume", "stock_price_adj_close"};
    String dividendsSchema[] = {"exchange", "stock_symbol", "date", "dividends"};
    String schema[] = null;
    
    // Setting up the input reader
    BufferedReader reader = null;
    String currentLine = null;
    
    // Creating the client connection to Solr
    HttpSolrServer solrServer = new HttpSolrServer(solrServerName);
    
    try {
      // We iterate through the CSV file line by line
      reader = new BufferedReader(new InputStreamReader(inputStream));
      while ((currentLine = reader.readLine()) != null) {
        String row[] = currentLine.split(",");
        
        // Make sure there is something in the row and it is in the expected
        // format
        if (row.length > 0) {
          if (row.length == dividendsSchema.length) schema = dividendsSchema;
          else if (row.length == dailyStocksSchema.length) schema = dailyStocksSchema;
          if (schema == null) {
            throw new RuntimeException("Data was not in an expected format");
          }
          
          // The rowKey is going to be defined as STOCK_SYMBOL|DATE
          String rowKey = row[1] + "|" + row[2];
          // Making sure that this is a data record (not the schema record i.e.
          // first line of the file)
          if (!rowKey.equals(schema[1] + "|" + schema[2])) {
            
            // Create a Solr Document object for Solr
            SolrInputDocument solrDoc = new SolrInputDocument();
            
            // Add each field/attribute to the SolrDocument
            for (int i = 0; i < row.length && i < schema.length; i++) {
              // Solr requires a special Date format. Here we format the field
              // for Solr if it is a date. SolrUtilities is a custom class I
              // created.
              if (schema[i].contains("date")) {
                row[i] = formatSolrDate(row[i]);
              }
              
              // Add the field to the Solr Document
              solrDoc.addField(schema[i], row[i]);
              
            }
            
            // Add the Solr document to the Solr Server
            if (solrServer != null && solrDoc != null) {
              solrDoc.addField(KEY_FIELD, rowKey);
              solrServer.add(solrDoc);
              count++;
              System.out.println("Successfully put " + KEY_FIELD + " : "
                  + rowKey + " to Solr");
            }
          }
        }
      }
      
      // Always make sure to commit!
      solrServer.commit();
    } finally {
      reader.close();
    }
  }
  
  /**
   * Solr expects a certain format. This method takes a date in format
   * yyyy-MM-dd and returns the correct Solr format
   * 
   * @param dateString
   * @return
   * @throws ParseException
   */
  public static String formatSolrDate(String dateString) throws ParseException {
    SimpleDateFormat outputFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss'Z'");
    String inputFormat = "yyyy-MM-dd";
    return outputFormat.format(new SimpleDateFormat(inputFormat)
        .parse(dateString));
  }
  
}