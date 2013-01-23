package lucenesolrdefguide;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

public class Test {
  
  private static final int companyLimit = 3;
  private static final int days = 90;
  
  /**
   * @param args
   * @throws SolrServerException
   */
  public static void main(String[] args) throws SolrServerException {
     RealTimeStocks rts = new RealTimeStocks();
     String graphData = rts.createGraphData(rts.createSolrQuery("*"));
     System.out.println(graphData);
    
//    System.out.println(createGraphData(createSolrQuery("*")));
//    String[] companies = new String[3];
    
  }
  
  public static String createGraphData(QueryResponse stockData, String companies[]) {
    int dayCount = 0;
    for (GroupCommand groups : stockData.getGroupResponse().getValues()) {
      for (int groupIndex = 0; groupIndex < groups.getValues().size(); groupIndex++) {
        dayCount++;
      }
    }
    // Add additional column and row for titles
    
    String[][] graphData = new String[(dayCount + 1)][((companyLimit + 1))];
    System.out.println(((dayCount) + 1) + " by " + (companyLimit + 1));
    graphData[0][0] = "\'Date\'";
    for (GroupCommand groups : stockData.getGroupResponse().getValues()) {
      for (int groupIndex = 0; groupIndex < groups.getValues().size(); groupIndex++) {
        // Putting the dates for each row
        graphData[groupIndex + 1][0] = "\'"
            + groups.getValues().get(groupIndex).getGroupValue() + "\'";
        for (int stockRecordIndex = 0; stockRecordIndex < groups.getValues()
            .get(groupIndex).getResult().size(); stockRecordIndex++) {
          // Adding rows
          SolrDocument stockRecord = groups.getValues().get(groupIndex)
              .getResult().get(stockRecordIndex);
          
          graphData[groupIndex + 1][stockRecordIndex + 1] = "\'"
              + stockRecord.getFieldValue("date").toString() + "\'";
          graphData[stockRecordIndex + 1][groupIndex + 1] = stockRecord
              .getFieldValue("stock_price_close").toString();
        }
      }
    }
    return Arrays.deepToString(graphData);
  }
  
  public static QueryResponse createSolrQuery(String stockSymbol)
      throws SolrServerException {
    HttpSolrServer solrServer = new HttpSolrServer(
        "http://localhost:8983/solr/stocks/");
    SolrQuery solrQuery = new SolrQuery();
    // We do not want any dividends data included in our results
    solrQuery.add("q", "stock_symbol:" + stockSymbol
        + " AND !dividends:[* TO *]");
    solrQuery.add("rows", String.valueOf(days));
    solrQuery.add("sort", "date asc");
    solrQuery.add("group", "true");
    solrQuery.add("group.field", "date");
    solrQuery.add("group.limit", String.valueOf(companyLimit));
    System.out.println("The query is " + solrQuery.toString());
    QueryResponse solrResponse = solrServer.query(solrQuery);
    return solrResponse;
  }
}
