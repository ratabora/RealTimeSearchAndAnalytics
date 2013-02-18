Real Time Search and Analytics on Big Data - Solr Cloud
=============

Introduction
-------
This exercise will guide you through setting up Solr Cloud. This exercise is intended to be run on the student's local environment.

Prerequisites
-------
* Linux based environment or Mac OS X. For Windows users you can use Cygwin.

Setting Up Pseudo Distributed Solr Cloud
-------

A PseudoDistributed SorlCloud cluster mimics the behavior of a multi-node SolrCloud cluster on a single node. This is a great way to test SolrCloud features with minimal effort. It is not recommended to use this type of setup in any type of production environment since any distributed fail safes are lost when the 'cluster' is working off of a single node.

### Creating Your First Shards

We will use the same Solr download that we used in Exercise 3. Navigate to that directory.

	$ cd RealTimeSearchAndAnalytics/03-installing-solr/apache-solr-4.0.0/
	$ ls
	CHANGES.txt	NOTICE.txt	contrib		docs		licenses
	LICENSE.txt	README.txt	dist		example		testing-example

Remember, the example directory includes a fully functional Solr configuration we can use for testing. Let's create a few copies for each of our shards. Each of these directories will host one of the nodes in our pseudo "cluster". Remove any data indexed if there is any.

$ cp -r example shard1core1
$ cp -r example shard2core1
$ ls
CHANGES.txt	NOTICE.txt	contrib		docs		licenses	shard2core1
LICENSE.txt	README.txt	dist		example		shard1core1	testing-example
$ rm -rf shard1core1/solr/collection1/data
$ rm -rf shard2core1/solr/collection1/data

We can now start the first shard of our collection. This instance will run an embedded Zookeeper process that the pseudo-distributed cluster will use for coordination and configuration. As the shard comes online, Solr will pick up the configuration files from the shard/solr/conf directory and upload them to the embedded Zookeeper process. We will name this configuration exampleConf so that when we start the other shards we can reuse the same configuration. We will also set the number of shards we intend to split this collection into. This parameter is very important because as of the current version of Apache Solr, you cannot change this number without reindexing your entire collection.

	$ cd shard1core1/
	$ java -Dbootstrap_confdir=./solr/collection1/conf -Dcollection.configName=exampleConf -DzkRun -DnumShards=2 -jar start.jar

After running the jar you will see many log entries in the console which describe each step of the Solr startup process. It should look something like the following log snippet. You can continue to monitor the Solr shard through this terminal.

	INFO: Updating cloud state from ZooKeeper... 
	Feb 18, 2013 11:31:16 AM org.apache.solr.common.cloud.ZkStateReader$2 process
	INFO: A cluster state change has occurred - updating...
	Feb 18, 2013 11:31:16 AM org.apache.solr.cloud.ZkController register
	INFO: We are http://Ryan-Taboras-MacBook-Air.local:8983/solr/collection1/ and leader is http://Ryan-Taboras-MacBook-Air.local:8983/solr/collection1/
	Feb 18, 2013 11:31:16 AM org.apache.solr.cloud.ZkController register
	INFO: No LogReplay needed for core=collection1 baseURL=http://Ryan-Taboras-MacBook-Air.local:8983/solr
	Feb 18, 2013 11:31:16 AM org.apache.solr.cloud.ZkController checkRecovery
	INFO: I am the leader, no recovery necessary
	Feb 18, 2013 11:31:16 AM org.apache.solr.common.cloud.ZkStateReader updateClusterState
	INFO: Updating cloud state from ZooKeeper... 
	Feb 18, 2013 11:31:16 AM org.apache.solr.servlet.SolrDispatchFilter init
	INFO: user.dir=/Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/03-installing-solr/apache-solr-4.0.0/shard1core1
	Feb 18, 2013 11:31:16 AM org.apache.solr.servlet.SolrDispatchFilter init
	INFO: SolrDispatchFilter.init() done
	2013-02-18 11:31:16.112:INFO:oejs.AbstractConnector:Started SocketConnector@0.0.0.0:8983
	Feb 18, 2013 11:31:16 AM org.apache.solr.common.cloud.ZkStateReader updateClusterState
	INFO: Updating cloud state from ZooKeeper... 
	Feb 18, 2013 11:31:16 AM org.apache.solr.common.cloud.ZkStateReader$2 process
	INFO: A cluster state change has occurred - updating...

