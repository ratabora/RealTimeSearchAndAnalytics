#!/bin/sh

# options parser sets the following variables:
# HOST - server hostname
# SCHEME  - (http|https)
# CERT_FILE - client certificate file
# AUTH_OPTS - Additional security options for curl
. ./set-solr-options.sh $*

CF=demo.stocks

SOLRCONFIG_URL="$SCHEME://$HOST:8983/solr/resource/$CF/solrconfig.xml"
SOLRCONFIG=solrconfig.xml

curl -s $AUTH_OPTS $CERT_FILE --data-binary @$SOLRCONFIG -H 'Content-type:text/xml; charset=utf-8' $SOLRCONFIG_URL
echo "Posted $SOLRCONFIG to $SOLRCONFIG_URL"

SCHEMA_URL="$SCHEME://$HOST:8983/solr/resource/$CF/schema.xml"
SCHEMA=schema.xml

curl -s $AUTH_OPTS $CERT_FILE --data-binary @$SCHEMA -H 'Content-type:text/xml; charset=utf-8' $SCHEMA_URL

echo "Posted $SCHEMA to $SCHEMA_URL"

# CREATE_URL="$SCHEME://$HOST:8983/solr/admin/cores?action=CREATE&name=$CF" 

CREATE_URL="$SCHEME://$HOST:8983/solr/admin/cores?action=RELOAD&name=$CF" 

curl -s $AUTH_OPTS $CERT_FILE  -X POST $CREATE_URL 

echo "Created index."
