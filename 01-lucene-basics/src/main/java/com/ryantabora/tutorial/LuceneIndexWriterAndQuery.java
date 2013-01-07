package com.ryantabora.tutorial;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndexWriterAndQuery {
  public static void main(String[] args) throws IOException, ParseException {
    
    // We instantiate a directory to hold the index. Here, its in RAM.
    Directory dir = new RAMDirectory();
    
    // We instantiate a built in analyzer that tokenizes on whitespace.
    Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_40);
    
    // We specify that we want to use the Whitespace Analyzer when indexing.
    IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, analyzer);
    
    // We instantiate the index writer that will actually write the index using
    // the directory and analyzer specified.
    IndexWriter writer = new IndexWriter(dir, conf);
    
    // Sample Data
    String[] text = new String[] {
        "The lemon (Citrus × limon) is a small evergreen tree native to Asia",
        "and the tree's ellipsoidal yellow fruit.",
        "The fruit's juice, pulp and peel",
        "especially the zest, are used as foods",
        "The juice of the lemon is about 5% to 6% citric acid",
        "which gives lemons a sour taste",
        "The distinctive sour taste of lemon juice makes it",
        "a key ingredient in drinks and foods such as lemonade."};
    
    // Each string in the sample data is going to be a document. The ID is a
    // non-tokenized String defined by the position in the sample data array and
    // then the String value itself is indexed as a TextField called text.
    for (int x = 0; x < text.length; x++) {
      Document doc = new Document();
      doc.add(new StringField("id", x + "", Field.Store.YES));
      doc.add(new TextField("text", text[x], Store.YES));
      writer.addDocument(doc);
    }
    // At this point we have successfully created the index.
    writer.close();
    
    // Here we define a very simple query.
    // Try replacing this with different terms
    // For example
    // "pulP and Peel"
    // "foods"
    String queryS = "";
    if (args.length == 0) {
      queryS = "lemon";
    } else {
      queryS = args[0];
    }
    
    // We use the same analyzer we did on the index.
    Query query = new QueryParser(Version.LUCENE_40, "text", analyzer)
        .parse(queryS);
    
    // We instantiate a Index Reader and Searcher so we can execute the query.
    IndexReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);
    
    // The max number of documents in the response
    int limit = 10;
    
    // We create a collector to gather the results. Collectors are primarily
    // meant to be used to gather raw results from a search, and implement
    // sorting or custom result filtering, collation, etc.
    
    // The TopScoreDocCollector sorts results according to score +
    // document ID. It is likely the most frequently used collector.
    TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);
    
    // Here we actually execute the query.
    searcher.search(query, collector);
    
    // We get the number of results for the query.
    ScoreDoc[] results = collector.topDocs().scoreDocs;
    
    // Printing out the results.
    System.out.println("=======================");
    for (int i = 0; i < results.length; ++i) {
      int documentID = results[i].doc;
      Document document = searcher.doc(documentID);
      System.out.println("====== Result " + (i + 1) + " =======");
      System.out.println("id: " + document.get("id"));
      System.out.println("text: " + document.get("text"));
      System.out.println("=======================");
    }
    System.out.println("=======================");
    reader.close();
  }
}