Now that the first shard is running successfully, we can view the status of the collection via the Solr Web Administration UI.

http://localhost:8983/solr/#/~cloud

We can see in the graph that we have a single collection with two shards. We can also see that there is a second shard specified but not running at this point. 

Let's start the second shard on a different port on our machine. Open up a new terminal and change directories to the second shard. By specifying an already running ZooKeeper host, Solr will recognize that there is already another shard running. Zookeeper is currently running as an embedded process within the first shard (By default, an embedded Zookeeper server runs at the Solr port plus 1000, so in this case it is running at 9983) Here is the command to start the second shard.

	$ cd ../shard2core1/
	$ java -Djetty.port=7574 -DzkHost=localhost:9983 -jar start.jar

The log output should look similar to the logs coming from the first shard startup. 

	INFO: A cluster state change has occurred - updating...
	Feb 18, 2013 11:41:19 AM org.apache.solr.core.CoreContainer register
	INFO: registering core: collection1
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.ZkController register
	INFO: Register shard - core:collection1 address:http://Ryan-Taboras-MacBook-Air.local:7574/solr shardId:shard2
	Feb 18, 2013 11:41:19 AM org.apache.solr.client.solrj.impl.HttpClientUtil createClient
	INFO: Creating new http client, config:maxConnections=10000&maxConnectionsPerHost=20&connTimeout=30000&socketTimeout=30000&retry=false
	Feb 18, 2013 11:41:19 AM org.apache.solr.common.cloud.SolrZkClient makePath
	INFO: makePath: /collections/collection1/leader_elect/shard2/election
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.ShardLeaderElectionContext runLeaderProcess
	INFO: Running the leader process.
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.ShardLeaderElectionContext waitForReplicasToComeUp
	INFO: Enough replicas found to continue.
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.ShardLeaderElectionContext runLeaderProcess
	INFO: I may be the new leader - try and sync
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.SyncStrategy sync
	INFO: Sync replicas to http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.SyncStrategy syncReplicas
	INFO: Sync Success - now sync replicas to me
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.SyncStrategy syncToMe
	INFO: http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/ has no replicas
	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.ShardLeaderElectionContext runLeaderProcess
	INFO: I am the new leader: http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/
	Feb 18, 2013 11:41:19 AM org.apache.solr.common.cloud.SolrZkClient makePath
	INFO: makePath: /collections/collection1/leaders/shard2
	Feb 18, 2013 11:41:20 AM org.apache.solr.common.cloud.ZkStateReader$2 process
	INFO: A cluster state change has occurred - updating...
	Feb 18, 2013 11:41:20 AM org.apache.solr.cloud.ZkController register
	INFO: We are http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/ and leader is http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/
	Feb 18, 2013 11:41:20 AM org.apache.solr.cloud.ZkController register
	INFO: No LogReplay needed for core=collection1 baseURL=http://Ryan-Taboras-MacBook-Air.local:7574/solr
	Feb 18, 2013 11:41:20 AM org.apache.solr.cloud.ZkController checkRecovery
	INFO: I am the leader, no recovery necessary
	Feb 18, 2013 11:41:20 AM org.apache.solr.common.cloud.ZkStateReader updateClusterState
	INFO: Updating cloud state from ZooKeeper... 
	Feb 18, 2013 11:41:20 AM org.apache.solr.servlet.SolrDispatchFilter init
	INFO: user.dir=/Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/03-installing-solr/apache-solr-4.0.0/shard2core1
	Feb 18, 2013 11:41:20 AM org.apache.solr.servlet.SolrDispatchFilter init
	INFO: SolrDispatchFilter.init() done
	2013-02-18 11:41:20.079:INFO:oejs.AbstractConnector:Started SocketConnector@0.0.0.0:7574
	Feb 18, 2013 11:41:20 AM org.apache.solr.common.cloud.ZkStateReader$2 process
	INFO: A cluster state change has occurred - updating...

