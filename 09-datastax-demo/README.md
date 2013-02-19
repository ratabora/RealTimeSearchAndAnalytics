Getting Starting with DSE 
-------
DSE Search/Solr is supported on Linux and Mac. You can run Solr on one or more nodes, assuming you installed DataStax Enterprise 2.0 or higher. DataStax does not support running Solr and Hadoop on the same node, although it's possible to do so in a development environment. In production environments, run Solr and Hadoop on separate nodes.

Starting DSE and DSE Search
-------
Follow these steps to start DSE Search on a single node.

### Start DSE as a Solr node.
    
Make the bin directory in the DSE installation directory, the current directory and run the dse cassandra command using the -s option.

    cd <install_location>/bin
    sudo ./dse cassandra -s

The -s option starts the Solr container inside DSE and marks the server as a search node.

Note DataStax does not support using the -s and -t search and trackers options to mark the node for search and Hadoop analytics.
In another shell, check that your Cassandra ring is up and running.

    cd <install_location>/bin
    ./dsetool ring -h localhost

A table of information appears showing the state of the node and identifying it as a Solr node.

Now, set up and run the DSE search demo.

### Running the DSE Search Demo

After starting DSE as a Solr node, open a shell window or tab, and follow these steps to run the demo.

Make the wikipedia demo directory your current directory.

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

    ./2-index.sh --wikifile enwiki-20111007-pages-articles25.xml-p023725001p026625000.bz2 --limit 10000

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