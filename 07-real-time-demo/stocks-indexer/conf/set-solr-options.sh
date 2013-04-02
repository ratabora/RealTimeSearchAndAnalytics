#!/bin/sh

###############################################################################
# Common options parsing for Solr demo scripts. Recognises a number of options
# related to DSE security settings. Kerberos authentication requires request
# URLs to use the correct fqdn for the host.
# Where `hostname -f` returns this correctly, use the -a option to enable
# Kerberos authentication. 
# Alternatively, use the -A option to specify the hostname on the command line
# To enable SSL encryption of HTTP requests, use the -e option and supply
# a path to a valid client certificate file (curl expects these in pem format)
# Use the -k option to disable certificate checking 
# Username and password for HTTP Basic auth can be supplied with the -u & -p 
# options
############################################################################### 

SCHEME="http"
HOST="localhost"
USERNAME="user"
PASSWORD="pass"
AUTH_OPTS=""

KRB_FLAG=0
CREDENTIALS_FLAG=0

while getopts ":ae:A:ku:p: " opt; do
  case $opt in
    a)
      HOST=`hostname -f`
      # Additional options for GSSAPI on secure DSE nodes. The user & password are required, but
      # ignored by Curl's authentication.
      AUTH_OPTS="--negotiate -b .cookiejar.txt -c .cookiejar.txt" 
      KRB_FLAG=`expr $HOST_FLAG + 1`
      ;;
    A)
      HOST=$OPTARG;
      # Additional options for GSSAPI on secure DSE nodes. The user & password are required, but
      # ignored by Curl's authentication.
      AUTH_OPTS="--negotiate -b .cookiejar.txt -c .cookiejar.txt" 
      KRB_FLAG=`expr $HOST_FLAG + 1`
      ;;
    e)
      SCHEME="https"
      CERT_FILE="$CERT_FILE --cacert $OPTARG"
      ;;
    k)
      CERT_FILE="-k $CERT_FILE"
      ;; 
    p)
      PASSWORD=$OPTARG
      CREDENTIALS_FLAG=`expr $CREDENTIALS_FLAG + 1`
      ;;
    u)
      USERNAME=$OPTARG
      CREDENTIALS_FLAG=`expr $CREDENTIALS_FLAG + 1`
      ;;
    :)
      "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
    \?)
      ## ignore unknown options as they may be specific to a particular demo
      ;;
  esac
done

if [ $CREDENTIALS_FLAG -gt 0 -a  $CREDENTIALS_FLAG -lt 2 ]
then
  echo "Please supply both a username and password" >&2
  exit 1;
fi 

if [ $KRB_FLAG -gt 1 ]
then 
  echo "Error: the -a and -A options are mutually exclusive" >&2
  exit 1;
fi

# if kerberos is enabled, we need to supply a set of credentials, 
# (it doesn't matter what they are, so we can use the defaults).
# if a username & password were supplied we'll use those, either
# for SPNEGO or HTTP Basic auth
if [ $KRB_FLAG -eq 1 -o $CREDENTIALS_FLAG -gt 0 ] 
then
  AUTH_OPTS="${AUTH_OPTS} -u${USERNAME}:${PASSWORD}"
fi

export HOST
export SCHEME
export CERT_FILE
export AUTH_OPTS
