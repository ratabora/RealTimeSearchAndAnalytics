Real Time Search and Analytics on Big Data - SQL to Solr
=============

Introduction
-------
This exercise is composed of a set of JUnit tests that run through a set of SQL queries and then the equivalent Solr queries. Each test will compare the results to ensure they are the same. The tests themselves will spawn an embedded Solr process as well as an embedded Derby (SQL) process to run the queries against. This exercise is intended to be run on the student's local environment.

Prerequisites
-------
* Maven 3.0

Running the Examples
-------

Students are encouraged to run the examples and then look at the code. You can see both the SQL query and the Solr query being created side by side. It is helpful to look at and compare the queries to understand the SQL translation into Solr.

You can find the code in ./src/test/java/com/ryantabora/tutorial/SQLtoSolr.java

	$ cd RealTimeSearchAndAnalytics/06-sql-to-solr/

Each of these examples are actually Maven tests. So you can execute the test in the command line and it will use JUnit to compare the results and verify they are the same. If the test outputs a SUCCESS then you know the results are the same. You should also look out for some console output the test creates. You will see four main sections for each test, generally in this format.

Starting the test

	***********Starting Test***********

Displaying the SQL Parameters

	===========================
	Start SQL Query Parameters
	===========================
	...
	===========================
	End SQL Query Parameters
	===========================

Displaying the SQL Results	

	===========================
	Start SQL Results
	===========================
	...
	===========================
	End SQL Results
	===========================

Displaying the Solr Query Parameters

	===========================
	Start Solr Query Parameters
	===========================
	...
	===========================
	End Solr Query Parameters
	===========================

Displaying the Solr Results

	===========================
	Start Solr Results
	===========================
	...
	===========================
	End Solr Results
	===========================

Ending the test

	***********End Test***********

You may see some messages in between that are coming from the Derby/Solr communication logs. You can ignore that information. You will also see some information about downloading dependencies and such through Maven, you can ignore that information as well.

On to the tests!

### Calculating Counts

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testCount
	
#### Results

	***********Starting Count Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT COUNT(*) FROM stocks WHERE stock_symbol = 'QRR'
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	QRR Count : 9
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : stock_symbol:QRR
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	QRR Count : 9
	===========================
	End Solr Results
	===========================
	***********End Count Test***********
	
	...

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.909 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 8.427s
	[INFO] Finished at: Thu Feb 21 14:11:45 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Calculating Max Values

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testMax

#### Results

	***********Starting Max Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT MAX(stock_volume) FROM stocks
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	Max Stock Volume Count : 5050100
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : *:*
	sort : stock_volume desc
	rows : 1
	fl : stock_volume
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	Max Stock Volume Count : 5050100
	===========================
	End Solr Results
	===========================
	***********End Max Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.923 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 7.210s
	[INFO] Finished at: Thu Feb 21 14:17:57 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Calculating Min Values

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testMin

#### Results

	***********Starting Min Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT MIN(stock_volume) FROM stocks
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	Min Stock Volume Count : 34900
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : *:*
	sort : stock_volume asc
	rows : 1
	fl : stock_volume
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	Min Stock Volume Count : 34900
	===========================
	End Solr Results
	===========================
	***********End Min Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.383 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 8.456s
	[INFO] Finished at: Thu Feb 21 14:20:12 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Calculating Sum Values

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testSum

#### Results

	***********Starting Sum Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT SUM(stock_volume) FROM stocks
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	Sum Stock Volume Count : 22015700
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : *:*
	stats : true
	stats.field : stock_volume
	wt : json
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	Sum Stock Volume Count : 2.20157E7
	===========================
	End Solr Results
	===========================
	***********End Sum Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.881 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 7.004s
	[INFO] Finished at: Thu Feb 21 14:23:33 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Calculating Average Values

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testAverage

#### Results

	***********Starting Average Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT AVG(stock_volume) FROM stocks
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	Average Stock Volume Count : 1467713
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : *:*
	stats : true
	stats.field : stock_volume
	wt : json
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	Average Stock Volume Count : 1467713.4
	===========================
	End Solr Results
	===========================
	***********End Average Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.832 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 6.986s
	[INFO] Finished at: Thu Feb 21 14:24:23 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Querying Fields Without a Value

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testStockPriceNull

#### Results

	***********Starting Stock Price Null Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT * FROM stocks WHERE stock_price_open IS NULL
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=6, stock_price_open=null, stock_volume=5050100, stock_symbol=QTM, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : -stock_price_open:[* TO *]
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=6, stock_price_open=null, stock_volume=5050100, stock_symbol=QTM, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Stock Price Null Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.908 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 7.193s
	[INFO] Finished at: Thu Feb 21 14:25:07 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Selecting Fields to Return/Limiting Result Set

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testSelectLimit

#### Results



### Calculating the Average Value for a Group

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testGroupByAve

