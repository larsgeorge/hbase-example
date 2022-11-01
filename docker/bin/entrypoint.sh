#!/bin/bash

set -eo pipefail

# apply templates to config files
for template in $(ls ${HBASE_CONF_DIR}/*.mustache)
do
    conf_file=${template%.mustache}
    cat ${conf_file}.mustache | mustache.sh > ${conf_file}
done

echo
echo "Dumping HBase Configuration..."
hbase org.apache.hadoop.hbase.HBaseConfiguration
echo "------------------------------"
echo
echo "Starting HBase (Standalone Mode)..."
hbase --config "${HBASE_CONF_DIR}" master start 

exec "$@"
