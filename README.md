# AWPLab Core Karaf Library

This is a library for use in [Karaf](http://karaf.apache.org/) 4 that supports the following:
  1. [iPOJO](http://felix.apache.org/documentation/subprojects/apache-felix-ipojo.html) native Karaf commands
  2. [Quartz Scheduler](https://quartz-scheduler.org/) with support for multiple schedulers and iPOJO suport
  3. [Jersey](https://jersey.java.net/) rest server (and client) with multiple aliases
    1.  Jackson support with Jackson JAX-RS providers and data modules
    2.  Basic security support with integration in Karaf jaas
  4. Robust features.xml will all dependencies required to run the library


Currently working on documentation and additional code so this repository should be considered under development at this time.  Currently only SNAPSHOT versions of the library are available until initial release

## Requirements
  1. Java 1.8+ (sorry, default interface implementations too valuable :))
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

**Using the Scheduler Library**

In your code, you will need to include a reference to the service library
```xml
<dependency>
    <groupId>com.awplab.core</groupId>
    <artifactId>scheduler.service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
To install the library in Karaf, use the following features (after install the features as described above) which will install the core library and all dependencies (including quartz scheduler):
```
feature:install core-scheduler
```

**Create a simple scheduler**
```java
@Requires
SchedulerManagerService schedulerManagerService

private void createScheduleExample() {
    // creates a simple scheduler with a RAMJobStore and 10 initial threads using a ResizableThreadPool
    schedulerManagerService.addScheduler(new VolatileSchedulerProvider("Simple", 10));
}
```

**Creating a job class**

Any job the implements quartz's standard job interfaces (Job.class / InterruptableJob.class) can be used.

This library also provides a *StatusJob.class* interface which adds a getJobStatus() method that cna be used to return a status of the job while running.   This status will show up, if the job implements this interface, in the command line and rest client interfaces.   The returned object is serialized as a JSON string using Jackson Object Mapper so Jackson annotations on the return class will be respected.

In addition, a *AbstractStatusInterruptableJob.class* abstract class is provided which can be extended to help support interrupt notifications in the job execution.

Although not necessary for usage, the provided schedulers also support iPOJO instantiation of the job from a iPOJO factory.   The system will scan available factories for a factory name that matches the class name or a factory who's meta data has the associated class name with it.

```java
@Component
public class HelloWorldJob extends AbstractStatusInterruptableJob {

    @Requires
    EventAdmin eventAdmin

    @Override
    public void interruptableExecute(JobExecutionContext context) throws JobExecutionException {
        while (!isInterruptRequested()) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored) {
                return;
            }

            eventAdmin.postEvent(new Event("com/awplab/core/test/HELLO_WORLD", Collections.emptyMap()));
        }

    }

    @Override
    public Object getJobStatus() {
        return "Running...  You can return any class here that is serializable by Jackson to JSON format.";
    }

}
```

**Schedule a job**

Job scheduling can be done using traditional quartz scheduler methods.
```java
// Get the scheduler we created before....
Scheduler scheduler = schedulerManagerService.getScheduler("simple");

JobDetail job = newJob(HelloWorldJob.class)
    .withIdentity("job1", "group1")
    .build();

// Trigger the job to run now, and then repeat every 40 seconds
Trigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .startNow()
    .withSchedule(simpleSchedule()
    .withIntervalInSeconds(40)
    .repeatForever())
    .build();

scheduler.scheduleJob(job, trigger);

```

Supplemental methods are made available to help reduce quartz boiler plate code in the SchedulerManagerService:
```java
schedulerManagerService.runJob(HelloWorldJob.class);
schedulerManagerService.scheduleJob(HelloWorldJob.class, null, 10, TimeUnit.MINUTES);
```

**Create a simple scheduler using the configuration admin**

Exmple using configuration command line:
```
config:edit com.awplab.core.scheduler.volatile-?
config:property-set com.awplab.core.scheduler.volatile.name simple
config:property-set com.awplab.core.scheduler.volatile.threads 10
config:update
```
Example using karaf features:
```xml
<config name="com.awplab.core.scheduler.volatile-?">
    com.awplab.core.scheduler.volatile.name = simple
    com.awplab.core.scheduler.volatile.threads = 10
</config>
```
**Scheduler Commands**

The library has built in some Karaf commands to help manage list running and scheduled jobs.

**Event Admin Topics**

All schedulers managed by the scheduler are registered with a EventAdminListener that will post event admin events for all listened events.   A list of topics is found in SchedulerEventTopics

## Jersey Rest Server

Jersey is the JAX-RS reference implementaton for RESTful web services.   The focus of this library is to allow for services to register itself other classes or singletons as JAX-RS service providers.

The library provides a RestManagerService that manages JAX-RS applications, registering them with the HTTP service provider as a Jersey Servlet, for each unique alias regsitered with the service manager.  The default service provider here is Apache Felix's HTTP service.

The RestManagerService allows multiple methods to register rest providers, classes, or singletons with the service.   This includes monitoring, utilizing a white board pattern, any service registered as a RestService.class.   Additionally the RestServiceManager has methods to manually register providers.  iPOJO is used in the examples, but is *not required* as the service(s) are available as standard OSGI services.

**Using the Rest Library**

In your code, you will need to include a reference to the service library
```xml
<dependency>
    <groupId>com.awplab.core</groupId>
    <artifactId>rest.service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
To install the library in Karaf, use the following features (after install the features as described above) which will install the core library and all dependencies (including Jersey):
```
feature:install core-rest
```
or to install the library and include Jackson JAX-RS JSON providers
```
feature:install core-rest-jackson
```

**Create a rest provider**

This simple example will create a RESTful webservice at the default alias ("/")
```java
@Component(immediate = true)
@Instantiate
@Provides
public class HelloRestProvider implements RestService
{

    @Path("hello")
    @Produces("text/html")
    public String hello(@QueryParam("name") String name) {
        return "Hello " + name + "!";
    }

}
```
To specify a different root alias, you can do so by overriding the getAlias() default implementation:
```java
    @Override
    public String getAlias() {

        return "/root";
    }
```
Alias naming requires the path to start with a / and not end with a /.

**Karaf jaas security**

Integration with Karaf jaas security is supported currently though basic HTTP username and password authentication.   This is done though an annotation of the class or methods with the @RequireBasicAuth.  A karaf realm can be specified (default is karaf) as well as access can limitation by group or role assignment.  This annotation can be applied at the class level or function level.

Note: *By default the feature requires the connection to be secure (HTTPS connection or X-Forwarded-Proto header set to HTTPS), but this can be overriden by setting requiresSecure = false*
```
@Component(immediate = true)
@Instantiate
@Provides
public class HelloRestProvider implements RestService
{

    // This example shows limiting access to users with admin privledges
    @Path("hello")
    @Produces("text/html")
    @RequireBasicAuth(limitToRoles = {"admin"})
    public String hello(@QueryParam("name") String name) {
        return "Hello " + name + "!";
    }

}
```

##Credits##
The original library was written by Andrew Phillips as part of some startup projects (HDScores, MDScores, etc...).

##License##
This software is licensed under the commercial friendly MIT License.  See LICENSE file for more details.
