# AWPLab Core Karaf Library

This is a library for use in [Karaf](http://karaf.apache.org/) 4 that supports the following:
  1. [iPOJO](http://felix.apache.org/documentation/subprojects/apache-felix-ipojo.html) native Karaf commands
  2. [Quartz Scheduler](https://quartz-scheduler.org/) with support for multiple schedulers and iPOJO suport
  3. [Jersey](https://jersey.java.net/) rest server (and client) with multiple aliases
    1.  Jackson support with Jackson JAX-RS providers and data modules


Currently working on documentation and additional code so this repository should be considered under development at this time

## Requirements
  1. Java 1.8+
  2. Karaf 4.0+


## Installing the features

```
feature:repo-add mvn:com.awplab.core/features/LATEST/xml/features
```

## iPOJO native Karaf commands

To install the commands, install the core-ipojo feature:
```
feature:install core-ipojo
```
iPOJO native command support for Karaf allows for the use of the following commands in Karaf natively (as opposed to the iPOJO shell library):
```

```





