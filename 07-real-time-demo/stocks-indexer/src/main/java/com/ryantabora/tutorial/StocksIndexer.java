package com.ryantabora.tutorial;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class StocksIndexer {
  private static void usage() {
    System.err.println("usage: <property_location> <stock_symbol>");
    System.exit(1);
  }
  
  private static Configuration conf;
  
  /**
   * This application takes in daily stocks data and dividends stocks data that
   * is already loaded into HBase and indexes that data in Solr. It uses the
   * HBase table as the input format and scans over all of the records defined
   * in the Scan object we create in this driver class.
   * 
   * The main method is called the Driver of the Map Reduce program. It
   * essentially creates the configuration of the Map Reduce job, specifying the
   * inputs, outputs, Map class, Reduce class, and others.
   * 
   * @param args
   * @throws IOException
   * @throws InterruptedException
   * @throws ClassNotFoundException
   * @throws URISyntaxException
   */
  public static void main(String[] args) throws IOException,
      InterruptedException, ClassNotFoundException, URISyntaxException {
    
    if (args.length == 0 || args.length > 2) {
      usage();
    }
    // Setting up the Hadoop configs
    createHBaseConfiguration(args[0]);
    
    Job job = new Job(conf, "Stocks Indexer");
    job.setJarByClass(StocksIndexer.class);
    
    // We configure the scan
    Scan scan = new Scan();
    if (args.length == 2) {
      scan.setStartRow(Bytes.toBytes(args[1]));
      scan.setStopRow(Bytes.toBytes(args[1] + "|Z"));
    }
    
    scan.addFamily(Bytes.toBytes(TutorialConstants.HBASE_RECORDS_FAMILY));
    scan.setCaching(500); // Set the number of rows for caching that will be
                          // passed to scanners. If not set, the default setting
                          // from HTable.getScannerCaching() will apply. Higher
                          // caching values will enable faster scanners but will
                          // use more memory.
    scan.setCacheBlocks(false); // Set whether blocks should be cached for this
                                // Scan.
    
    // This is how we create a map job over an HBase table
    TableMapReduceUtil.initTableMapperJob(TutorialConstants.HBASE_STOCKS_TABLE,
        scan, StocksIndexerMapper.class, null, null, job);
    
    // We're not outputting anything from the job, we are submitting the puts
    // within the Mapper
    job.setOutputFormatClass(NullOutputFormat.class);
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
  
  /**
   * Creates the HBase Configuration from the properties file
   * 
   * @param propsLocation
   * @return
   */
  private static void createHBaseConfiguration(String propsLocation) {
    TutorialProperties tutorialProperties = new TutorialProperties(
        propsLocation);
    conf = HBaseConfiguration.create();
    conf.set("hbase.zookeeper.quorum", tutorialProperties.getZKQuorum());
    conf.set("hbase.zookeeper.property.clientPort",
        tutorialProperties.getZKPort());
    conf.set("hbase.master", tutorialProperties.getHBMaster());
    conf.set("hbase.rootdir", tutorialProperties.getHBrootDir());
    conf.set("solr.server", tutorialProperties.getSolrServer());
  }
}
