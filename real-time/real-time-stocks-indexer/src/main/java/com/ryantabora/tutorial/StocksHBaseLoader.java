package com.ryantabora.tutorial;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class StocksHBaseLoader {
  
  private static Configuration conf = new Configuration();
  
  /**
   * How to use the StocksHBaseLoader.
   */
  private static void usage() {
    System.out
        .println("usage: <tutorial.properties path> <input data path> <output path>");
    System.exit(1);
  }
  
  /**
   * This application takes in daily stocks data and dividends stocks data and
   * loads each record into HBase. It uses the column name for each value as the
   * qualifier in HBase. The rowkey is defined as STOCK_SYMBOL|DATE.
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
    
    // GenericOptionsParser is a utility to parse command line arguments generic
    // to the Hadoop framework. GenericOptionsParser recognizes several
    // standarad command line arguments, enabling applications to easily specify
    // a namenode, a jobtracker, additional configuration resources etc.
    String[] otherArgs = new GenericOptionsParser(conf, args)
        .getRemainingArgs();
    
    if (otherArgs.length != 3) usage();
    setConfiguration(otherArgs[0]);
    
    // If the HBase table does not exist, then create it
    if (!hbaseTableExists(TutorialConstants.HBASE_STOCKS_TABLE)) {
      createTable(TutorialConstants.HBASE_STOCKS_TABLE,
          TutorialConstants.HBASE_STOCKS_FAMILIES);
      System.out.println("Created new " + TutorialConstants.HBASE_STOCKS_TABLE);
    }
    Job job = new Job(conf, "Stocks HBase Loader");
    job.setJarByClass(StocksHBaseLoader.class);
    job.setMapperClass(StocksHBaseLoaderMapper.class);
    FileInputFormat.addInputPath(job, new Path(otherArgs[1]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
  
  /**
   * Creates the HBase Configuration from the properties file.
   * 
   * @param propertiesPath
   */
  private static void setConfiguration(String propertiesPath) {
    TutorialProperties tutorialProperties = new TutorialProperties(
        propertiesPath);
    conf = HBaseConfiguration.create();
    conf.set("hbase.zookeeper.quorum", tutorialProperties.getZKQuorum());
    conf.set("hbase.zookeeper.property.clientPort",
        tutorialProperties.getZKPort());
    conf.set("hbase.master", tutorialProperties.getHBMaster());
    conf.set("hbase.rootdir", tutorialProperties.getHBrootDir());
  }
  
  /**
   * This method creates the specified table with the specified column families.
   * 
   * @param tableName
   * @param families
   */
  private static void createTable(String tableName, String[] families) {
    // We initialize the HBaseAdmin this way so we can close the connection in
    // the finally block
    HBaseAdmin admin = null;
    try {
      // We create the HBaseAdmin with the configuration
      admin = new HBaseAdmin(conf);
      // We dont want to create the table if it already exists
      if (!admin.tableExists(tableName)) {
        // We create a single HTableDescriptor to tell the HBaseAdmin what we
        // want created
        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        for (String family : families) {
          // We add HColumnDescriptors for each family and add it to the
          // HTableDescriptor. I know the naming convention here is funny,
          // (column families, columns, qualifiers, etc) but this is the correct
          // object for creating column families.
          tableDesc.addFamily(new HColumnDescriptor(family));
        }
        // This method actually requests the HBaseAdmin to create the table.
        admin.createTable(tableDesc);
      } else {
        System.out.println("Table " + tableName
            + " already exists. Delete it first.");
        System.exit(0);
      }
    } catch (MasterNotRunningException e) {
      throw new RuntimeException("Unable to create the table " + tableName
          + ". The actual exception is: " + e.getMessage(), e);
    } catch (ZooKeeperConnectionException e) {
      throw new RuntimeException("Unable to create the table " + tableName
          + ". The actual exception is: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create the table " + tableName
          + ". The actual exception is: " + e.getMessage(), e);
    } finally {
      // Make sure to close the connection to the HBaseAdmin.
      close(admin);
    }
  }
  
  /**
   * This method is a wrapper around the HBaseAdmin.tableExists method
   * 
   * @param tableName
   * @return
   */
  private static boolean hbaseTableExists(String tableName) {
    // We initialize the HBaseAdmin this way so we can close the connection in
    // the finally block
    HBaseAdmin admin = null;
    try {
      // We create the HBaseAdmin with the configuration
      admin = new HBaseAdmin(conf);
      // We simply call the HBaseAdmin.tableExists method
      return admin.tableExists(tableName);
    } catch (MasterNotRunningException e) {
      throw new RuntimeException(
          "Unable to check if the following table exists: " + tableName
              + ". The actual exception is: " + e.getMessage(), e);
    } catch (ZooKeeperConnectionException e) {
      throw new RuntimeException(
          "Unable to check if the following table exists: " + tableName
              + ". The actual exception is: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new RuntimeException(
          "Unable to check if the following table exists: " + tableName
              + ". The actual exception is: " + e.getMessage(), e);
    } finally {
      // Make sure to close the connection to the HBaseAdmin.
      close(admin);
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