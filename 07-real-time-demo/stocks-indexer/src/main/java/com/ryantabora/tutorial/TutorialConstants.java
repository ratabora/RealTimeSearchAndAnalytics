package com.ryantabora.tutorial;

public class TutorialConstants {
  
  /**
   * HBase Specific
   */
  public static final String HBASE_STOCKS_TABLE = "STOCKS";
  public static final String HBASE_RECORDS_FAMILY = "RECORDS";
  public static final String[] HBASE_STOCKS_FAMILIES = {HBASE_RECORDS_FAMILY};
  public static final String HBASE_ROW_KEY = "rowkey";
  public static final String KEY_DELIMITER = "|";
  
  /**
   * Data Specific
   */
  public static final String[] STOCKS_DAILY_SCHEMA = {"exchange",
      "stock_symbol", "date", "stock_price_open", "stock_price_high",
      "stock_price_low", "stock_price_close", "stock_volume",
      "stock_price_adj_close"};
  public static final String[] STOCKS_DIVIDENDS_SCHEMA = {"exchange",
      "stock_symbol", "date", "dividends"};
  
  /**
   * Data Specific
   */
  public static final String RECORD_DELIMITER = ":";
  public static final String DAILY = "DAILY";
  public static final String DIVIDENDS = "DIVIDENDS";
  
}
