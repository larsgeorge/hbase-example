FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.16.1_1
LABEL maintainer="larsgeorge"

ENV HBASE_VERSION=2.4.15
ENV HBASE_HOME=/usr/local/hbase-${HBASE_VERSION}
ENV HBASE_CONF_DIR=${HBASE_HOME}/conf
ENV HBASE_DATA_DIR /data
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$PATH:${JAVA_HOME}/bin:${HBASE_HOME}/bin

RUN set -x \
    && apk update \
    && apk --no-cache add \
        bash \
        less \
        tar \
        wget \
        bzip2 \
        openssl \
        snappy \
        zlib \
    && apk --no-cache add --virtual \
        zip \
    # get tarball and unpack it
    && wget -q -O - https://archive.apache.org/dist/hbase/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz \
        | tar -xzvf - -C /usr/local

# Start new RUN here to avoid downloading the tarball during changes in the below

    # user/dir/permission
RUN adduser -D  -g '' -s /sbin/nologin -u 1000 docker \
    && adduser -D  -g '' -s /sbin/nologin hbase \
    #&& echo "hbase ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/hbase \
    # create directories and set ownerships
    && mkdir -p \
        /etc/hbase \
        #${HBASE_CONF_DIR} \
        #${HBASE_LOG_DIR} \
        ${HBASE_DATA_DIR} \
        /opt/scripts \
    && chown -R hbase:hbase \
        ${HBASE_HOME} \
        #${HBASE_LOG_DIR} \
        ${HBASE_DATA_DIR} \
        /opt/scripts \
    # delete docs
    && rm -rf ${HBASE_HOME}/docs \
    # disable IPv6
    && echo "net.ipv6.conf.all.disable_ipv6 = 1" >> /etc/sysctl.conf \
    # clean up
    && apk del wget

# copy in templates and scripts
COPY conf/* ${HBASE_CONF_DIR}/
COPY bin/*  /usr/local/bin/
COPY lib/*  /usr/local/lib/

#VOLUME ["${HBASE_LOG_DIR}", "${HBASE_DATA_DIR}", "${HBASE_HOME}"]
VOLUME ["${HBASE_DATA_DIR}", "${HBASE_HOME}"]

WORKDIR ${PRESTO_HOME}

EXPOSE 2181
EXPOSE 16000
EXPOSE 16010
EXPOSE 16020
EXPOSE 16030

USER hbase

ENTRYPOINT ["entrypoint.sh"]


