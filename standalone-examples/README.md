# Standalone Examples of Nautilus Applications
These applications only need a running Nautilus Deployment to execute against.

## Pre requisites
1. Java 8
2. Nautilus Deployment

## Building

You may be required to set the `repositoryUrl` property to properly resolve several pacakges. Typically it should point to the master node's maven repository: `http://${nautilusMasterIP}/maven`.

Most examples can be run either using the gradle wrapper (gradlew) or scripts.
To run the examples using scripts, the scripts need to be generated.  In the directory where you downloaded the nautilus samples, run the following once, and all the scripts will be generated.

```
$ ./gradlew installDist
```

The scripts can be found under the nautilus-samples directory in:

```
standalone-examples/build/install/nautilus-standalone-examples/bin
```

There is a Linux/Mac script and a Windows (.bat) script for each separate executable.

## HelloPravega Example
This example consists of two applications, a HelloWorldReader that reads from a stream and a HelloWorldWriter, that writes to a stream.  You might want to run HelloWorldWriter in one window and HelloWorldReader in another window. 

### Authentication
Authentication is enabled by default during the creation of your cluster; you must define several **ENV** variables to build the sample with authentication enabled. In order for your user to access the Pravega stream(s), they must be given the necessary ACLs at the scope level. This is accomplished through the Nautilus UI for your cluster.

```
export pravega_client_auth_loadDynamic=true
export pravega_client_auth_method=nautilus
export NAUTILUS_GUARDIAN_URL=https://{nautilusMasterIP}/auth
export NAUTILUS_USERNAME={username}
export NAUTILUS_PASSWORD={password}
```


You can also define these requirements as system properties by running the sample(s) as a gradle task.

```
$ gradle startHelloWorldWriter \
-Dexec.args="..." \
-Dcom.emc.nautilus.username=username \
-Dcom.emc.nautilus.password=password \
-Dcom.emc.nautilus.guardian.url=https://{nautilusMasterIP}/auth \
-Dpravega.client.auth.loadDynamic="true" \
-Dpravega.client.auth.method=nautilus
```

### HelloWorldWriter
A simple application that shows how to write to a Pravega stream. You can run the sample via the build scripts or by a gradle task.

```
$ bin/helloWorldWriter [-scope myScope] [-name myStream] [-uri tcp://{nautilusMasterIP}:9091] [-routingKey myRK] [-message 'hello world']
```

```
$ gradle startHelloWorldWriter -Dexec.args="-scope myScope -name myStream -uri tcp://{nautilusMasterIP}:9091 -routingKey myRK -message hello world"
```

All args are optional, if not included, the defaults are:

 * scope - "examples"
 * name - "helloStream"
 * uri - "tcp://127.0.0.1:9091" (the URI to the master node)
 * routingKey - "helloRoutingKey"
 * message - "hello world"

The program writes the given message with the given routing key to the stream with given scope/stream name.

### HelloWorldReader
A simple application that shows how to read from a Pravega stream.

```
$ bin/helloWorldReader [-scope myScope] [-name myStream] [-uri tcp://{nautilusMasterIP}:9091]
```

```
$ gradle startHelloWorldReader -Dexec.args="-scope myScope -name myStream -uri tcp://{nautilusMasterIP}:9091"
```

All args are optional, if not included, the defaults are:

 * scope - "examples"
 * name - "helloStream"
 * uri - "tcp://127.0.0.1:9091" (the URI to the master node)

The program reads all the events from the stream with given scope/stream name and prints each event to the console.