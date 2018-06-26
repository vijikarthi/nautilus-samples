# Nautilus Samples

This repository contains sample [Flink](https://flink.apache.org/) applications that integrate with [Pravega](http://pravega.io/) and are optimized for use in Nautilus.

The samples are based on the latest release of Pravega (`0.3.0`).

## Getting Started
These instructions assume that Pravega server is already installed and available.  See the [Pravega Deployment Overview](http://pravega.io/docs/v0.3.0/deployment/deployment/) for more information.

### Build the Sample Code

Follow the below steps to build the sample code:

```
$ git clone https://github.com/pravega/nautilus-samples.git
$ cd nautilus-samples
$ ./gradlew clean installDist
```

## Anomaly Detection

The anomaly detection sample is an application that simulates network anomaly intrusion and detection using Apache Flink and Apache Pravega. It contains details [instructions](anomaly-detection) on how to deploy, configure and run this sample on Nautilus.
