Getting Starting with DSE Search
DSE Search/Solr is supported on Linux and Mac. You can run Solr on one or more nodes, assuming you installed DataStax Enterprise 2.0 or higher. DataStax does not support running Solr and Hadoop on the same node, although it's possible to do so in a development environment. In production environments, run Solr and Hadoop on separate nodes.

Starting DSE and DSE Search

Follow these steps to start DSE Search on a single node.

Start DSE as a Solr node. The method you use depends on your platform:

RPM-Redhat or Debian installations

Edit /etc/default/dse, set SOLR_ENABLED=1, and run this command:

/etc/init.d/dse service start
Note DataStax does not support using the SOLR_ENABLED and HADOOP_ENABLED options to mark the same node for both search and Hadoop analytics.
Tar distribution, such as Mac

Make the bin directory in the DSE installation directory, the current directory and run the dse cassandra command using the -s option.

cd <install_location>/bin
sudo ./dse cassandra -s
The -s option starts the Solr container inside DSE and marks the server as a search node.

Note DataStax does not support using the -s and -t search and trackers options to mark the node for search and Hadoop analytics.
In another shell, check that your Cassandra ring is up and running. For example, on a Mac:

RPM-Redhat or Debian installations

dsetool ring -h localhost
Tar distribution

cd <install_location>/bin

./dsetool ring -h localhost
A table of information appears showing the state of the node and identifying it as a Solr node.

Now, set up and run the DSE search demo.

Running the DSE Search Demo

After starting DSE as a Solr node, open a shell window or tab, and follow these steps to run the demo.

Make the wikipedia demo directory your current directory. The location of the demo directory depends on your platform:

RPM-Redhat or Debian installations

cd  /usr/share/dse-demos/wikipedia
Tar distribution

cd <install_location>/demos/wikipedia
Add the schema:

./1-add-schema.sh
The script posts solrconfig.xml and schema.xml to these locations:

http://localhost:8983/solr/resource/wiki.solr/solrconfig.xml

http://localhost:8983/solr/resource/wiki.solr/schema.xml

wiki.solr in the URL represents the keyspace (wiki) and the column family (solr).

Index the articles contained in the wikipedia-sample.bz2 file in the demo directory:

./2-index.sh --wikifile wikipedia-sample.bz2
Three thousand articles load.

If you want to download all the Wikipedia articles from the internet, start indexing articles using the following wikifile option instead of wikipedia-sample.bz2:

./2-index.sh --wikifile
  enwiki-20111007-pages-articles25.xml-p023725001p026625000.bz2 --limit 10000
The first 10k articles load. To load all the articles, use the --limit option.

To see a sample Wikipedia search UI, open your web browser and go to:

http://localhost:8983/demos/wikipedia

Inspect the index keyspace, wiki, using the Solr Admin tool:

http://localhost:8983/solr/wiki.solr/admin/
Be sure to enter the trailing "/".


Inspect the column family, solr. In the Solr Admin tool, click SCHEMA to inspect the schema.

Using DataStax Enterprise and DSE Search, you can now:

Run Hadoop MapReduce on the data through DSE Analytics.
Update an individual column under a row in Cassandra and find the updated data in search results.
Take advantage of Solr searching to query Cassandra using CQL.

Creating a Schema
A Solr schema defines the relationship between data in a column family and a Solr core. The schema identifies the columns to index in Solr and maps column names to Solr types. This document describes the Solr schema at a high level. For details about all the options and Solr schema settings, see the Solr wiki.

Wikipedia Sample Schema Elements

The sample schema.xml for the Wikipedia demo represents a typical schema. It specifies a tokenizer that determines the parsing of the wiki text. The set of fields specifies what Solr indexes and stores. In this example, these name, body, title, and date fields are indexed.

