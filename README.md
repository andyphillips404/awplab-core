# AWPLab Core Karaf Library

This is a library for use in [Karaf](http://karaf.apache.org/) 4 that supports the following:
  1. [iPOJO](http://felix.apache.org/documentation/subprojects/apache-felix-ipojo.html) native Karaf commands
  2. [Quartz Scheduler](https://quartz-scheduler.org/) with support for multiple schedulers and iPOJO suport
  3. [Jersey](https://jersey.java.net/) rest server (and client) with multiple aliases
    1.  Jackson support with Jackson JAX-RS providers and data modules
    2.  Basic security support with integration in Karaf jaas


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
ipojo:instances
ipojo:instance
ipojo:factories
ipojo:factory
```
Some of the code was lifted from OW2 Shelbie 2 and ported to Karaf 4.

## Quartz Scheduler

The motivation behind creating the quartz scheduler implemenation, as opposed to the scheduler in Karaf distribution, is to allow for the following improvements:
  1.  Better native support of Quartz schedulers
  2.  Support for multiple schedulers, particularly with flexible thread limits
  3.  Support for instance creation using iPOJO API (allowing for iPOJO annotations in the jobs classes).
  5.  Event Admin notifications
  6.  Registration of schedulers utilizing the white board pattern, configuration admin, or directly using the manager service

In order to achieve this, a new Quartz scheduler factory was created as a OSGI service, available as SchedulerManagerService.class and should be used to manage all schedulers.   This service is available as the SchedulerManagerService.class.  iPOJO is used in the examples, but is *not required* as the service is available as standard OSGI services.

**Create a simple scheduler**
```
@Requires
SchedulerManagerService schedulerManagerService

private void createScheduleExample() {
    // creates a simple schedule with a RAMJobStore and 10 initial threads in ResizableThreadPool
    schedulerManagerService.addScheduler(new VolatileSchedulerProvider("Simple", 10));
}
```

**Schedule a job**


**Create a simple scheduler using the configuration admin**

Exmple using configuration command line:
```
config:edit com.awplab.core.scheduler.volatile-?
config:property-set com.awplab.core.scheduler.volatile.name simple
config:property-set com.awplab.core.scheduler.volatile.threads 10
config:update
```
Example using karaf features:
```
<config name="com.awplab.core.scheduler.volatile-?">
    com.awplab.core.scheduler.volatile.name = simple
    com.awplab.core.scheduler.volatile.threads = 10
</config>
```





