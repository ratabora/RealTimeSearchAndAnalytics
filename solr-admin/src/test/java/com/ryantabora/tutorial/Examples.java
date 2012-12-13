package com.ryantabora.tutorial;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;

public class Examples {
  
  /**
   * Good if the user needs to make backwards compatible changes such as adding
   * fields to the schema. Essentially restarts the core without restarting the
   * entire servlet.
   */
  public void reloadCore() {
    
  }
  
  /**
   * The user needs to have the solrconfig and schema files in the classpath or
   * already created at the location of the solr installation.
   * 
   * @throws IOException
   * @throws SolrServerException
   */
  public void createNewCore() throws SolrServerException, IOException {
    
  }
  
  public void disableCore() throws SolrServerException, IOException {
    
  }
  
}