<schema name="wikipedia" version="1.1">
 <types>
  <fieldType name="string" class="solr.StrField"/>
  <fieldType name="text" class="solr.TextField">
    <analyzer><tokenizer class="solr.WikipediaTokenizerFactory"/></analyzer>
  </fieldType>
 </types>
 <fields>
    <field name="id"  type="string" indexed="true"  stored="true"/>
    <field name="body"  type="text" indexed="true"  stored="true"/>
    <field name="date"  type="string" indexed="true"  stored="true"/>
    <field name="name"  type="text" indexed="true"  stored="true"/>
    <field name="title"  type="text" indexed="true"  stored="true"/>
 </fields>
 <defaultSearchField>body</defaultSearchField>
 <uniqueKey>id</uniqueKey>
The example schema.xml meets the requirement to have a unique key and no duplicate rows. The unique key maps to the row key and is necessary for DSE to route documents to cluster nodes. This unique key is like a primary key in SQL. The last element in the schema.xml example designates that the unique key is id.

Checking a Schema

After creating a schema and indexing documents, you can check that the Solr index is working by using the Solr Admin tool in this location:

http://hostname/solr/{keyspace}.{columnfamily}/admin/
If the tool appears, the index is working. The tool looks something like this:


Wikipedia Sample Column Family Metadata

After indexing the Wikipedia articles, Cassandra columns in the column family contain metadata corresponding to the fields listed in the demo schema. The output of the CLI command, DESCRIBE wiki, shows this metadata:

 Column Name: body
   Validation Class: org.apache.cassandra.db.marshal.UTF8Type
   Index Name: wiki_solr_body_index
   Index Type: CUSTOM
   Index Options: {class_name=com.datastax.bdp.cassandra.index.solr.SolrSecondaryIndex}
 Column Name: date
   Validation Class: org.apache.cassandra.db.marshal.UTF8Type
   Index Name: wiki_solr_date_index
   Index Type: CUSTOM
   Index Options: {class_name=com.datastax.bdp.cassandra.index.solr.SolrSecondaryIndex}
 Column Name: name
   Validation Class: org.apache.cassandra.db.marshal.UTF8Type
   Index Name: wiki_solr_name_index
   Index Type: CUSTOM
   Index Options: {class_name=com.datastax.bdp.cassandra.index.solr.SolrSecondaryIndex}
 Column Name: solr_query
   Validation Class: org.apache.cassandra.db.marshal.UTF8Type
   Index Name: wiki_solr_solr_query_index
   Index Type: CUSTOM
   Index Options: {class_name=com.datastax.bdp.cassandra.index.solr.SolrSecondaryIndex}
 Column Name: title
   Validation Class: org.apache.cassandra.db.marshal.UTF8Type
   Index Name: wiki_solr_title_index
   Index Type: CUSTOM
   Index Options: {class_name=com.datastax.bdp.cassandra.index.solr.SolrSecondaryIndex}
Compaction Strategy: org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy
Column metadata matches each field in the schema except the id field because id is the unique key.

The column metadata example shows some of the Cassandra Validator types in the Validation Class attribute. The Solr types map to Cassandra validator types as shown in this table:

Solr Type	Cassandra Validator
TextField	UTF8Type
StrField	UTF8Type
LongField	LongType
IntField	Int32Type
FloatField	FloatType
DoubleField	DoubleType
DateField	UTF8Type
ByteField	BytesType
BinaryField	BytesType
BoolField	UTF8Type
UUIDField	UUIDType
All Others	UTF8Type
Using Dynamic Fields instead of Composite Columns

You can use Solr dynamic fields for pattern matching on a wildcard instead of using composite columns, which are not supported. The number of dynamic fields allowed for a particular row is 1024. Adding the following element to the schema will index anything with the column name that ends with -tag.

<dynamicField name="*-tag" type="string" indexed="true"/>
When you use the dynamicField element, DSE Search adds a special solr field, _dynFld, to the index, so you can search for rows that have columns X, Y and Z.

To learn more about the Solr schema, see the well-documented sample Solr schema file.