Looking through the logs you can see the steps the core takes while it is starting up. First it registers the core as the second shard of collection1:

		INFO: Register shard - core:collection1 address:http://Ryan-Taboras-MacBook-Air.local:7574/solr shardId:shard2

Then you can see it go through the Leader election process to determine that it is a Leader core:

	Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.SyncStrategy syncToMe
		INFO: http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/ has no replicas
		Feb 18, 2013 11:41:19 AM org.apache.solr.cloud.ShardLeaderElectionContext runLeaderProcess
		INFO: I am the new leader: http://Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/

Finally, it finishes by updating Zookeeper with its status.

	Feb 18, 2013 11:41:20 AM org.apache.solr.common.cloud.ZkStateReader updateClusterState
		INFO: Updating cloud state from ZooKeeper... 
		Feb 18, 2013 11:41:20 AM org.apache.solr.servlet.SolrDispatchFilter init
		INFO: user.dir=/Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/03-installing-solr/apache-solr-4.0.0/shard2core1
		Feb 18, 2013 11:41:20 AM org.apache.solr.servlet.SolrDispatchFilter init
		INFO: SolrDispatchFilter.init() done
		2013-02-18 11:41:20.079:INFO:oejs.AbstractConnector:Started SocketConnector@0.0.0.0:7574

If we check the Web UI again we can see the new shard is included in our collection. We can also see that shard1 is located at host:8983 and shard2 is located at host:7574. You should also note that you can view the Solr Web UI through the new shard as well and see the same information 

http://localhost:7574/solr/#/~cloud.

### Querying Documents in the SolrCloud

Now that we have both shards up we can start indexing documents to the SolrCloud. First navigate to the exampledocs directory of the first shard and execute the post.jar command.

	$ cd ../shard1core1/exampledocs/
	$ ls
	books.csv		hd.xml			manufacturers.xml	monitor.xml		post.jar		solr.xml		vidcard.xml
	books.json		ipod_other.xml		mem.xml			monitor2.xml		post.sh			test_utf8.sh
	gb18030-example.xml	ipod_video.xml		money.xml		mp500.xml		sd500.xml		utf8-example.xml
	$ java -jar post.jar *.xml
	SimplePostTool version 1.5
	Posting files to base url http://localhost:8983/solr/update using content-type application/xml..
	POSTing file gb18030-example.xml
	POSTing file hd.xml
	POSTing file ipod_other.xml
	POSTing file ipod_video.xml
	POSTing file manufacturers.xml
	POSTing file mem.xml
	POSTing file money.xml
	POSTing file monitor.xml
	POSTing file monitor2.xml
	POSTing file mp500.xml
	POSTing file sd500.xml
	POSTing file solr.xml
	POSTing file utf8-example.xml
	POSTing file vidcard.xml
	14 files indexed.
	COMMITting Solr index changes to http://localhost:8983/solr/update..

From reviewing the logs of your first terminal screen you can see that the documents were all indexed on the first shard (port 8983). By sending a query to the first shard, we can see there are 32 results.

