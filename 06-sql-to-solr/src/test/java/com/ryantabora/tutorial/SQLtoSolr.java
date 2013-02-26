package com.ryantabora.tutorial;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.internal.csv.CSVParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLtoSolr {
  
  /**
   * Some sample stock data used in the Solr instance
   */
  private static String sampleData = "exchange,stock_symbol,date,stock_price_open,stock_price_high,stock_price_low,stock_price_close,stock_volume,stock_price_adj_close\n"
      + "NYSE,QTM,2010-02-08,2.37,2.42,2.29,2.36,3013600,2.36\n"
      + "NYSE,QTM,2010-02-05,2.38,2.50,2.34,2.41,2687600,2.41\n"
      + "NYSE,QTM,2010-02-04,2.57,2.64,2.39,2.46,4529800,2.46\n"
      + "NYSE,QTM,2010-02-03,2.64,2.67,2.55,2.63,2688600,2.63\n"
      + "NYSE,QTM,2010-02-02,2.69,2.76,2.56,2.66,2959700,2.66\n"
      + "NYSE,QTM,2010-02-01,null,2.80,2.52,2.67,5050100,2.67\n"
      + "NYSE,QRR,2007-03-08,13.75,13.85,13.70,13.77,34900,13.77\n"
      + "NYSE,QRR,2007-03-07,13.75,13.85,13.60,13.76,53200,13.76\n"
      + "NYSE,QRR,2007-03-06,13.77,13.98,13.54,13.77,58600,13.77\n"
      + "NYSE,QRR,2007-03-05,13.75,13.97,13.39,13.75,171900,13.75\n"
      + "NYSE,QRR,2007-03-02,13.75,14.00,13.75,13.90,66900,13.90\n"
      + "NYSE,QRR,2007-03-01,13.70,13.85,13.15,13.75,232000,13.75\n"
      + "NYSE,QRR,2007-02-28,14.08,14.12,13.95,13.88,173000,13.88\n"
      + "NYSE,QRR,2007-02-27,14.50,14.50,13.86,14.10,200800,14.10\n"
      + "NYSE,QRR,2007-02-26,14.78,14.95,14.50,14.75,95000,14.75";
  private static SimpleDateFormat dateFormat = new SimpleDateFormat(
      "yyyy-MM-dd");
  private static Connection con;
  private static JettySolrRunner jetty;
  private static HttpSolrServer solrClient;
  private static String solrUrl = "http://127.0.0.1:8983/solr";
  
  /**
   * Startup the embedded SQL and Solr services
   * 
   * @throws Exception
   */
  @BeforeClass
  public static void setup() throws Exception {
    initSolr();
    initSQL();
  }
  
  /**
   * Stop the embedded SQL and Solr services
   * 
   * @throws Exception
   */
  @AfterClass
  public static void cleanup() throws Exception {
    jetty.stop();
  }
  
  private static void initSQL() throws Exception {
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    String ct = "CREATE TABLE stocks (id BIGINT PRIMARY KEY, exchange varchar(255),"
        + "stock_symbol varchar(255),"
        + "ddate DATE,"
        + "stock_volume BIGINT,"
        + "stock_price_open DOUBLE)";
    
    String jdbcurl = "jdbc:derby:memory:solr;create=true";
    con = DriverManager.getConnection(jdbcurl);
    Statement stmt = con.createStatement();
    stmt.execute(ct);
    con.commit();
    
    CSVParser csvParser = new CSVParser(new StringReader(sampleData));
    
    String[][] csvrows = csvParser.getAllValues();
    
    for (int x = 1; x < csvrows.length; x++) {
      String[] csvrow = csvrows[x];
      String up = "INSERT INTO stocks (id,exchange,stock_symbol,ddate,stock_volume,stock_price_open)"
          + " VALUES (?,?,?,?,?,?)";
      PreparedStatement sqlStatement = con.prepareStatement(up);
      
      sqlStatement.setLong(1, x);
      sqlStatement.setString(2, csvrow[0]);
      sqlStatement.setString(3, csvrow[1]);
      java.util.Date date = dateFormat.parse(csvrow[2]);
      sqlStatement.setDate(4, new java.sql.Date(date.getTime()));
      sqlStatement.setLong(5, Long.parseLong(csvrow[7]));
      if (csvrow[3].equals("null")) {
        sqlStatement.setNull(6, Types.DOUBLE);
      } else {
        sqlStatement.setDouble(6, Double.parseDouble(csvrow[3]));
      }
      
      sqlStatement.executeUpdate();
    }
    con.commit();
    
  }
  
  private static void initSolr() throws Exception {
    String solrHome = "stocks";
    
    jetty = new JettySolrRunner(solrHome, "/solr", 8983);
    jetty.start();
    
    solrClient = new HttpSolrServer(solrUrl);
    solrClient.deleteByQuery("*:*");
    solrClient.commit();
    CSVParser csvParser = new CSVParser(new StringReader(sampleData));
    
    String[][] csvrows = csvParser.getAllValues();
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    for (int x = 1; x < csvrows.length; x++) {
      String[] csvrow = csvrows[x];
      SolrInputDocument solrInputDoc = new SolrInputDocument();
      solrInputDoc.addField("id", x);
      solrInputDoc.addField("exchange", csvrow[0]);
      solrInputDoc.addField("stock_symbol", csvrow[1]);
      java.util.Date date = dateFormat.parse(csvrow[2]);
      solrInputDoc.addField("ddate", date);
      solrInputDoc.addField("stock_volume", Long.parseLong(csvrow[7]));
      if (!csvrow[3].equals("null")) solrInputDoc.addField("stock_price_open",
          Double.parseDouble(csvrow[3]));
      solrClient.add(solrInputDoc);
    }
    
    solrClient.commit(true, true);
  }
  
  private static HashMap<String,String> convertToMap(ResultSet rs)
      throws SQLException {
    HashMap<String,String> fields = new HashMap<String,String>();
    try {
      fields.put("id", String.valueOf((Long) rs.getObject("id")));
    } catch (java.sql.SQLException e) {
      // Field doesn't exist
      fields.put("id", null);
    }
    try {
      fields.put("exchange", (String) rs.getObject("exchange"));
    } catch (java.sql.SQLException e) {
      // Field doesn't exist
      fields.put("exchange", null);
    }
    try {
      fields.put("stock_symbol", (String) rs.getObject("stock_symbol"));
    } catch (java.sql.SQLException e) {
      // Field doesn't exist
      fields.put("stock_symbol", null);
    }
    // fields.put("ddate", String.valueOf((java.sql.Date)
    // rs.getObject("ddate")));
    try {
      fields.put("stock_volume",
          String.valueOf((Long) rs.getObject("stock_volume")));
    } catch (java.sql.SQLException e) {
      // Field doesn't exist
      fields.put("stock_volume", null);
    }
    try {
      fields.put("stock_price_open",
          String.valueOf((Double) rs.getObject("stock_price_open")));
    } catch (java.sql.SQLException e) {
      // Field doesn't exist
      fields.put("stock_price_open", null);
    }
    System.out.println(fields.toString());
    return fields;
  }
  
  private static HashMap<String,String> convertToMap(SolrDocument sd) {
    HashMap<String,String> fields = new HashMap<String,String>();
    fields.put("id", String.valueOf(sd.get("id")));
    fields.put("exchange", String.valueOf(sd.get("exchange")));
    fields.put("stock_symbol", String.valueOf(sd.get("stock_symbol")));
    // fields.put("ddate", String.valueOf(sd.get("ddate")));
    fields.put("stock_volume", String.valueOf(sd.get("stock_volume")));
    fields.put("stock_price_open", String.valueOf(sd.get("stock_price_open")));
    System.out.println(fields.toString());
    return fields;
  }
  
  private static ArrayList<HashMap<String,String>> createDocs(QueryResponse qr) {
    ArrayList<HashMap<String,String>> docs = new ArrayList<HashMap<String,String>>();
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    for (SolrDocument solrDoc : qr.getResults()) {
      docs.add(convertToMap(solrDoc));
    }
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return docs;
  }
  
  private static ArrayList<HashMap<String,String>> createDocs(ResultSet rs)
      throws SQLException {
    ArrayList<HashMap<String,String>> docs = new ArrayList<HashMap<String,String>>();
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    while (rs.next()) {
      docs.add(convertToMap(rs));
    }
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return docs;
  }
  
  /**
   * Performs the Solr query and returns the response
   * 
   * @param name
   * @param params
   * @throws Exception
   */
  private static QueryResponse querySolr(ModifiableSolrParams params)
      throws Exception {
    System.out.println("===========================");
    System.out.println("Start Solr Query Parameters");
    System.out.println("===========================");
    System.out.println("Name : Value");
    for (String n : params.getParameterNames()) {
      String[] vals = params.getParams(n);
      for (String v : vals) {
        System.out.println(n + " : " + v);
      }
    }
    System.out.println("===========================");
    System.out.println("End Solr Query Parameters");
    System.out.println("===========================");
    QueryResponse qr = solrClient.query(params);
    return qr;
  }
  
  private static String querySolrJson(ModifiableSolrParams params)
      throws ClientProtocolException, IOException {
    params.set("wt", "json");
    System.out.println("===========================");
    System.out.println("Start Solr Query Parameters");
    System.out.println("===========================");
    System.out.println("Name : Value");
    for (String n : params.getParameterNames()) {
      String[] vals = params.getParams(n);
      for (String v : vals) {
        System.out.println(n + " : " + v);
      }
    }
    System.out.println("===========================");
    System.out.println("End Solr Query Parameters");
    System.out.println("===========================");
    HttpGet method = new HttpGet(solrUrl + "/select"
        + ClientUtils.toQueryString(params, false));
    HttpClient httpClient = HttpClientUtil
        .createClient(new ModifiableSolrParams());
    HttpResponse response = httpClient.execute(method);
    return IOUtils.toString(response.getEntity().getContent());
  }
  
  private static ResultSet querySql(String query) throws SQLException {
    System.out.println("===========================");
    System.out.println("Start SQL Query Parameters");
    System.out.println("===========================");
    System.out.println(query);
    System.out.println("===========================");
    System.out.println("End SQL Query Parameters");
    System.out.println("===========================");
    PreparedStatement ps = con.prepareStatement(query);
    ResultSet rs = ps.executeQuery();
    return rs;
  }
  
  @Test
  public void testCount() throws Exception {
    System.out.println("***********Starting Count Test***********");
    Assert.assertEquals(sqlCount(con), solrCount());
    System.out.println("***********End Count Test***********");
  }
  
  @Test
  public void testCount2() throws Exception {
    Integer count = sqlCount2(con);
    System.out.println("count: "+count);
  }
    
  private static Integer sqlCount(Connection con) throws Exception {
    String query = "SELECT COUNT(*) FROM stocks WHERE stock_symbol = 'QRR'";
    ResultSet rs = querySql(query);
    rs.next();
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out.println("QRR Count : " + String.valueOf(rs.getInt(1)));
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return rs.getInt(1);
  }
  
  private static Integer sqlCount2(Connection con) throws Exception {
    String query = "SELECT COUNT(*) FROM stocks "+
    "WHERE stock_symbol = 'QRR' AND "+
    "(ddate <= '2007-03-06' AND ddate >= '2007-02-27')";
    ResultSet rs = querySql(query);
    rs.next();
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out.println("QRR Count : " + String.valueOf(rs.getInt(1)));
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return rs.getInt(1);
  }
  
  private static Integer solrCount() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "stock_symbol:QRR");
    QueryResponse qr = querySolr(params);
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    System.out.println("QRR Count : "
        + String.valueOf(qr.getResults().getNumFound()));
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return (int) qr.getResults().getNumFound();
  }
  
  @Test
  public void testMax() throws Exception {
    System.out.println("***********Starting Max Test***********");
    Assert.assertEquals(sqlMax(con), solrMax());
    System.out.println("***********End Max Test***********");
  }
  
  private static long sqlMax(Connection con) throws Exception {
    String query = "SELECT MAX(stock_volume) FROM stocks";
    ResultSet rs = querySql(query);
    rs.next();
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out.println("Max Stock Volume Count : "
        + String.valueOf(rs.getLong(1)));
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return rs.getLong(1);
  }
  
  private static long solrMax() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "*:*");
    params.set("sort", "stock_volume desc");
    params.set("rows", "1");
    params.set("fl", "stock_volume");
    QueryResponse qr = querySolr(params);
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    long maxVolume = (Long) qr.getResults().get(0)
        .getFieldValue("stock_volume");
    System.out.println("Max Stock Volume Count : " + maxVolume);
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return maxVolume;
  }
  
  @Test
  public void testMin() throws Exception {
    System.out.println("***********Starting Min Test***********");
    Assert.assertEquals(sqlMin(con), solrMin());
    System.out.println("***********End Min Test***********");
  }
  
  private static long sqlMin(Connection con) throws Exception {
    String query = "SELECT MIN(stock_volume) FROM stocks";
    ResultSet rs = querySql(query);
    rs.next();
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out.println("Min Stock Volume Count : "
        + String.valueOf(rs.getLong(1)));
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return rs.getLong(1);
  }
  
  private static long solrMin() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "*:*");
    params.set("sort", "stock_volume asc");
    params.set("rows", "1");
    params.set("fl", "stock_volume");
    QueryResponse qr = querySolr(params);
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    long maxVolume = (Long.valueOf(qr.getResults().get(0)
        .getFieldValue("stock_volume").toString()));
    System.out.println("Min Stock Volume Count : " + maxVolume);
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return maxVolume;
  }
  
  @Test
  public void testSum() throws Exception {
    System.out.println("***********Starting Sum Test***********");
    Assert.assertEquals(sqlSum(con), solrSum());
    System.out.println("***********End Sum Test***********");
  }
  
  private static long sqlSum(Connection con) throws Exception {
    String query = "SELECT SUM(stock_volume) FROM stocks";
    ResultSet rs = querySql(query);
    rs.next();
    long sumVol = rs.getLong(1);
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out.println("Sum Stock Volume Count : " + String.valueOf(sumVol));
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return sumVol;
  }
  
  private static long solrSum() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "*:*");
    params.set("stats", "true");
    params.set("stats.field", "stock_volume");
    String jsonResults = querySolrJson(params);
    JSONObject json = (JSONObject) JSONSerializer
        .toJSON(jsonResults.toString());
    Double sum = (Double) json.getJSONObject("stats")
        .getJSONObject("stats_fields").getJSONObject("stock_volume").get("sum");
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    System.out.println("Sum Stock Volume Count : " + String.valueOf(sum));
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return sum.longValue();
  }
  
  @Test
  public void testStockDateRange() throws Exception {
    double r = sqlStockDateRange(con);
    System.out.println(""+r);
  }
  
  @Test
  public void testAverage() throws Exception {
    System.out.println("***********Starting Average Test***********");
    Assert.assertEquals(sqlAverage(con), solrAverage());
    System.out.println("***********End Average Test***********");
  }
  
  private static long sqlAverage(Connection con) throws Exception {
    String query = "SELECT AVG(stock_volume) FROM stocks";
    ResultSet rs = querySql(query);
    rs.next();
    long aveVol = rs.getLong(1);
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out
        .println("Average Stock Volume Count : " + String.valueOf(aveVol));
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return aveVol;
  }
  
  private static long solrAverage() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "*:*");
    params.set("stats", "true");
    params.set("stats.field", "stock_volume");
    String jsonResults = querySolrJson(params);
    JSONObject json = (JSONObject) JSONSerializer
        .toJSON(jsonResults.toString());
    Double aveVol = (Double) json.getJSONObject("stats")
        .getJSONObject("stats_fields").getJSONObject("stock_volume")
        .get("mean");
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    System.out
        .println("Average Stock Volume Count : " + String.valueOf(aveVol));
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return aveVol.longValue();
  }
  
  @Test
  public void testStockPriceNull() throws Exception {
    System.out.println("***********Starting Stock Price Null Test***********");
    Assert.assertEquals(sqlStockPriceNull(con), solrStockPriceNull());
    System.out.println("***********End Stock Price Null Test***********");
    
  }
  
  /**
   * Create a Solr query that looks for a record where the stock price is null
   * 
   * @throws Exception
   */
  private static ArrayList<HashMap<String,String>> solrStockPriceNull()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "-stock_price_open:[* TO *]");
    QueryResponse results = querySolr(params);
    return createDocs(results);
  }
  
  /**
   * Create a SQL query that looks for a record where the stock price is null
   * 
   * @throws Exception
   */
  private static ArrayList<HashMap<String,String>> sqlStockPriceNull(
      Connection con) throws Exception {
    ResultSet results = querySql("SELECT * FROM stocks WHERE stock_price_open IS NULL");
    return createDocs(results);
  }
  
  @Test
  public void testSelectLimit() throws Exception {
    System.out.println("***********Starting Select Limit Test***********");
    Assert.assertEquals(sqlSelectLimit(con).toString(), solrSelectLimit()
        .toString());
    System.out.println("***********End Select Limit Test***********");
  }
  
  private static ArrayList<HashMap<String,String>> solrSelectLimit()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "*:*");
    params.set("fl", "id,exchange,stock_volume");
    params.set("start", "9");
    params.set("rows", "6");
    QueryResponse results = querySolr(params);
    return createDocs(results);
  }
  
  private static ArrayList<HashMap<String,String>> sqlSelectLimit(Connection con)
      throws Exception {
    // Typically, SQL syntax would dictate a query like LIMIT 9, 6 to retrieve
    // 6 documents starting at an offset of 9. However, Derby does not support
    // LIMIT so we must add a WHERE clause to limit the result set
    ResultSet results = querySql("SELECT id,exchange,stock_volume FROM stocks WHERE id > 9");
    return createDocs(results);
  }
  
  @Test
  public void testGroupByAve() throws Exception {
    System.out.println("***********Starting Group By Average Test***********");
    ArrayList<HashMap<String,String>> sqlResults = sqlStockPriceGroupByAvg(con);
    ArrayList<HashMap<String,String>> solrResults = solrStockPriceGroupByAvg();
    for (int i = 0; i < sqlResults.size() && i < solrResults.size(); i++) {
      Assert.assertEquals(sqlResults.get(i).get("stock_symbol"), solrResults
          .get(i).get("stock_symbol"));
      Assert.assertEquals(sqlResults.get(i).get("mean").substring(0, 4),
          solrResults.get(i).get("mean").substring(0, 4));
    }
    System.out.println("***********End Group By Average Test***********");
  }
  
  private static ArrayList<HashMap<String,String>> solrStockPriceGroupByAvg()
      throws Exception {
    
    // List to keep results ordered
    // Map contains mean:value and stock_symbol:value pairs
    ArrayList<HashMap<String,String>> groupAverages = new ArrayList<HashMap<String,String>>();
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("stats", "true");
    params.set("stats.field", "stock_price_open");
    params.set("stats.facet", "stock_symbol");
    params.set("q", "*:*");
    String jsonResults = querySolrJson(params);
    JSONObject json = (JSONObject) JSONSerializer
        .toJSON(jsonResults.toString());
    JSONObject jsonAverages = json.getJSONObject("stats")
        .getJSONObject("stats_fields").getJSONObject("stock_price_open")
        .getJSONObject("facets").getJSONObject("stock_symbol");
    for (Object key : jsonAverages.keySet()) {
      HashMap<String,String> stockAverage = new HashMap<String,String>();
      stockAverage.put("stock_symbol", key.toString());
      stockAverage.put("mean", jsonAverages.getJSONObject(key.toString())
          .getString("mean"));
      groupAverages.add(stockAverage);
    }
    System.out.println("===========================");
    System.out.println("Start Solr Results");
    System.out.println("===========================");
    System.out.println(groupAverages);
    System.out.println("===========================");
    System.out.println("End Solr Results");
    System.out.println("===========================");
    return groupAverages;
  }
  
  private static ArrayList<HashMap<String,String>> sqlStockPriceGroupByAvg(
      Connection con) throws Exception {
    ResultSet results = querySql("SELECT stock_symbol, AVG(stock_price_open) FROM stocks GROUP BY stock_symbol");
    ArrayList<HashMap<String,String>> groupAverages = new ArrayList<HashMap<String,String>>();
    while (results.next()) {
      HashMap<String,String> kv = new HashMap<String,String>();
      kv.put("stock_symbol", results.getString("stock_symbol"));
      kv.put("mean", String.valueOf(results.getDouble(2)));
      groupAverages.add(kv);
    }
    System.out.println("===========================");
    System.out.println("Start SQL Results");
    System.out.println("===========================");
    System.out.println(groupAverages);
    System.out.println("===========================");
    System.out.println("End SQL Results");
    System.out.println("===========================");
    return groupAverages;
  }
  
  @Test
  public void testPriceRange() throws Exception {
    System.out.println("***********Starting Price Range Test***********");
    Assert.assertEquals(sqlStockPriceRange(con), solrStockPriceRange());
    System.out.println("***********End Price Range Test***********");
  }
  
  private static ArrayList<HashMap<String,String>> solrStockPriceRange()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "+stock_price_open:[2.38 TO 2.64] +stock_symbol:QTM");
    return createDocs(querySolr(params));
  }
  
  private static ArrayList<HashMap<String,String>> sqlStockPriceRange(
      Connection con) throws Exception {
    String query = "SELECT * FROM stocks "
        + "WHERE stock_symbol = 'QRR' AND (stock_price_open <= 2.64 AND stock_price_open >= 2.38)";
    return createDocs(querySql(query));
  }
  
  private static Double sqlStockDateRange(
      Connection con) throws Exception {
    String query = "SELECT AVG(stock_price_open) AS stock_price_open_avg FROM stocks "
        + "WHERE stock_symbol = 'QRR' AND "+
        "(ddate <= '2007-03-06' AND ddate >= '2007-02-27') ";
    ResultSet rs = querySql(query);
    rs.next();
    Object o = rs.getObject(1);
    return (Double)o;
  }
  
  @Test
  public void testSort() throws Exception {
    System.out.println("***********Starting Sort Test***********");
    Assert.assertEquals(sqlStockSort(con), solrStockSort());
    System.out.println("***********End Sort Test***********");
  }
  
  private static ArrayList<HashMap<String,String>> solrStockSort()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("indent", "true");
    params.set("q", "+stock_symbol:QTM +stock_price_open:[* TO *]");
    params.set("sort", "stock_price_open desc");
    return createDocs(querySolr(params));
  }
  
  private static ArrayList<HashMap<String,String>> sqlStockSort(Connection con)
      throws Exception {
    String query = "SELECT * FROM stocks WHERE NOT stock_price_open IS NULL AND stock_symbol = 'QTM' ORDER BY stock_price_open DESC";
    return createDocs(querySql(query));
  }
  
  @Test
  public void testComplexQuery() throws Exception {
    System.out.println("***********Starting Complex Test***********");
    Assert.assertEquals(sqlComplexQuery(con), solrComplexQuery());
    System.out.println("***********End Complex Test***********");
    
  }
  
  private static ArrayList<HashMap<String,String>> sqlComplexQuery(
      Connection con) throws Exception {
    String query = "SELECT * FROM stocks "
        + "WHERE (stock_symbol = 'QTM' AND stock_price_open > 2.57)"
        + " OR (stock_symbol = 'QRR' AND stock_volume >= 95000 AND stock_volume < 173000)"
        + " ORDER BY stock_volume DESC,stock_price_open ASC";
    return createDocs(querySql(query));
  }
  
  private static ArrayList<HashMap<String,String>> solrComplexQuery()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("indent", "true");
    params.set("q", "(+stock_symbol:QTM +stock_price_open:{2.57 TO *]) "
        + "(+stock_symbol:QRR +stock_volume:[95000 TO 173000})");
    params.set("sort", "stock_volume desc,stock_price_open asc");
    return createDocs(querySolr(params));
  }
  
  @Test
  public void testLike() throws Exception {
    System.out.println("***********Starting Like Test***********");
    Assert.assertEquals(sqlLike(con), solrLike());
    System.out.println("***********End Like Test***********");
  }
  
  private static ArrayList<HashMap<String,String>> sqlLike(Connection con)
      throws Exception {
    String query = "SELECT * FROM stocks " + "WHERE stock_symbol LIKE 'QR%'";
    return createDocs(querySql(query));
  }
  
  private static ArrayList<HashMap<String,String>> solrLike() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "stock_symbol:QR*");
    params.set("rows", "100");
    return createDocs(querySolr(params));
  }
  
  @Test
  public void testUpdate() throws Exception {
    System.out.println("***********Starting Update Test***********");
    Assert.assertEquals(sqlUpdate(con), solrUpdate());
    System.out.println("***********End Update Test***********");
  }
  
  private static ArrayList<HashMap<String,String>> sqlUpdate(Connection con)
      throws Exception {
    String updateQuery = "UPDATE stocks SET stock_volume = ? WHERE stock_symbol = 'QRR'";
    System.out.println(updateQuery);
    PreparedStatement ps1 = con.prepareStatement(updateQuery);
    ps1.setInt(1, 20130227);
    ps1.executeUpdate();
    
    String verifyQuery = "SELECT * FROM stocks WHERE stock_symbol = 'QRR'";
    return createDocs(querySql(verifyQuery));
  }
  
  private static ArrayList<HashMap<String,String>> solrUpdate()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "stock_symbol:QRR");
    params.set("rows", "100");
    int val = 20130227;
    QueryResponse qr = querySolr(params);
    ArrayList<SolrInputDocument> inputDocs = new ArrayList<SolrInputDocument>();
    for (SolrDocument doc : qr.getResults()) {
      SolrInputDocument inputDoc = ClientUtils.toSolrInputDocument(doc);
      inputDoc.setField("stock_volume", val);
      inputDocs.add(inputDoc);
    }
    solrClient.add(inputDocs);
    solrClient.commit();
    
    ModifiableSolrParams verifyParams = new ModifiableSolrParams();
    verifyParams.set("q", "stock_symbol:QRR");
    verifyParams.set("rows", "100");
    
    return createDocs(querySolr(verifyParams));
  }
}
