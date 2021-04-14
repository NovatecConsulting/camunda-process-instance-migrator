# Camunda Process Instance Migrator

This tool will allow you to automatically or semi-automatically migrate all of your Camunda Process Instances whenever you release a new version.

## Why should I use this?

If you develop process definition in an agile environment, your process definitions will change regularly. As soon as your process definitions are instantiated, be it in a test or productive environment, you would be advised to migrate them whenever a new process definition is released. 
This is for two reasons:
1. Without migration your process instances will not gain the features added in the new release
2. Without migration you are forced to maintain the existing Java API: you may not rename Java Delegates or change the signature of called Bean's methods. 

If you want to learn more about it, please read this article: https://www.novatec-gmbh.de/blog/automating-process-instance-migration-with-camunda-bpm/

## How do I use this?

In order to use the migrator, all your process models need to be properly versioned. Versioning is done via the Version Tag property of the process:
* A version has the format Major.Minor.Patch, with 1.0.0 being the first released version.
* Process definitions with a different or missing format, or process definition with major version 0 (e.g. 0.0.1) will not be subject of migration
* The patch level should be increased whenever a "simple" change is conducted. This includes:
    * Renaming Activities ot other Flow-Elements
    * Adding Activities or other Flow-Elements
    * Changes to the Java-API (e.g. Java Delegates, called Beans, Delegate Expressions)
    * Removing Activities that aren't wait states
* The minor level should be increased whenever a change is conducted that does not allow for a migration using the mapping of activity IDs. This includes:
    * Changing the ID of Activities that are wait states
    * Removing Activities that are wait states
    * Moving Activities in to Sub Processes
* The major level should be increased whenever a change is conducted where, for technical or other reasons, no migration is wanted.

First, add the dependency:

```xml
<dependency>
    <groupId>info.novatec</groupId>
    <artifactId>camunda-process-instance-migrator</artifactId>
    <version>1.0.1</version>
</dependency>
```
Secondly, initialise the migrator by injecting the only dependency to it, Camundas ProcessEngine. For example:

```java
@Configuration
public class MigratorConfiguration {

    @Autowired
    private ProcessEngine processEngine;
    
    @Bean
    public ProcessInstanceMigrator processInstanceMigrator() {
        return new ProcessInstanceMigrator(processEngine);
    }
}
```
You may then use the ProcessInstanceMigrator-Bean to manually trigger the migration (e.g. via a REST-endpoint), or to automatically migrate upon each deployment using @PostConstruct:

```java
@Component
public class OnStartupMigrator {

    @Autowired
    private ProcessInstanceMigrator processInstanceMigrator;
    
    @PostConstruct
    public void migrateAllProcessInstances() {
        processInstanceMigrator.migrateInstancesOfAllProcesses();
    }
}
```
## What limitations are there?

The tool was developed and tested using camunda 7.14. It may not work with older versions but there will be releases compatible with newer versions of Camunda BPM.

As of right now, migration of minor versions is not yet supported.

## What else do I need to know?

Firstly, Migration of Process Engines takes "real" time. Migrating thousands of Process Instances may take several minutes. The containing application should not be impacted, but it should be kept in mind.

Secondly, the migrator was built to be robust and informative. Any action the migrator takes will be logged, and any issue that may come up during migration, will just cause the migration of that specific process instance to fail and be logged accordingly. So it is adviced to check your logs after each migration for faulty process instances. It is very rare that a migration attempt fails, but when it does, you may want to correct it manually.

## Can I contribute?

Of course! Add an issue, submit a pull request. We will be happy to extend the tool with your help.