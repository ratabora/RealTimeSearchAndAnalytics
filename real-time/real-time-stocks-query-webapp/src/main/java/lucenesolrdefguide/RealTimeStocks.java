package lucenesolrdefguide;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

public class RealTimeStocks extends HttpServlet {
  
  private static final long serialVersionUID = -3170942194370528939L;
  
  private static final String stockSymbolArg = "stock_symbol";
  private static final int groupLimit = 200;
  private static final int rows = 10;
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    response.setContentType("text/html");
    try {
      request.getRequestDispatcher("/RealTimeStocks.jsp").include(request,
          response);
    } catch (ServletException e) {
      e.printStackTrace();
    }
    
    String stockSymbol = "*";
    String args[] = request.getRequestURI().split("/");
    for (int i = 1; i < args.length; i = i + 2) {
      if (args[i].startsWith("--")) {
        String arg = args[i].substring(2);
        String value = args[i + 1];
        try {
          if (arg.equalsIgnoreCase(stockSymbolArg)) stockSymbol = value;
        } catch (Throwable t) {
          
        }
      }
    }
    
    PrintWriter out = response.getWriter();
    try {
      
      out.println("<html><head><script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script><script type=\"text/javascript\"> google.load(\"visualization\", \"1\", {packages : [ \"corechart\" ]});google.setOnLoadCallback(drawChart);function drawChart() {var data = google.visualization.arrayToDataTable(");
      out.println(createGraphData(createSolrQuery(stockSymbol)));
      out.println(");var options = {title : \'Company Performance\'};var chart = new google.visualization.LineChart(document.getElementById(\'chart_div\'));chart.draw(data, options);}</script></head><body><div id=\"chart_div\" style=\"width: 900px; height: 500px;\"></div></body></html>");
    } catch (SolrServerException e) {
      out.println("<p> Failed to make the Solr query: + " + e.getMessage()
          + "</p>");
    }
    out.close();
  }
  
  public String createGraphData(QueryResponse stockData) {
    int groupCount = 0;
    for (GroupCommand groups : stockData.getGroupResponse().getValues()) {
      for (int groupIndex = 0; groupIndex < groups.getValues().size(); groupIndex++) {
        groupCount++;
      }
    }
    // Add additional column and row for titles
    String[][] graphData = new String[((groupLimit * groupCount + 1))][(groupCount + 1)];
    System.out.println(((groupLimit * groupCount) + 1) + " by "
        + (groupCount + 1));
    graphData[0][0] = "\'Date\'";
    int totalRecordIndex = 0;
    for (GroupCommand groups : stockData.getGroupResponse().getValues()) {
      // Iterating through each company
      for (int groupIndex = 0; groupIndex < groups.getValues().size(); groupIndex++) {
        // Putting the name of the companies as the column header
        graphData[0][groupIndex + 1] = "\'"
            + groups.getValues().get(groupIndex).getGroupValue() + "\'";
        // Iterating through each record for that company starting at the
        // earliest record in the SolrResponse
        int numberOfRecords = groups.getValues().get(groupIndex).getResult()
            .size();
        for (int stockRecordIndex = numberOfRecords - 1; stockRecordIndex > 0; stockRecordIndex--) {
          // Putting the value of the stock and the date as rows
          SolrDocument stockRecord = groups.getValues().get(groupIndex)
              .getResult().get(stockRecordIndex);
          graphData[totalRecordIndex + 1][0] = "\'"
              + stockRecord.getFieldValue("date").toString() + "\'";
          graphData[totalRecordIndex + 1][groupIndex + 1] = stockRecord
              .getFieldValue("stock_price_close").toString();
          totalRecordIndex++;
        }
      }
    }
    System.out.println("There were " + totalRecordIndex + " records");
    return Arrays.deepToString(graphData);
  }
  
  public QueryResponse createSolrQuery(String stockSymbol)
      throws SolrServerException {
    HttpSolrServer solrServer = new HttpSolrServer(
        "http://localhost:8983/solr/stocks/");
    SolrQuery solrQuery = new SolrQuery();
    // We do not want any dividends data included in our results
    solrQuery.add("q", "stock_symbol:" + stockSymbol
        + " AND !dividends:[* TO *]");
    solrQuery.add("rows", String.valueOf(rows));
    solrQuery.add("sort", "date desc");
    solrQuery.add("group", "true");
    solrQuery.add("group.field", "stock_symbol");
    solrQuery.add("group.limit", String.valueOf(groupLimit));
    System.out.println("The query is " + solrQuery.toString());
    QueryResponse solrResponse = solrServer.query(solrQuery);
    return solrResponse;
  }
}
