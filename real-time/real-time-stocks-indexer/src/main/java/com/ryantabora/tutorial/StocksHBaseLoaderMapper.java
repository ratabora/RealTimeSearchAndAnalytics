package com.ryantabora.tutorial;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class StocksHBaseLoaderMapper extends
    Mapper<Object,Text,Text,IntWritable> {
  
  /**
   * This is the Map function for our Map Reduce job.
   * 
   * The value is a single line of the CSV file. We take the line and split it
   * into multiple key/value pairs and insert each record into HBase.
   * 
   * We have already learned all of the HBase specific APIs in previous examples
   * (HTable and Put), so this example is a good example of a powerful
   * application made with two very simple APIs.
   */
  public void map(Object key, Text value, Context context) throws IOException,
      InterruptedException {
    // Initialize the HTable so we can close it in the finally clause
    HTable hbaseTable = null;
    try {
      String[] record = value.toString().split(",");
      String[] schema = null;
      // The record is either a dividends record or daily stocks record. We can
      // tell which one it is by the length of the record.
      if (record.length == TutorialConstants.STOCKS_DAILY_SCHEMA.length) {
        schema = TutorialConstants.STOCKS_DAILY_SCHEMA;
      } else if (record.length == TutorialConstants.STOCKS_DIVIDENDS_SCHEMA.length) {
        schema = TutorialConstants.STOCKS_DIVIDENDS_SCHEMA;
      }
      // The rowkey is defined as STOCK_SYMBOL|DATE
      String rowkey = record[1] + TutorialConstants.KEY_DELIMITER + record[2];
      // We want to make sure the record is an actual record, not just a listing
      // of the qualifiers (the first line of the CSV file)
      if (!rowkey.equals(schema[1] + TutorialConstants.KEY_DELIMITER
          + schema[2])) {
        // We create the HTable by the configuration created in the main driver
        // class.
        hbaseTable = new HTable(context.getConfiguration(),
            Bytes.toBytes(TutorialConstants.HBASE_STOCKS_TABLE));
        // As long as we understand the format of the CSV, lets add it to HBase.
        if (schema != null) {
          // We create our put object for the rowkey
          Put put = new Put(Bytes.toBytes(rowkey));
          for (int i = 0; i < record.length && i < schema.length; i++) {
            // Adding the value to the put object
            put.add(Bytes.toBytes(TutorialConstants.HBASE_RECORDS_FAMILY),
                Bytes.toBytes(schema[i]), Bytes.toBytes(record[i]));
          }
          // Here the client actually submits the put to HBase
          hbaseTable.put(put);
        }
      }
    } finally {
      // Always close the connection to the HTable
      close(hbaseTable);
    }
  }
  
  /**
   * Convenience method to close connections
   * 
   * @param c
   */
  private static void close(Closeable c) {
    if (c == null) return;
    try {
      c.close();
    } catch (IOException e) {
      // Nothing we can do
    }
  }
}