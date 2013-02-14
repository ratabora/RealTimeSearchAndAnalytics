package com.ryantabora.tutorial;

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

import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.internal.csv.CSVParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO
// get more data
// facet + stats = groupby
// add sort by = orderby
// range queries
// count
// count + facet = group by
// LIKE
// 
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
  
  @Test
  public void testMax() throws Exception {
    sqlmax1(con);
    sqlmax2(con);
    // TODO: Create Solr Comparison
  }
  
  @Test
  public void testMin() throws Exception {
    sqlmin1(con);
    // TODO: Create Solr Comparison
  }
  
  @Test
  public void testSum() throws Exception {
    sqlsum1(con);
    // TODO: Create Solr Comparison
  }
  
  @Test
  public void testStockPriceNull() throws Exception {
    System.out.println("***********Starting Stock Price Null Test***********");
    Assert.assertEquals(sqlStockPriceNull(con), solrStockPriceNull());
    System.out.println("***********End Stock Price Null Test***********");
    
  }
  
  @Test
  public void testSelectLimit() throws Exception {
    System.out.println("***********Starting Select Limit Test***********");
    Assert.assertEquals(sqlSelectLimit(con).toString(), solrSelectLimit()
        .toString());
    System.out.println("***********End Select Limit Test***********");
  }
  
  @Test
  public void testGroupByAve() throws Exception {
    System.out.println("***********Starting Group By Average Test***********");
    sqlStockPriceGroupByAvg(con);
    solrStockPriceGroupByAvg();
    System.out.println("***********End Group By Average Test***********");
  }
  
  @Test
  public void testPriceRange() throws Exception {
    sqlStockPriceRange(con);
    solrStockPriceRange();
  }
  
  @Test
  public void testSort() throws Exception {
    sqlStockSort(con);
    solrStockSort();
  }
  
  @Test
  public void testCount() throws Exception {
    sqlCount1(con);
    // TODO: Create Solr Comparison
  }
  
  @Test
  public void testPriceOpenAgg() throws Exception {
    // stockPriceOpenAgg();
    
  }
  
  @Test
  public void testStockPriceRange() throws Exception {
    solrStockPriceRange2();
  }
  
  @Test
  public void testComplexQuery() throws Exception {
    sqlComplexQuery1(con);
    solrComplexQuery1();
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
    
    solrClient = new HttpSolrServer("http://localhost:8983/solr");
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
  
  // private static SolrInputDocument createSolrDoc(ResultSet rs, List<String>
  // cols)
  // throws Exception {
  // SolrInputDocument solrInputDocument = new SolrInputDocument();
  // String str = "";
  // for (String col : cols) {
  // col = col.toLowerCase();
  // Object obj = rs.getObject(col);
  // str += " | " + obj;
  // solrInputDocument.addField(col, obj);
  // }
  // System.out.println(str);
  // return solrInputDocument;
  // }
  
  // private static List<String> printRowMeta(ResultSet rs) throws Exception {
  // ResultSetMetaData rsmd = rs.getMetaData();
  // int c = rsmd.getColumnCount();
  // List<String> cols = new ArrayList<String>();
  // for (int x = 1; x <= c; x++) {
  // String cname = rsmd.getColumnName(x);
  // cols.add(cname);
  // }
  // String header = "| " + StringUtil.concatEntries(cols, " | ", " | ");
  // System.out.println(header);
  // return cols;
  // }
  
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
    System.out.println(qr.toString());
    return qr;
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
    // LIMIT so we must add a WHERE claus to limit the result set
    ResultSet results = querySql("SELECT id,exchange,stock_volume FROM stocks WHERE id > 9");
    return createDocs(results);
  }
  
  private static ArrayList<HashMap<String,String>> solrStockPriceGroupByAvg()
      throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("stats", "true");
    params.set("stats.field", "stock_price_open");
    params.set("stats.facet", "stock_symbol");
    params.set("q", "*:*");
    QueryResponse results = querySolr(params);
    ArrayList<HashMap<String,String>> groupAverages = new ArrayList<HashMap<String,String>>();
    JSONObject json = (JSONObject) JSONSerializer.toJSON(results.toString());
    return groupAverages;
  }
  
  private static ArrayList<HashMap<String,String>> sqlStockPriceGroupByAvg(
      Connection con) throws Exception {
    ResultSet results = querySql("SELECT stock_symbol, AVG(stock_price_open) FROM stocks GROUP BY stock_symbol");
    ArrayList<HashMap<String,String>> groupAverages = new ArrayList<HashMap<String,String>>();
    while (results.next()) {
      HashMap<String,String> kv = new HashMap<String,String>();
      kv.put(results.getString(1), String.valueOf(results.getDouble(2)));
      groupAverages.add(kv);
    }
    return groupAverages;
  }
  
  private static void solrStockPriceRange() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", "+stock_price_open:[2.38 TO 2.64] +stock_symbol:QTM");
    querySolr(params);
  }
  
  private static void sqlStockPriceRange(Connection con) throws Exception {
    String s1 = "SELECT * FROM stocks "
        + "WHERE stock_symbol = 'QTM' AND (stock_price_open <= 2.64 AND stock_price_open >= 2.38)";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    while (rs1.next()) {
      // printSQLResult(rs1);
    }
  }
  
  private static void solrStockPriceRange2() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("indent", "true");
    params.set("q", "+stock_price_open:[2.38 TO 2.64} +stock_symbol:QTM");
    
    // printSolrResults(querySolr(params));
  }
  
  private static void solrStockSort() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("indent", "true");
    params.set("q", "+stock_symbol:QTM +stock_price_open:[* TO *]");
    params.set("sort", "stock_price_open desc");
    
    // printSolrResults(querySolr(params));
  }
  
  private static void sqlStockSort(Connection con) throws Exception {
    String s1 = "SELECT * FROM stocks WHERE NOT stock_price_open IS NULL AND stock_symbol = 'QTM' ORDER BY stock_price_open DESC";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    while (rs1.next()) {
      // printSQLResult(rs1);
    }
  }
  
  private static void stockPriceOpenAgg() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("stats", "true");
    params.set("stats.field", "stock_price_open");
    params.set("indent", "true");
    params.set("q", "*:*");
    
    // printSolrResults(querySolr(params));
  }
  
  private static void sqlavg1(Connection con) throws Exception {
    String s1 = "SELECT AVG(stock_volume) FROM stocks WHERE stock_symbol = ?";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ps1.setString(1, "QTM");
    ResultSet rs1 = ps1.executeQuery();
    if (rs1.next()) {
      double maxvol = rs1.getDouble(1);
      System.out.println(maxvol);
    }
  }
  
  private static void sqlsum1(Connection con) throws Exception {
    String s1 = "SELECT SUM(stock_volume) FROM stocks WHERE stock_symbol = ?";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ps1.setString(1, "QTM");
    ResultSet rs1 = ps1.executeQuery();
    if (rs1.next()) {
      long maxvol = rs1.getLong(1);
      System.out.println(maxvol);
    }
  }
  
  private static void sqlmin1(Connection con) throws Exception {
    String s1 = "SELECT MIN(stock_volume) FROM stocks WHERE stock_symbol = ?";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ps1.setString(1, "QTM");
    ResultSet rs1 = ps1.executeQuery();
    if (rs1.next()) {
      long maxvol = rs1.getLong(1);
      System.out.println(maxvol);
    }
  }
  
  private static void sqlmax2(Connection con) throws Exception {
    String s1 = "SELECT MAX(stock_volume) FROM stocks WHERE stock_symbol = ?";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ps1.setString(1, "QTM");
    ResultSet rs1 = ps1.executeQuery();
    if (rs1.next()) {
      long maxvol = rs1.getLong(1);
      System.out.println(maxvol);
    }
  }
  
  private static void sqlmax1(Connection con) throws Exception {
    String s1 = "SELECT MAX(stock_volume) FROM stocks";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    if (rs1.next()) {
      long maxvol = rs1.getLong(1);
      System.out.println(maxvol);
    }
  }
  
  private static void sqlorderby1(Connection con) throws Exception {
    String s1 = "SELECT * FROM stocks ORDER BY stock_volume ASC";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    while (rs1.next()) {
      // printSQLResult(rs1);
    }
  }
  
  private static void sqlComplexQuery1(Connection con) throws Exception {
    String s1 = "SELECT * FROM stocks "
        + "WHERE (stock_symbol = 'QTM' AND stock_price_open > 2.57)"
        + " OR (stock_symbol = 'QRR' AND stock_volume >= 95000 AND stock_volume < 173000)"
        + " ORDER BY stock_volume DESC,stock_price_open ASC";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    while (rs1.next()) {
      // printSQLResult(rs1);
    }
  }
  
  private static void solrComplexQuery1() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("indent", "true");
    params.set("q", "(+stock_symbol:QTM +stock_price_open:{2.57 TO *]) "
        + "(+stock_symbol:QRR +stock_volume:[95000 TO 173000})");
    params.set("sort", "stock_volume desc,stock_price_open asc");
    querySolr(params);
  }
  
  private static void sqlLikeQuery1(Connection con) throws Exception {
    String s1 = "SELECT * FROM stocks " + "WHERE stock_symbol LIKE 'Q%'";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    while (rs1.next()) {
      // printSQLResult(rs1);
    }
  }
  
  private static void sqlCount1(Connection con) throws Exception {
    String s1 = "SELECT COUNT(*) FROM stocks WHERE stock_symbol = 'QRR'";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    ResultSet rs1 = ps1.executeQuery();
    while (rs1.next()) {
      int c = rs1.getInt(1);
      System.out.println(c);
    }
  }
  
  private static void sqlUpdate1(Connection con) throws Exception {
    String s1 = "UPDATE stocks SET stock_volume = ? WHERE stock_symbol = 'QRR' AND ddate = ?";
    System.out.println(s1);
    PreparedStatement ps1 = con.prepareStatement(s1);
    java.util.Date date = dateFormat.parse("2007-02-26");
    ps1.setDate(1, new java.sql.Date(date.getTime()));
    ps1.executeUpdate();
  }
}
