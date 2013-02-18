package com.ryantabora.tutorial;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.Text;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class StocksIndexerMapper extends TableMapper<Text,Text> {
  
  /**
   * This is the Map function for our Map Reduce job.
   * 
   * Each record is indexed in Solr as the MapReduce job scans the HBase table.
   * First, we create a connection to the Solr Server and then we create a
   * SolrDocument that is used to index documents in Solr. We iterate through
   * the result, add each field to the SolrDocument, and then submit the
   * SolrDocument to Solr.
   */
  public void map(ImmutableBytesWritable key, Result hbaseResult,
      Context context) throws InterruptedException, IOException {
    
    Configuration conf = context.getConfiguration();
    
    // Creating the Solr client
    HttpSolrServer solrServer = new HttpSolrServer(conf.get("solr.server"));
    SolrInputDocument solrDoc = new SolrInputDocument();
    try {
      // Create the Solr document
      solrDoc.addField("rowkey", new String(hbaseResult.getRow()));
      for (KeyValue rowQualifierAndValue : hbaseResult.list()) {
        if (!(new String(rowQualifierAndValue.getQualifier())
            .contains("history"))) {
          // No support for AVRO.
          String fieldName = new String(rowQualifierAndValue.getQualifier());
          String fieldValue = new String(rowQualifierAndValue.getValue());
          if (fieldName.contains("date")) {
            fieldValue = formatSolrDate(fieldValue);
          }
          solrDoc.addField(fieldName, fieldValue);
        }
        
      }
      solrServer.add(solrDoc);
      solrServer.commit(true, true, true);
    } catch (SolrServerException e) {
      System.err.println("Failed to update Solr with document "
          + new String(hbaseResult.getRow()));
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
  public static String formatSolrDate(String dateString) {
    SimpleDateFormat outputFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss'Z'");
    String inputFormat = "yyyy-MM-dd";
    try {
      return outputFormat.format(new SimpleDateFormat(inputFormat)
          .parse(dateString));
    } catch (ParseException e) {
      throw new RuntimeException("Input date must be in format " + inputFormat,
          e);
    }
  }
}
