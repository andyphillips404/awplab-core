# AWPLab Core Karaf Library

This is a library for use in [Karaf](http://karaf.apache.org/) 4 that supports the following:
  1. [iPOJO](http://felix.apache.org/documentatprivilegesjects/apache-felix-ipojo.html) native Karaf commands
  2. [Quartz Scheduler](https://quartz-scheduler.org/) with support for multiple schedulers and iPOJO support
  3. [Jersey](https://jersey.java.net/) rest server (and client) with multiple aliases
    1. [Jackson](https://github.com/FasterXML) support with Jackson JAX-RS providers and data modules
    2. Basic security support with integration in Karaf jaas
    3. [OpenAPI](https://openapis.org/)/[Swagger](http://swagger.io/) support with annotations or config admin
  4. Robust features.xml will all dependencies required to run the library


Currently working on documentation and additional code so this repository should be considered under development at this time.  Currently only SNAPSHOT versions of the library are available until initial release

# Minimum Requirements
  1. Java 1.8
  2. Karaf 4.0


# Installing the features

```
feature:repo-add mvn:com.awplab.core/features/LATEST/xml/features
```

# iPOJO native Karaf commands

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

# Quartz Scheduler

The motivation behind creating the quartz scheduler implemenation, as opposed to the scheduler in Karaf distribution, is to allow for the following improvements:
  1.  Better native support of Quartz schedulers
  2.  Support for multiple schedulers, particularly with flexible thread limits
  3.  Support for instance creation using iPOJO API (allowing for iPOJO annotations in the jobs classes).
  5.  Event Admin notifications
  6.  Registration of schedulers utilizing the white board pattern, configuration admin, or directly using the manager service

In order to achieve this, a new Quartz scheduler factory was created as a OSGI service, available as SchedulerManagerService.class and should be used to manage all schedulers.   This service is available as the SchedulerManagerService.class.  iPOJO is used in the examples, but is *not required* as the service is available as standard OSGI services.

### Using the Scheduler Library

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

### Create a simple scheduler
```java
@Requires
SchedulerManagerService schedulerManagerService

private void createScheduleExample() {
    // creates a simple scheduler with a RAMJobStore and 10 initial threads using a ResizableThreadPool
    schedulerManagerService.addScheduler(new VolatileSchedulerProvider("Simple", 10));
}
```

### Creating a job class

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

### Schedule a job

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

### Create a simple scheduler using the configuration admin

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
### Scheduler Commands

The library has built in some Karaf commands to help manage list running and scheduled jobs.  These commands can be found with the prefix scheduler:*

### Event Admin Topics

All schedulers managed by the scheduler are registered with listeners that send event admin events.   A list of topics is found in SchedulerEventTopics

# Jersey Rest Server

Jersey is the JAX-RS reference implementaton for RESTful web services.   The focus of this library is to allow for services to register itself other classes or singletons as JAX-RS service providers.

The library provides a RestManagerService that manages JAX-RS applications, registering them with the HTTP service provider as a Jersey Servlet, for each unique alias regsitered with the service manager.  The default service provider here is Apache Felix's HTTP service.

The RestManagerService allows multiple methods to register rest providers, classes, or singletons with the service.   This includes monitoring, utilizing a white board pattern, any service registered as a RestService.class.   Additionally the RestServiceManager has methods to manually register providers.  iPOJO is used in the examples, but is *not required* as the service(s) are available as standard OSGI services.

### Using the Rest Library

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
or to install the library and include Jackson JAX-RS JSON providers and OpenAPI / Swagger support
```
feature:install core-rest-swagger
```

### Create a rest provider

This simple example will create a RESTful webservice at the default alias ("/service")
```java
@Component(immediate = true)
@Instantiate
@Provides
@Path("/")
public class HelloRestProvider implements RestService
{
    @GET
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

        return "/rest";
    }
```
**NOTE:** Alias naming requires the path to start with a / and not end with a /.

**NOTE:**  There appears to be a bug in either Jetty or Jersey Servlet Container when using the root alias of "/".   Currently investigating still, but at this time the root alias should be avoided.

You may register additional classes with the rest manager by overriding the getClasses(String alias) and getSingletons(String alias) methods:
```java
    @Override
    public Set<Class<?>> getClasses(String alias) {
        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(SwaggerRestProvider.class);
        classes.add(CorsResponseFilter.class);
        return classes;
    }

```
By default, the getClasses(String alias) returns Collections.emptySet();

Under the covers, the RestApplication calls two methods to get its classes and singletons to register with the Jersey.   The getClasses(String alias) and getSingletons(String alias).   The getSingletons(String alias) returns, unless the alias is GLOBAL (more ina bit on this), an instance of itself.   You may override this behavior as need be to return additional singletons but if you should return an instance of yourself if you wish to suplement the exiting instance.   The default implementation looks like:
```java
    default Set<Object> getSingletons(String alias) {
        if (!getAlias().equals(RestManagerService.GLOBAL_ALIAS)) return Collections.singleton(this);
        else return Collections.emptySet();
    }
```

The GLOBAL_ALIAS can be used to register classes and singletons with every unique non global alias registered.   This is how the OpenAPI / Swagger implementation is done, for example.   Remember, instances of a class returned as a singletone **CANNOT** be shared between aliases as this violates JAX-RS and Jersey.

### Jersey Commands

The library has built in some Karaf commands to help manage list providers and rest applications / aliases.   The commands start with thre prefix rest:*.

### Event Admin Topics

The rest manager will send event admin events on various state changes with the providers (load, reload, etc...).   A list of topics is found in RestEventTopics

## Karaf jaas security

Integration with Karaf jaas security is supported currently though basic HTTP username and password authentication.   This is done though an annotation of the class or methods with the @RequireBasicAuth.  A karaf realm can be specified (default is karaf) as well as access can limitation by group or role assignment.  This annotation can be applied at the class level or function level.

Note: *By default the feature requires the connection to be secure (HTTPS connection or X-Forwarded-Proto header set to HTTPS), but this can be overriden by setting requiresSecure = false*
```java
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

## OpenAPI / Swagger implementation

### Using the OpenAPI / Swagger Library

In your code, you will need to include a reference to the service library
```xml
<dependency>
    <groupId>com.awplab.core</groupId>
    <artifactId>rest.swagger</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
To install the library in Karaf, use the following features:
```
feature:install core-rest-swagger
```

### Annotation of a RestService provider

The core library will automatically, unless disabled, create OpenAPI / Swagger 2.0 compliant json and yaml feed found at the root of each alias as */swagger.json* and */swagger.yaml*.  The feed is generated with each request by scanning the every rest provider with a class with the appropriate swagger annotations.   The below examples show a class with the required annotations:

```java
@Component(immediate = true)
@Instantiate
@Provides
@Path("/")
@SwaggerDefinition(host = "awplab.com", basePath = "/root",
        info = @Info(title = "Swagger API Example Title", version = "1.0.0", description = "Description of API", termsOfService = "Terms",
                contact = @Contact(name = "Andy"),
                license = @License(name = "Apache")))
@Api(value = "Hello Test")
public class HelloRestProvider implements RestService
{
   @GET
    @Path("hello")
    @Produces("text/html")
    @ApiOperation(value = "Hello test operation", notes = "Notes for the operation")
    public String hello(@QueryParam("name") String name) {
        return "Hello " + name + "!";
    }
}

```
For more details on the annotation usage, including annotations not used here, please see [swagger](http://swagger.io) website.

### CORS Filter
Included in the library is a CorsResponseFilter that will add the appropriate header fields to support java script same origion policy issues when working with Swagger documentation and code generation.  This can also be disabled in the manager configuration.

### Swagger Global Manager Configuration

The swagger global manager can be managed, including disabled, with the configuration admin at pid *com.awplab.core.rest.swagger.global*.   The following describes configuration options for the global manager:

| Property Name | Default Value | Description |
| ------------- | :-------------: | ----------- |
|com.awplab.core.rest.swagger.global.enabled|true|Enable or Disable to global swagger manager|
|com.awplab.core.rest.swagger.global.cors|true|Enable or Disable the CORS filter|
|com.awplab.core.rest.swagger.global.skipAliases|null|Array of strings representing a list of aliases to skip|
|com.awplab.core.rest.swagger.global.onlyIncludeAliases|null|Array of strings representing a list of aliases to only include.   If null or not set, include all aliases.|

### Fine Tune Control

If you disable the swagger global manager, you may still register Swagger rest providers indivually per alias with more fine grained control using the configuraiton admin or overriding BaseSwaggerRestProvider.   See SwaggerRestProvider and BaseSwaggerRestProvider javadoc for more details.

## Jackson / Jackson JAX-RS

The Jackson rest library was added to allow for registration of JAX-RS providers and data modules for ObjectMappers from Jackson in the RestManagerService globally.  This functionality is used in our features.xml to register the modules and jaxrs providers that are installed.

### Using the Jackson library

In your code, you will need to include a reference to the service library
```xml
<dependency>
    <groupId>com.awplab.core</groupId>
    <artifactId>rest.jackson</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
To install the library in Karaf, use the following features (also included in core-rest-swagger):
```
feature:install core-rest-jackson
```

### Loading Jackson, Modules, JAX-RS Providers, Data Formats
We have loaded a set of stanard data modules that the system supports in the features.  By installing these features, the data modules are available in all registered Jackson JAX-RS providers.  Below is a list of supported modules and JAX-RS providers.   You can reveiw features.xml file to see how to use the configuration admin to add more.

### Included Jackson Modules

|Feature|Module Class|Description|
|-------|------------|-----------|
|jackson-jaxb|com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule|JAXB annotation support|
|jackson-joda|com.fasterxml.jackson.datatype.joda.JodaModule|Joda time support|
|jackson-jdk7|com.fasterxml.jackson.datatype.jdk7.Jdk7Module|JDK 1.7 class support|
|jackson-jdk8|com.fasterxml.jackson.datatype.jdk8.Jdk8Module, com.fasterxml.jackson.datatype.jsr310.JSR310Module|JDK 1.8 Class and Date format support|

### Registering with Object Mapper

If you are using a custom object mapper, you can register all supported modules in the system with the object mapper.  Traditionally with Jackson, the object mappers have a auto discovery feature of modules, but this does not work in OSGI.   Using the JacksonManagerService will allow you to register all modules the manager manages with an object mapper
```java
@Requires
JacksonManagerService jacksonManagerService;

public void String toJson(Object test) {
    ObjectMapper objectMapper = new ObjectMapper();
    jacksonManagerService.registerModulesWithObjectMapper(objectMapper);
    return objectMapper.writeValueAsString(test);
}
```


### Included Jackson JAX-RS Providers
|Feature|JAX-RS Provider|Object Mapper|Description|
|-------|---------------|-------------|-----------|
|jackson-jaxrs-json|com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider|com.fasterxml.jackson.databind.ObjectMapper|JSON JAX-RS Provider|
|jackson-jaxrs-xml|com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider|com.fasterxml.jackson.dataformat.xml.XmlMapper|XML JAX-RS Provider|

### Included Data Formats
|Feature|Description|
|-------|-----------|
|jackson-csv|CSV Object Mapper|
|jackson-xml|XML Object Mapper|
|jackson-yaml|YAML Object Mapper|



# Credits
The original library was written by Andrew Phillips as part of some startup projects (HDScores, MDScores, etc...).

# License
This software is licensed under the commercial friendly MIT License.  See LICENSE file for more details.
