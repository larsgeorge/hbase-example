FROM alpine:3.16.2

ARG KDC_REALM=LOCAL
ARG KDC_NET_TLD=local
ARG KDC_HOST_FQDN=kdc.${KDC_NET_TLD}

ENV KDC_REALM=$KDC_REALM
ENV KDC_NET_TLD=$KDC_NET_TLD
ENV KDC_HOST_FQDN=$KDC_HOST_FQDN

ENV KDC_CONF_DIR=/var/kerberos/krb5kdc
ENV KDC_CONF_FILE=${KDC_CONF_DIR}/kdc.conf
ENV KADM5_ACL_CONF_FILE=${KDC_CONF_DIR}/kadm5.acl

ENV KRB5_CONF_DIR=/etc
ENV KRB5_CONF_FILE=${KRB5_CONF_DIR}/krb5.conf

RUN set -x \
    && apk update \
    && apk --no-cache add \
        krb5 \
        krb5-server \
        bash \
        less \
        tar \
        wget

# copy in templates and scripts
COPY conf/server/*  ${KDC_CONF_DIR}
COPY conf/client/*  ${KRB5_CONF_DIR}
COPY bin/*          /usr/local/bin/
COPY lib/*          /usr/local/lib/

EXPOSE 88/tcp
EXPOSE 88/udp

ENTRYPOINT ["entrypoint.sh"]
