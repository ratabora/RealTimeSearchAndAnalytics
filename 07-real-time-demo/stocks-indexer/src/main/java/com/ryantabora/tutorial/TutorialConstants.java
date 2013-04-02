package com.ryantabora.tutorial;

public class TutorialConstants {
  
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