http://localhost:8983/solr/collection1/select?q=*:*

	<response>
		<lst name="responseHeader">
			<int name="status">0</int>
			<int name="QTime">40</int>
			<lst name="params">
				<str name="q">*:*</str>
			</lst>
		</lst>
		<result name="response" numFound="32" start="0" maxScore="1.0">
			<doc>
				<str name="id">SP2514N</str>
				<str name="name">
					Samsung SpinPoint P120 SP2514N - hard drive - 250 GB - ATA-133
				</str>
				<str name="manu">Samsung Electronics Co. Ltd.</str>
				<str name="manu_id_s">samsung</str>
				<arr name="cat">
					<str>electronics</str>
					<str>hard drive</str>
				</arr>
				<arr name="features">
					<str>7200RPM, 8MB cache, IDE Ultra ATA-133</str>
					<str>
						NoiseGuard, SilentSeek technology, Fluid Dynamic Bearing (FDB) motor
					</str>
				</arr>
				<float name="price">92.0</float>
				<str name="price_c">92,USD</str>
				<int name="popularity">6</int>
				<bool name="inStock">true</bool>
				<date name="manufacturedate_dt">2006-02-13T15:26:37Z</date>
				<str name="store">35.0752,-97.032</str>
				<long name="_version_">1427339449902039040</long>
			</doc>
			<doc>
				<str name="id">6H500F0</str>
				<str name="name">
					Maxtor DiamondMax 11 - hard drive - 500 GB - SATA-300
				</str>
				<str name="manu">Maxtor Corp.</str>
				<str name="manu_id_s">maxtor</str>
				<arr name="cat">
					<str>electronics</str>
					<str>hard drive</str>
				</arr>
				<arr name="features">
					<str>SATA 3.0Gb/s, NCQ</str>
					<str>8.5ms seek</str>
					<str>16MB cache</str>
				</arr>
				<float name="price">350.0</float>
				<str name="price_c">350,USD</str>
				<int name="popularity">6</int>
				<bool name="inStock">true</bool>
				<str name="store">45.17614,-93.87341</str>
				<date name="manufacturedate_dt">2006-02-13T15:26:37Z</date>
				<long name="_version_">1427339449949224960</long>
			</doc>
			<doc>
				<str name="id">F8V7067-APL-KIT</str>
				<str name="name">Belkin Mobile Power Cord for iPod w/ Dock</str>
				<str name="manu">Belkin</str>
				<str name="manu_id_s">belkin</str>
				<arr name="cat">
					<str>electronics</str>
					<str>connector</str>
				</arr>
				<arr name="features">
					<str>car power adapter, white</str>
				</arr>
				<float name="weight">4.0</float>
				<float name="price">19.95</float>
				<str name="price_c">19.95,USD</str>
				<int name="popularity">1</int>
				<bool name="inStock">false</bool>
				<str name="store">45.18014,-93.87741</str>
				<date name="manufacturedate_dt">2005-08-01T16:30:25Z</date>
				<long name="_version_">1427339449982779392</long>
			</doc>
			<doc>
				<str name="id">apple</str>
				<str name="compName_s">Apple</str>
				<str name="address_s">1 Infinite Way, Cupertino CA</str>
				<long name="_version_">1427339450007945216</long>
			</doc>
			<doc>
				<str name="id">ati</str>
				<str name="compName_s">ATI Technologies</str>
				<str name="address_s">
					33 Commerce Valley Drive East Thornhill, ON L3T 7N6 Canada
				</str>
				<long name="_version_">1427339450008993792</long>
			</doc>
			<doc>
				<str name="id">canon</str>
				<str name="compName_s">Canon, Inc.</str>
				<str name="address_s">One Canon Plaza Lake Success, NY 11042</str>
				<long name="_version_">1427339450010042368</long>
			</doc>
			<doc>
				<str name="id">corsair</str>
				<str name="compName_s">Corsair Microsystems</str>
				<str name="address_s">46221 Landing Parkway Fremont, CA 94538</str>
				<long name="_version_">1427339450011090944</long>
			</doc>
			<doc>
				<str name="id">dell</str>
				<str name="compName_s">Dell, Inc.</str>
				<str name="address_s">One Dell Way Round Rock, Texas 78682</str>
				<long name="_version_">1427339450011090945</long>
			</doc>
			<doc>
				<str name="id">samsung</str>
				<str name="compName_s">Samsung Electronics Co. Ltd.</str>
				<str name="address_s">105 Challenger Rd. Ridgefield Park, NJ 07660-0511</str>
				<long name="_version_">1427339450012139520</long>
			</doc>
			<doc>
				<str name="id">viewsonic</str>
				<str name="compName_s">ViewSonic Corp</str>
				<str name="address_s">381 Brea Canyon Road Walnut, CA 91789-0708</str>
				<long name="_version_">1427339450013188096</long>
			</doc>
		</result>
	</response>

