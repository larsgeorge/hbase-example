#!/bin/bash

set -eo pipefail

# apply templates to config files
for template in $(ls ${HBASE_CONF_DIR}/*.mustache)
do
    conf_file=${template%.mustache}
    cat ${conf_file}.mustache | mustache.sh > ${conf_file}
done

if [[ $WITH_PHOENIX == "true" ]]; then
    echo "Enabling Phoenix integration"
    PHOENIX_JAR="/usr/local/phoenix-hbase-${HBASE_MINOR_VERSION}-${PHOENIX_VERSION}-bin/phoenix-server-hbase-${HBASE_MINOR_VERSION}-${PHOENIX_VERSION}.jar"
    export HBASE_CLASSPATH=$PHOENIX_JAR
    cp $PHOENIX_JAR $HBASE_HOME/lib/
fi

echo
echo "Dumping HBase Configuration..."
hbase org.apache.hadoop.hbase.HBaseConfiguration
echo "------------------------------"
echo
echo "Starting HBase (Standalone Mode)..."
hbase --config "${HBASE_CONF_DIR}" master start 

exec "$@"
