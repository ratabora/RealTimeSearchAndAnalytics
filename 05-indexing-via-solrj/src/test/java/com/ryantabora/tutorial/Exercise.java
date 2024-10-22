package com.ryantabora.tutorial;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

public class Exercise {
  private static final String solrServerURL = "http://localhost:8080/solr/mycore";
  private final String[] schema = {"key", "first_name", "last_name",
      "birth_date", "age", "interests"};
  private final String[][] sampleRecords = {
      {"1", "Ryan", "Tabora", "02/01/1920", "25",
          "zombies, comic books, dogs, video games, beer"},
      {"2", "Rick", "Grimes", "07/12/1983", "30",
          "my family, police work, rifles, government leaders"},
      {"3", "Carl", "Grimes", "12/10/2004", "9",
          "atomic dog, comic books,family"},
      {"4", "Dr. Denise", "Cloyd", "5/24/1980", "33",
          "medicine, reading, philosophy"}};
  
  @Test
  public void addRecords() throws SolrServerException, IOException,
      ParseException {
    // Create the Solr Server client
    
    // Create a list of SolrInputDocuments
    
    // Iterate through the sample records
    for (String[] record : sampleRecords) {
      
      // Create a new SolrInputDocument for this record
      
      // Iterate through this sample record
      for (int i = 0; i < record.length; i++) {
        
      }
      // Add the SolrInputDocument for this record to the list of SolrDocuments
    }
    
    // Add and commit the SolrInputDocuments to the Solr Server
    
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
    String inputFormat = "MM/dd/yyyy";
    return outputFormat.format(new SimpleDateFormat(inputFormat)
        .parse(dateString));
  }
}