If we make the same query on the second shard, you can see that the results are the same.

http://localhost:7574/solr/collection1/select?q=*:*

	<response>
		<lst name="responseHeader">
			<int name="status">0</int>
			<int name="QTime">118</int>
			<lst name="params">
				<str name="q">*:*</str>
			</lst>
		</lst>
		<result name="response" numFound="32" start="0" maxScore="1.0">
			<doc>
				<str name="id">SP2514N</str>
				<str name="name">
					Samsung SpinPoint P120 SP2514N - hard drive - 250 GB - ATA-133
				</str>
				<str name="manu">Samsung Electronics Co. Ltd.</str>
				<str name="manu_id_s">samsung</str>
				<arr name="cat">
					<str>electronics</str>
					<str>hard drive</str>
				</arr>
				<arr name="features">
					<str>7200RPM, 8MB cache, IDE Ultra ATA-133</str>
					<str>
						NoiseGuard, SilentSeek technology, Fluid Dynamic Bearing (FDB) motor
					</str>
				</arr>
				<float name="price">92.0</float>
				<str name="price_c">92,USD</str>
				<int name="popularity">6</int>
				<bool name="inStock">true</bool>
				<date name="manufacturedate_dt">2006-02-13T15:26:37Z</date>
				<str name="store">35.0752,-97.032</str>
				<long name="_version_">1427339449902039040</long>
			</doc>
			<doc>
				<str name="id">6H500F0</str>
				<str name="name">
					Maxtor DiamondMax 11 - hard drive - 500 GB - SATA-300
				</str>
				<str name="manu">Maxtor Corp.</str>
				<str name="manu_id_s">maxtor</str>
				<arr name="cat">
					<str>electronics</str>
					<str>hard drive</str>
				</arr>
				<arr name="features">
					<str>SATA 3.0Gb/s, NCQ</str>
					<str>8.5ms seek</str>
					<str>16MB cache</str>
				</arr>
				<float name="price">350.0</float>
				<str name="price_c">350,USD</str>
				<int name="popularity">6</int>
				<bool name="inStock">true</bool>
				<str name="store">45.17614,-93.87341</str>
				<date name="manufacturedate_dt">2006-02-13T15:26:37Z</date>
				<long name="_version_">1427339449949224960</long>
			</doc>
			<doc>
				<str name="id">F8V7067-APL-KIT</str>
				<str name="name">Belkin Mobile Power Cord for iPod w/ Dock</str>
				<str name="manu">Belkin</str>
				<str name="manu_id_s">belkin</str>
				<arr name="cat">
					<str>electronics</str>
					<str>connector</str>
				</arr>
				<arr name="features">
					<str>car power adapter, white</str>
				</arr>
				<float name="weight">4.0</float>
				<float name="price">19.95</float>
				<str name="price_c">19.95,USD</str>
				<int name="popularity">1</int>
				<bool name="inStock">false</bool>
				<str name="store">45.18014,-93.87741</str>
				<date name="manufacturedate_dt">2005-08-01T16:30:25Z</date>
				<long name="_version_">1427339449982779392</long>
			</doc>
			<doc>
				<str name="id">apple</str>
				<str name="compName_s">Apple</str>
				<str name="address_s">1 Infinite Way, Cupertino CA</str>
				<long name="_version_">1427339450007945216</long>
			</doc>
			<doc>
				<str name="id">ati</str>
				<str name="compName_s">ATI Technologies</str>
				<str name="address_s">
					33 Commerce Valley Drive East Thornhill, ON L3T 7N6 Canada
				</str>
				<long name="_version_">1427339450008993792</long>
			</doc>
			<doc>
				<str name="id">canon</str>
				<str name="compName_s">Canon, Inc.</str>
				<str name="address_s">One Canon Plaza Lake Success, NY 11042</str>
				<long name="_version_">1427339450010042368</long>
			</doc>
			<doc>
				<str name="id">corsair</str>
				<str name="compName_s">Corsair Microsystems</str>
				<str name="address_s">46221 Landing Parkway Fremont, CA 94538</str>
				<long name="_version_">1427339450011090944</long>
			</doc>
			<doc>
				<str name="id">dell</str>
				<str name="compName_s">Dell, Inc.</str>
				<str name="address_s">One Dell Way Round Rock, Texas 78682</str>
				<long name="_version_">1427339450011090945</long>
			</doc>
			<doc>
				<str name="id">samsung</str>
				<str name="compName_s">Samsung Electronics Co. Ltd.</str>
				<str name="address_s">105 Challenger Rd. Ridgefield Park, NJ 07660-0511</str>
				<long name="_version_">1427339450012139520</long>
			</doc>
			<doc>
				<str name="id">viewsonic</str>
				<str name="compName_s">ViewSonic Corp</str>
				<str name="address_s">381 Brea Canyon Road Walnut, CA 91789-0708</str>
				<long name="_version_">1427339450013188096</long>
			</doc>
		</result>
	</response>