#### Results

	***********Starting Select Limit Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT id,exchange,stock_volume FROM stocks WHERE id > 9
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=10, stock_price_open=null, stock_volume=171900, stock_symbol=null, exchange=NYSE}
	{id=11, stock_price_open=null, stock_volume=66900, stock_symbol=null, exchange=NYSE}
	{id=12, stock_price_open=null, stock_volume=232000, stock_symbol=null, exchange=NYSE}
	{id=13, stock_price_open=null, stock_volume=173000, stock_symbol=null, exchange=NYSE}
	{id=14, stock_price_open=null, stock_volume=200800, stock_symbol=null, exchange=NYSE}
	{id=15, stock_price_open=null, stock_volume=95000, stock_symbol=null, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : *:*
	fl : id,exchange,stock_volume
	start : 9
	rows : 6
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=10, stock_price_open=null, stock_volume=171900, stock_symbol=null, exchange=NYSE}
	{id=11, stock_price_open=null, stock_volume=66900, stock_symbol=null, exchange=NYSE}
	{id=12, stock_price_open=null, stock_volume=232000, stock_symbol=null, exchange=NYSE}
	{id=13, stock_price_open=null, stock_volume=173000, stock_symbol=null, exchange=NYSE}
	{id=14, stock_price_open=null, stock_volume=200800, stock_symbol=null, exchange=NYSE}
	{id=15, stock_price_open=null, stock_volume=95000, stock_symbol=null, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Select Limit Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.854 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 7.154s
	[INFO] Finished at: Thu Feb 21 14:25:50 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Querying Range Values

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testPriceRange

#### Results

	***********Starting Price Range Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT * FROM stocks WHERE stock_symbol = 'QTM' AND (stock_price_open <= 2.64 AND stock_price_open >= 2.38)
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=2, stock_price_open=2.38, stock_volume=2687600, stock_symbol=QTM, exchange=NYSE}
	{id=3, stock_price_open=2.57, stock_volume=4529800, stock_symbol=QTM, exchange=NYSE}
	{id=4, stock_price_open=2.64, stock_volume=2688600, stock_symbol=QTM, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : +stock_price_open:[2.38 TO 2.64] +stock_symbol:QTM
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=2, stock_price_open=2.38, stock_volume=2687600, stock_symbol=QTM, exchange=NYSE}
	{id=3, stock_price_open=2.57, stock_volume=4529800, stock_symbol=QTM, exchange=NYSE}
	{id=4, stock_price_open=2.64, stock_volume=2688600, stock_symbol=QTM, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Price Range Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.659 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 6.667s
	[INFO] Finished at: Thu Feb 21 14:26:43 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Sorting Results

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testSort

#### Results

	***********Starting Sort Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT * FROM stocks WHERE NOT stock_price_open IS NULL AND stock_symbol = 'QTM' ORDER BY stock_price_open DESC
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=5, stock_price_open=2.69, stock_volume=2959700, stock_symbol=QTM, exchange=NYSE}
	{id=4, stock_price_open=2.64, stock_volume=2688600, stock_symbol=QTM, exchange=NYSE}
	{id=3, stock_price_open=2.57, stock_volume=4529800, stock_symbol=QTM, exchange=NYSE}
	{id=2, stock_price_open=2.38, stock_volume=2687600, stock_symbol=QTM, exchange=NYSE}
	{id=1, stock_price_open=2.37, stock_volume=3013600, stock_symbol=QTM, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	indent : true
	q : +stock_symbol:QTM +stock_price_open:[* TO *]
	sort : stock_price_open desc
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=5, stock_price_open=2.69, stock_volume=2959700, stock_symbol=QTM, exchange=NYSE}
	{id=4, stock_price_open=2.64, stock_volume=2688600, stock_symbol=QTM, exchange=NYSE}
	{id=3, stock_price_open=2.57, stock_volume=4529800, stock_symbol=QTM, exchange=NYSE}
	{id=2, stock_price_open=2.38, stock_volume=2687600, stock_symbol=QTM, exchange=NYSE}
	{id=1, stock_price_open=2.37, stock_volume=3013600, stock_symbol=QTM, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Sort Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.729 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 6.939s
	[INFO] Finished at: Thu Feb 21 14:27:23 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### AND/OR Logic

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testComplexQuery

#### Results

	***********Starting Complex Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT * FROM stocks WHERE (stock_symbol = 'QTM' AND stock_price_open > 2.57) OR (stock_symbol = 'QRR' AND stock_volume >= 95000 AND stock_volume < 173000) ORDER BY stock_volume DESC,stock_price_open ASC
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=5, stock_price_open=2.69, stock_volume=2959700, stock_symbol=QTM, exchange=NYSE}
	{id=4, stock_price_open=2.64, stock_volume=2688600, stock_symbol=QTM, exchange=NYSE}
	{id=10, stock_price_open=13.75, stock_volume=171900, stock_symbol=QRR, exchange=NYSE}
	{id=15, stock_price_open=14.78, stock_volume=95000, stock_symbol=QRR, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	indent : true
	q : (+stock_symbol:QTM +stock_price_open:{2.57 TO *]) (+stock_symbol:QRR +stock_volume:[95000 TO 173000})
	sort : stock_volume desc,stock_price_open asc
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=5, stock_price_open=2.69, stock_volume=2959700, stock_symbol=QTM, exchange=NYSE}
	{id=4, stock_price_open=2.64, stock_volume=2688600, stock_symbol=QTM, exchange=NYSE}
	{id=10, stock_price_open=13.75, stock_volume=171900, stock_symbol=QRR, exchange=NYSE}
	{id=15, stock_price_open=14.78, stock_volume=95000, stock_symbol=QRR, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Complex Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.788 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 7.065s
	[INFO] Finished at: Thu Feb 21 14:28:10 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Querying LIKE

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testLike

#### Results

	***********Starting Like Test***********
	===========================
	Start SQL Query Parameters
	===========================
	SELECT * FROM stocks WHERE stock_symbol LIKE 'QR%'
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=7, stock_price_open=13.75, stock_volume=34900, stock_symbol=QRR, exchange=NYSE}
	{id=8, stock_price_open=13.75, stock_volume=53200, stock_symbol=QRR, exchange=NYSE}
	{id=9, stock_price_open=13.77, stock_volume=58600, stock_symbol=QRR, exchange=NYSE}
	{id=10, stock_price_open=13.75, stock_volume=171900, stock_symbol=QRR, exchange=NYSE}
	{id=11, stock_price_open=13.75, stock_volume=66900, stock_symbol=QRR, exchange=NYSE}
	{id=12, stock_price_open=13.7, stock_volume=232000, stock_symbol=QRR, exchange=NYSE}
	{id=13, stock_price_open=14.08, stock_volume=173000, stock_symbol=QRR, exchange=NYSE}
	{id=14, stock_price_open=14.5, stock_volume=200800, stock_symbol=QRR, exchange=NYSE}
	{id=15, stock_price_open=14.78, stock_volume=95000, stock_symbol=QRR, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : stock_symbol:QR*
	rows : 100
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=7, stock_price_open=13.75, stock_volume=34900, stock_symbol=QRR, exchange=NYSE}
	{id=8, stock_price_open=13.75, stock_volume=53200, stock_symbol=QRR, exchange=NYSE}
	{id=9, stock_price_open=13.77, stock_volume=58600, stock_symbol=QRR, exchange=NYSE}
	{id=10, stock_price_open=13.75, stock_volume=171900, stock_symbol=QRR, exchange=NYSE}
	{id=11, stock_price_open=13.75, stock_volume=66900, stock_symbol=QRR, exchange=NYSE}
	{id=12, stock_price_open=13.7, stock_volume=232000, stock_symbol=QRR, exchange=NYSE}
	{id=13, stock_price_open=14.08, stock_volume=173000, stock_symbol=QRR, exchange=NYSE}
	{id=14, stock_price_open=14.5, stock_volume=200800, stock_symbol=QRR, exchange=NYSE}
	{id=15, stock_price_open=14.78, stock_volume=95000, stock_symbol=QRR, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Like Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.724 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 6.762s
	[INFO] Finished at: Thu Feb 21 14:28:57 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------

### Updating Documents Based on a Query

#### Maven Command

	$ mvn test -Dtest=SQLtoSolr#testUpdate

#### Results

	***********Starting Update Test***********
	UPDATE stocks SET stock_volume = ? WHERE stock_symbol = 'QRR'
	===========================
	Start SQL Query Parameters
	===========================
	SELECT * FROM stocks WHERE stock_symbol = 'QRR'
	===========================
	End SQL Query Parameters
	===========================
	===========================
	Start SQL Results
	===========================
	{id=7, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=8, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=9, stock_price_open=13.77, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=10, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=11, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=12, stock_price_open=13.7, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=13, stock_price_open=14.08, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=14, stock_price_open=14.5, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=15, stock_price_open=14.78, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	===========================
	End SQL Results
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : stock_symbol:QRR
	rows : 100
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Query Parameters
	===========================
	Name : Value
	q : stock_symbol:QRR
	rows : 100
	===========================
	End Solr Query Parameters
	===========================
	===========================
	Start Solr Results
	===========================
	{id=7, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=8, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=9, stock_price_open=13.77, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=10, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=11, stock_price_open=13.75, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=12, stock_price_open=13.7, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=13, stock_price_open=14.08, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=14, stock_price_open=14.5, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	{id=15, stock_price_open=14.78, stock_volume=20130227, stock_symbol=QRR, exchange=NYSE}
	===========================
	End Solr Results
	===========================
	***********End Update Test***********
	...
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.177 sec

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 7.360s
	[INFO] Finished at: Thu Feb 21 14:29:35 PST 2013
	[INFO] Final Memory: 6M/81M
	[INFO] ------------------------------------------------------------------------
