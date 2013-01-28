Real Time Search and Analytics on Big Data - Real Time Demo
=============

Introduction
-------
This demo contains two applications:
1. A Java application that indexes stock data into Solr.
2. An HTML web page that uses JavaScript and AJAX to generate a graph from data in Solr.
The interesting part about this demo is that we are indexing data in a streaming fashion and querying on it in real time. As the data is being indexed, we query on that data in real time and display it in a graph. Essentially, this demo is meant to replicate ticker like data being indexed and indexed on in real time.

Prerequisites
-------
* An Environment with Solr Installed (The instructor should provide a cluster on EC2)
* An Environment with HBase Installed (The instructor should provide a cluster on EC2)

The Indexing Job
-------

### Building the stocks-indexer jar

The real-time-stocks-indexer-1.0-SNAPSHOT.jar is already provided in the directory for this exercise, however if you want to build the jar yourself you can use the following Maven command and should see similar results. The Maven command will create the jar in the ./target directory.


	$ cd ~/code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/
	$ ls -l
	total 85464
	-rw-r--r--  1 ryantabora  staff      2491 Jan 22 16:36 pom.xml
	-rw-r--r--  1 ryantabora  staff  43749464 Jan 25 15:44 real-time-stocks-indexer-1.0-SNAPSHOT.jar
	drwxr-xr-x  4 ryantabora  staff       136 Jan 22 15:38 src
	$ mvn clean package
	[INFO] Scanning for projects...
	[INFO]                                                                         
	[INFO] ------------------------------------------------------------------------
	[INFO] Building Real Time Stocks Indexer 1.0-SNAPSHOT
	[INFO] ------------------------------------------------------------------------
	[INFO] 
	[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ real-time-stocks-indexer ---
	[INFO] Deleting /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/target
	[INFO] 
	[INFO] --- maven-resources-plugin:2.5:resources (default-resources) @ real-time-stocks-indexer ---
	[debug] execute contextualize
	[INFO] Using 'UTF-8' encoding to copy filtered resources.
	[INFO] skip non existing resourceDirectory /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/src/main/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ real-time-stocks-indexer ---
	[INFO] Compiling 6 source files to /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/target/classes
	[INFO] 
	[INFO] --- maven-resources-plugin:2.5:testResources (default-testResources) @ real-time-stocks-indexer ---
	[debug] execute contextualize
	[INFO] Using 'UTF-8' encoding to copy filtered resources.
	[INFO] skip non existing resourceDirectory /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/src/test/resources
	[INFO] 
	[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ real-time-stocks-indexer ---
	[INFO] Nothing to compile - all classes are up to date
	[INFO] 
	[INFO] --- maven-surefire-plugin:2.10:test (default-test) @ real-time-stocks-indexer ---
	[INFO] No tests to run.
	[INFO] Surefire report directory: /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/target/surefire-reports

	-------------------------------------------------------
	 T E S T S
	-------------------------------------------------------

	Results :

	Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

	[INFO] 
	[INFO] --- maven-jar-plugin:2.3.2:jar (default-jar) @ real-time-stocks-indexer ---
	[INFO] Building jar: /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/target/real-time-stocks-indexer-1.0-SNAPSHOT.jar
	[INFO] 
	[INFO] --- maven-shade-plugin:2.0:shade (default) @ real-time-stocks-indexer ---
	[INFO] Including junit:junit:jar:4.10 in the shaded jar.
	[INFO] Including org.hamcrest:hamcrest-core:jar:1.1 in the shaded jar.
	[INFO] Including org.apache.hbase:hbase:jar:0.94.1 in the shaded jar.
	.
	.
	.
	[WARNING] We have a duplicate org/hamcrest/BaseDescription.class in /Users/ryantabora/.m2/repository/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar
	[WARNING] We have a duplicate org/hamcrest/BaseMatcher.class in /Users/ryantabora/.m2/repository/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar
	[WARNING] We have a duplicate org/hamcrest/core/AllOf.class in /Users/ryantabora/.m2/repository/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar
	[WARNING] We have a duplicate org/hamcrest/core/AnyOf.class in /Users/ryantabora/.
	.
	.
	.
	[INFO] Replacing original artifact with shaded artifact.
	[INFO] Replacing /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/target/real-time-stocks-indexer-1.0-SNAPSHOT.jar with /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/target/real-time-stocks-indexer-1.0-SNAPSHOT-shaded.jar
	[INFO] Dependency-reduced POM written at: /Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/07-real-time-demo/stocks-indexer/dependency-reduced-pom.xml
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 18.925s
	[INFO] Finished at: Fri Jan 25 15:39:53 PST 2013
	[INFO] Final Memory: 17M/81M
	[INFO] ------------------------------------------------------------------------

### Loading HBase with Sample Data



### Indexing Solr

The Web Application
-------

### Running the Query Application