At this point, if we lost one of our shards we would still lose all of the data that was indexed on that shard. To help prevent this we can use replicas to serve as hot failovers for our shards.

### Starting Replica Cores

To create a replica core we simply make copies of the shard1core1 and shard2core1 directories. These copied directories will hold all of the data for our new Replica cores.

	$ cd ../../
	$ ls
	CHANGES.txt	LICENSE.txt	NOTICE.txt	README.txt	contrib		dist		docs		example		licenses	shard1core1	shard2core1	testing-example
	$ cp -r shard1core1 shard1core2
	$ cp -r shard2core1 shard2core2
	$ ls
	CHANGES.txt	LICENSE.txt	NOTICE.txt	README.txt	contrib		dist		docs		example		licenses	shard1core1	shard1core2	shard2core1	shard2core2	testing-example

Now start each of these cores in a new terminal window.

	$ cd shard1core2
	$ java -Djetty.port=8900 -DzkHost=localhost:9983 -jar start.jar

You will see logs similar to the following:

	INFO: A cluster state change has occurred - updating...
	Feb 18, 2013 2:00:40 PM org.apache.solr.update.PeerSync handleVersions
	INFO: PeerSync: core=collection1 url=http://Ryan-Taboras-MacBook-Air.local:8900/solr  Received 14 versions from Ryan-Taboras-MacBook-Air.local:8983/solr/collection1/
	Feb 18, 2013 2:00:40 PM org.apache.solr.update.PeerSync handleVersions
	INFO: PeerSync: core=collection1 url=http://Ryan-Taboras-MacBook-Air.local:8900/solr  Our versions are newer. ourLowThreshold=1427339449990119424 otherHigh=1427339450035208192
	Feb 18, 2013 2:00:40 PM org.apache.solr.update.PeerSync sync
	INFO: PeerSync: core=collection1 url=http://Ryan-Taboras-MacBook-Air.local:8900/solr DONE. sync succeeded
	Feb 18, 2013 2:00:40 PM org.apache.solr.update.DirectUpdateHandler2 commit
	INFO: start commit{flags=0,_version_=0,optimize=false,openSearcher=true,waitSearcher=true,expungeDeletes=false,softCommit=false}
	Feb 18, 2013 2:00:40 PM org.apache.solr.core.SolrDeletionPolicy onInit
	INFO: SolrDeletionPolicy.onInit: commits:num=1
		commit{dir=NRTCachingDirectory(org.apache.lucene.store.NIOFSDirectory@/Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/03-installing-solr/apache-solr-4.0.0/shard1core2/solr/collection1/data/index lockFactory=org.apache.lucene.store.NativeFSLockFactory@381bd13; maxCacheMB=48.0 maxMergeSizeMB=4.0),segFN=segments_2,generation=2,filenames=[_0_Lucene40_0.tim, _0.fnm, _0.tvd, _0.tvf, _0_nrm.cfs, _0_Lucene40_0.prx, _0_Lucene40_0.tip, _0_Lucene40_0.frq, _0.tvx, _0_nrm.cfe, segments_2, _0.fdx, _0.si, _0.fdt]
	Feb 18, 2013 2:00:40 PM org.apache.solr.core.SolrDeletionPolicy updateCommits
	INFO: newest commit = 2
	Feb 18, 2013 2:00:40 PM org.apache.solr.search.SolrIndexSearcher <init>
	INFO: Opening Searcher@fe14de0 main
	Feb 18, 2013 2:00:40 PM org.apache.solr.update.DirectUpdateHandler2 commit
	INFO: end_commit_flush
	Feb 18, 2013 2:00:40 PM org.apache.solr.core.QuerySenderListener newSearcher
	INFO: QuerySenderListener sending requests to Searcher@fe14de0 main{StandardDirectoryReader(segments_2:3 _0(4.0.0.2):C14)}
	Feb 18, 2013 2:00:40 PM org.apache.solr.core.QuerySenderListener newSearcher
	INFO: QuerySenderListener done.
	Feb 18, 2013 2:00:40 PM org.apache.solr.core.SolrCore registerSearcher
	INFO: [collection1] Registered new searcher Searcher@fe14de0 main{StandardDirectoryReader(segments_2:3 _0(4.0.0.2):C14)}
	Feb 18, 2013 2:00:40 PM org.apache.solr.cloud.RecoveryStrategy doRecovery
	INFO: PeerSync Recovery was successful - registering as Active. core=collection1
	Feb 18, 2013 2:00:40 PM org.apache.solr.common.cloud.ZkStateReader$2 process
	INFO: A cluster state change has occurred - updating...

And in another terminal window

	$ cd shard2core2
	$ java -Djetty.port=7500 -DzkHost=localhost:9983 -jar start.jar

See the logs:

	INFO: A cluster state change has occurred - updating...
	Feb 18, 2013 2:02:22 PM org.apache.solr.update.PeerSync handleVersions
	INFO: PeerSync: core=collection1 url=http://Ryan-Taboras-MacBook-Air.local:7500/solr  Received 18 versions from Ryan-Taboras-MacBook-Air.local:7574/solr/collection1/
	Feb 18, 2013 2:02:22 PM org.apache.solr.update.PeerSync handleVersions
	INFO: PeerSync: core=collection1 url=http://Ryan-Taboras-MacBook-Air.local:7500/solr  Our versions are newer. ourLowThreshold=1427339450007945216 otherHigh=1427339450087636992
	Feb 18, 2013 2:02:22 PM org.apache.solr.update.PeerSync sync
	INFO: PeerSync: core=collection1 url=http://Ryan-Taboras-MacBook-Air.local:7500/solr DONE. sync succeeded
	Feb 18, 2013 2:02:22 PM org.apache.solr.update.DirectUpdateHandler2 commit
	INFO: start commit{flags=0,_version_=0,optimize=false,openSearcher=true,waitSearcher=true,expungeDeletes=false,softCommit=false}
	Feb 18, 2013 2:02:22 PM org.apache.solr.core.SolrDeletionPolicy onInit
	INFO: SolrDeletionPolicy.onInit: commits:num=1
		commit{dir=NRTCachingDirectory(org.apache.lucene.store.NIOFSDirectory@/Users/ryantabora/Code/ryantabora/RealTimeSearchAndAnalytics/03-installing-solr/apache-solr-4.0.0/shard2core2/solr/collection1/data/index lockFactory=org.apache.lucene.store.NativeFSLockFactory@5a10c276; maxCacheMB=48.0 maxMergeSizeMB=4.0),segFN=segments_2,generation=2,filenames=[_0_Lucene40_0.tim, _0.fnm, _0.tvd, _0.tvf, _0_nrm.cfs, _0_Lucene40_0.prx, _0_Lucene40_0.tip, _0_Lucene40_0.frq, _0.tvx, _0_nrm.cfe, segments_2, _0.fdx, _0.si, _0.fdt]
	Feb 18, 2013 2:02:22 PM org.apache.solr.core.SolrDeletionPolicy updateCommits
	INFO: newest commit = 2
	Feb 18, 2013 2:02:22 PM org.apache.solr.search.SolrIndexSearcher <init>
	INFO: Opening Searcher@2d2ce574 main
	Feb 18, 2013 2:02:22 PM org.apache.solr.update.DirectUpdateHandler2 commit
	INFO: end_commit_flush
	Feb 18, 2013 2:02:22 PM org.apache.solr.core.QuerySenderListener newSearcher
	INFO: QuerySenderListener sending requests to Searcher@2d2ce574 main{StandardDirectoryReader(segments_2:3 _0(4.0.0.2):C18)}
	Feb 18, 2013 2:02:22 PM org.apache.solr.core.QuerySenderListener newSearcher
	INFO: QuerySenderListener done.
	Feb 18, 2013 2:02:22 PM org.apache.solr.core.SolrCore registerSearcher
	INFO: [collection1] Registered new searcher Searcher@2d2ce574 main{StandardDirectoryReader(segments_2:3 _0(4.0.0.2):C18)}
	Feb 18, 2013 2:02:22 PM org.apache.solr.cloud.RecoveryStrategy doRecovery
	INFO: PeerSync Recovery was successful - registering as Active. core=collection1
	Feb 18, 2013 2:02:22 PM org.apache.solr.common.cloud.ZkStateReader$2 process
	INFO: A cluster state change has occurred - updating...

As you can see neither of the two new replica cores determine that they are Leader cores. By communicating with Zookeeper, these cores can determine that the Leader of their shard is already up and running.

We can now query any of these cores and view the same results.

http://localhost:8983/solr/collection1/select?q=id:6H500F0
http://localhost:8900/solr/collection1/select?q=id:6H500F0
http://localhost:7574/solr/collection1/select?q=id:6H500F0
http://localhost:7500/solr/collection1/select?q=id:6H500F0

	<response>
		<lst name="responseHeader">
			<int name="status">0</int>
			<int name="QTime">15</int>
			<lst name="params">
				<str name="q">id:6H500F0</str>
			</lst>
		</lst>
		<result name="response" numFound="1" start="0" maxScore="3.1972246">
			<doc>
				<str name="id">6H500F0</str>
				<str name="name">
					Maxtor DiamondMax 11 - hard drive - 500 GB - SATA-300
				</str>
				<str name="manu">Maxtor Corp.</str>
				<str name="manu_id_s">maxtor</str>
				<arr name="cat">
					<str>electronics</str>
					<str>hard drive</str>
				</arr>
				<arr name="features">
					<str>SATA 3.0Gb/s, NCQ</str>
					<str>8.5ms seek</str>
					<str>16MB cache</str>
				</arr>
				<float name="price">350.0</float>
				<str name="price_c">350,USD</str>
				<int name="popularity">6</int>
				<bool name="inStock">true</bool>
				<str name="store">45.17614,-93.87341</str>
				<date name="manufacturedate_dt">2006-02-13T15:26:37Z</date>
				<long name="_version_">1427339449949224960</long>
			</doc>
		</result>
	</response>

It is also interesting to view submit the same query to a single core over and over again. You will see Solr distribute the queries to the replica core if you ping a single server several times. Monitor a single core and query it several times.
