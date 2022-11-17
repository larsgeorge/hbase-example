# HBase Examples

This repo contains an easy way to a) create a HBase docker image and b) run examples against it.

## Docker Image

After cloning the repo, you can build and run the HBase Docker image, like so:

```
$ docker build -t larsgeorge/hbase-standalone:v0.1 hbase
$ docker run --rm -p 2181:2181 -p 16000:16000 -p 16010:16010 -p 16020:16020 -p 16030:16030 --name hbase larsgeorge/hbase-standalone:v0.1
```

Notes:
- You can set the HBase version at the top of the `Dockerfile`
- The exposed and mapped ports are required for clients to connect (via Zookeeper) and talk to the services provided by HBase (Thrift API and informational WebUIs)

Once the container is running, connect to the HBase Master WebUI like so:

> http://localhost:16010/master-status

## HBase Shell

You can use the built-in HBase Shell to issue commands:

```sh
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

```sh
$ docker ps
CONTAINER ID   IMAGE                          COMMAND           CREATED          STATUS          PORTS                                                                                                                            NAMES
c7da77582ee4   larsgeorge/hbase-standalone:v0.1   "entrypoint.sh"   29 seconds ago   Up 27 seconds   0.0.0.0:2181->2181/tcp, 0.0.0.0:16000->16000/tcp, 0.0.0.0:16010->16010/tcp, 0.0.0.0:16020->16020/tcp, 0.0.0.0:16030->16030/tcp   hbase
...
$ sudo vi /etc/hosts
$ cat /etc/hosts
...
127.0.0.1       c7da77582ee4
...
```

## HBase Examples

The included examples are build via Maven:

```sh
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

```sh
$ mvn -q exec:java -Dexec.mainClass="com.larsgeorge.ScanExample"
```

## Local Cluster Setup

Install and start Docker:

Note: For non-Linux operating systems use an alternative, like Docker Desktop for Windows!

```sh
$ sudo yum update
$ sudo yum install docker
$ sudo usermod -a -G docker ec2-user
$ id ec2-user
$ newgrp docker
$ sudo systemctl enable docker.service
$ sudo systemctl start docker.service
```

Install kubectl CLI tool:

```sh
$ curl -o kubectl https://s3.us-west-2.amazonaws.com/amazon-eks/1.23.7/2022-06-29/bin/linux/amd64/kubectl
$ sudo mv kubectl /usr/local/bin/
$ sudo chmod +x /usr/local/bin/kubectl
$ kubectl
```

Install kind:

```sh
$ curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.17.0/kind-linux-amd64
$ chmod +x ./kind
$ sudo mv ./kind /usr/local/bin/kind
$ kind
```

Install script to run kind with local registry:

```sh
$ curl -O https://kind.sigs.k8s.io/examples/kind-with-registry.sh
$ chmod +x kind-with-registry.sh
$ sudo mv kind-with-registry.sh /usr/local/bin/
```

OPTIONALLY - Start local K8s cluster with registry:
DO NOT DO THIS USALLY!

```sh
$ kind-with-registry.sh
$ kubectl get pods # for testing only
```

INSTEAD DO THIS:
This also creates a local data directory that is mapped into
the kind configuration and then mapped again in the Helm Chart. 

```sh
$ sudo mkdir -p /data
$ sudo chown -R docker:docker /data
$ tools/start-local-cluster.sh 
$ kubectl get pods # for testing only
```



Install Helm:

```sh
$ curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
$ chmod 700 get_helm.sh
$ ./get_helm.sh
```

Tag and push images:

```sh
$ docker tag larsgeorge/hbase-standalone:v0.1 larsgeorge/hbase-standalone:2.4.15
$ docker push larsgeorge/hbase-standalone:2.4.15
```

Deploy HBase:

```sh
$ helm --create-namespace --namespace=hbase upgrade -i hbase helm -f helm/values.yaml
Release "hbase" does not exist. Installing it now.
$ kubectl get pods
```

Install k9s:

```
$ curl -OL https://github.com/derailed/k9s/releases/download/v0.26.7/k9s_Linux_x86_64.tar.gz
$ tar -zxvf k9s_Linux_x86_64.tar.gz
$ rm LICENSE README.md
$ sudo mv k9s /usr/local/bin/
$ k9s
```

Port forwarding:

```sh
$ kubectl -n hbase port-forward service/hbase-master 16000:16000 16010:16010 16020:16020 16030:16030 --address 0.0.0.0
Forwarding from 0.0.0.0:16000 -> 16000
Forwarding from 0.0.0.0:16010 -> 16010
Forwarding from 0.0.0.0:16020 -> 16020
Forwarding from 0.0.0.0:16030 -> 16030
```