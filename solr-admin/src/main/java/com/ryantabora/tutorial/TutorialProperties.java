package com.ryantabora.tutorial;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TutorialProperties {
  
  private static Properties props;
  private final String SOLR_ADMIN_URL;
  private final String SOLR_STOCKS_CORE;
  private final String SOLR_WIKI_CORE;
  private final String SIMPLE_CORE_NAME;
  private final String SOLR_STOCKS_URL;
  private final String SOLR_WIKIPEDIA_URL;
  private final String SOLR_SIMPLE_URL;
  private final String SOLR_WIKIPEDIA_SERVERS[];
  private final String SOLR_STOCKS_SERVERS[];
  private final String SOLR_UNIQUE_KEY;
  
  public TutorialProperties(String propLocation) {
    props = new Properties();
    try {
      File file = new File(propLocation);
      System.out.println("Loading properties from " + file.getAbsolutePath());
      FileReader is = new FileReader(file);
      props.load(is);
      System.out.println("Properties: " + props.toString());
      
      SOLR_ADMIN_URL = props.getProperty("SOLR_ADMIN_URL");
      SOLR_STOCKS_CORE = props.getProperty("SOLR_STOCKS_CORE");
      SOLR_WIKI_CORE = props.getProperty("SOLR_WIKI_CORE");
      SIMPLE_CORE_NAME = props.getProperty("SIMPLE_CORE_NAME");
      SOLR_STOCKS_URL = props.getProperty("SOLR_STOCKS_URL");
      SOLR_WIKIPEDIA_URL = props.getProperty("SOLR_WIKIPEDIA_URL");
      SOLR_SIMPLE_URL = props.getProperty("SOLR_SIMPLE_URL");
      SOLR_WIKIPEDIA_SERVERS = props.getProperty("SOLR_WIKIPEDIA_SERVERS")
          .split(",");
      SOLR_STOCKS_SERVERS = props.getProperty("SOLR_WIKIPEDIA_SERVERS").split(
          ",");
      SOLR_UNIQUE_KEY = props.getProperty("SOLR_UNIQUE_KEY");
      
    } catch (IOException e) {
      throw new RuntimeException("Could not load properties file");
    } catch (NullPointerException e) {
      throw new RuntimeException("File does not exist");
    }
  }
}