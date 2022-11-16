#!/bin/bash

set -eo pipefail

# apply templates to config files
for template in $(ls ${KDC_CONF_DIR}/*.mustache ${KRB5_CONF_DIR}/*.mustache)
do
    conf_file=${template%.mustache}
    cat ${conf_file}.mustache | mustache.sh > ${conf_file}
    # DEBUG
    echo
    echo "File: ${conf_file}"
    cat ${conf_file}
    echo
done

# create KDC database
test -e /var/lib/krb5kdc/principal || kdb5_util create -s -P asdf
# create root principal
kadmin.local get_principal -terse root || kadmin.local add_principal -pw asdf root
kadmin.local get_principal -terse root/admin || kadmin.local add_principal -pw asdf root/admin
# start daemon
krb5kdc -n

ls -la /var/log/
#cat /var/log/

exec "$@"

