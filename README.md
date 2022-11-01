# HBase Examples

This repo contains an easy way to a) create a HBase docker image and b) run examples against it.

## Docker Image

After cloning the repo, you can build and run the HBase Docker image, like so:

```
$ cd docker
$ docker build -t larsgeorge/hbase-single:v0.1 .
$ docker run --rm -p 2181:2181 -p 16000:16000 -p 16010:16010 -p 16020:16020 -p 16030:16030 --name hbase larsgeorge/hbase-single:v0.1
```

Notes:
- You can set the HBase version at the top of the `Dockerfile`
- The exposed and mapped ports are required for clients to connect (via Zookeeper) and talk to the services provided by HBase (Thrift API and informational WebUIs)

Once the container is running, connect to the HBase Master WebUI like so:

> http://localhost:16010/master-status

## HBase Shell

You can use the built-in HBase Shell to issue commands:

```
$ docker exec -it hbase hbase shell
...
HBase Shell
Use "help" to get list of supported commands.
Use "exit" to quit this interactive shell.
For Reference, please visit: http://hbase.apache.org/2.0/book.html#shell
Version 2.4.15, r35310fcd6b11a1d04d75eb7db2e592dd34e4d5b6, Thu Oct 13 11:42:20 PDT 2022
Took 0.0010 seconds
hbase:001:0>
```

Another option is to use a local HBase binary tarball, which by default uses `localhost` as the server address as well.
The steps are:

1. Download the tarball from the HBase website
2. Untar it with `$ tar -xzvf <hbase_tarball_name>`
3. Change into the unpacked directory
4. Start HBase Shell

For example:

```
$ cd ~
$ curl -O https://dlcdn.apache.org/hbase/2.4.15/hbase-2.4.15-bin.tar.gz
$ tar -xzvf hbase-2.4.15-bin.tar.gz
~$ cd hbase-2.4.15
~/hbase-2.4.15$ bin/hbase shell
...
HBase Shell
Use "help" to get list of supported commands.
Use "exit" to quit this interactive shell.
For Reference, please visit: http://hbase.apache.org/2.0/book.html#shell
Version 2.4.15, r35310fcd6b11a1d04d75eb7db2e592dd34e4d5b6, Thu Oct 13 11:42:20 PDT 2022
Took 0.0012 seconds
hbase:001:0> list
TABLE
0 row(s)
Took 0.2062 seconds
=> []
hbase:002:0> list_namespace_tables 'hbase'
TABLE
meta
namespace
2 row(s)
Took 0.0195 seconds
=> ["meta", "namespace"]
hbase:003:0>
```

NOTE: It may be necessary to ensure the container's internal hostname can be resolved from the local terminal.
If that is the case, get the container ID from `docker ps` (first column) and update the local `/etc/hosts` so that the container ID as a hostname points to the loopback IP `127.0.0.1`.
For example:

```
$ docker ps
CONTAINER ID   IMAGE                          COMMAND           CREATED          STATUS          PORTS                                                                                                                            NAMES
c7da77582ee4   larsgeorge/hbase-single:v0.1   "entrypoint.sh"   29 seconds ago   Up 27 seconds   0.0.0.0:2181->2181/tcp, 0.0.0.0:16000->16000/tcp, 0.0.0.0:16010->16010/tcp, 0.0.0.0:16020->16020/tcp, 0.0.0.0:16030->16030/tcp   hbase
...
$ sudo vi /etc/hosts
$ cat /etc/hosts
...
127.0.0.1       c7da77582ee4
...
```

## HBase Examples

The included examples are build via Maven:

```
$ cd hbase-examples
$ mvn package
...
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hbase-example ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.130 s
[INFO] Finished at: 2022-11-01T12:23:24+01:00
[INFO] ------------------------------------------------------------------------
```

The run the examples using Maven as well, which helps setting up the proper Java `classpath` for you:

```
$ mvn -q exec:java -Dexec.mainClass="com.larsgeorge.ScanExample"
```