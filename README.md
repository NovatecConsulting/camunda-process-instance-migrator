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
    <version>1.0.3</version>
</dependency>
```
Secondly, initialise the migrator by injecting Camundas ProcessEngine. As long as you're only migrating on patch level and don't need to do minor migrations, there is no need for further configuration:

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
You may then use the ProcessInstanceMigrator-Bean to manually trigger the migration (e.g. via a REST-endpoint), or to automatically migrate upon each deployment via PostConstruct or an ApplicationReadyEvent:

```java
@Component
public class OnStartupMigrator {

    @Autowired
    private ProcessInstanceMigrator processInstanceMigrator;
    
    @EventListener(ApplicationReadyEvent.class)
    public void migrateAllProcessInstances() {
        processInstanceMigrator.migrateInstancesOfAllProcesses();
    }
}
```

Every time you need to do minor migrations (which becomes necessary whenever you remove a wait state activity or move it into a subprocess), you will need to specify instructions for that:

```java
@Configuration
public class MigratorConfiguration {

    @Autowired
    private ProcessEngine processEngine;
    
    @Bean
    public ProcessInstanceMigrator processInstanceMigrator() {
        ProcessInstanceMigrator processInstanceMigrator = new ProcessInstanceMigrator(processEngine);
        
        //MigrationInstructions are required for minor migrations
        processInstanceMigrator.setMigrationInstructions(generateMigrationInstructions());
        return processInstanceMigrator;
    }
    
    private MigrationInstructions generateMigrationInstructions(){
        return MigrationInstructions.Builder()
            .putInstructions("Some_process_definition_key, Arrays.asList(
								MinorMigrationInstructions.builder()
					        		.sourceMinorVersion(0)
					        		.targetMinorVersion(2)					        		
					        		.majorVersion(1)
					        		.migrationInstructions(Arrays.asList(
					        				new MigrationInstructionImpl("UserTask1", "UserTask3"), 
					        				new MigrationInstructionImpl("UserTask2", "UserTask3")))
					        		.build()))
        .build();
    }
}
```
Note that every call of "putInstructions" corresponds to one specific migration (in this case going from 1.0.x to 1.2.x). This could, however, also be achieved by specifying instructions for migration from 1.0.x to 1.1.x and from 1.1.x to 1.2.x.
Note that there is no necessity of actually having all versions deployed on a target environment. If you jump from 1.5.x to 1.8.x in, say, a productive environment, because intermediate versions were only deployed to earlier stages, it will still be sufficient to provide instructions that go from 1.5.x to 1.6.x, from 1.6.x to 1.7.x and from 1.7.x to 1.8.x. The migrator will interpret these instructions accordingly and skip the non-existent versions.

## What limitations are there?

The tool was developed and tested using Camunda 7.14 and subsequently updated to Camunda 7.15 and 7.16. It may not work with older versions but there will be releases compatible with newer versions of Camunda Platform.

Required Java 8.

There are also no restrictions to the specifiable migration instructions for minor migrations, unlike in the migration wizard of Camundas EE Cockpit. So this migrator will not prevent you from trying to migrate activities to different types of activities (i.e. from waitstates to non-waitstates or from receive tasks to user tasks). This might, however, result in undefined states and has not been tested whatsoever. So handle with care!

Operations that go beyond migration, like Process Instance Modifications or the setting of variables upon migration are also not implemented as of yet.

## What else do I need to know?

Firstly, Migration of Process Engines takes "real" time. Migrating thousands of Process Instances may take several minutes. So it is advisable to carry out the migration asynchronously.

Secondly, the migrator was built to be robust and informative. Any action the migrator takes will be logged, and any issue that may come up during migration, will just cause the migration of that specific process instance to fail and be logged accordingly. So it is adviced to check your logs after each migration for faulty process instances. It is very rare that a migration attempt fails, but when it does, you may want to correct it manually.

## Can I contribute?

Of course! Add an issue, submit a pull request. We will be happy to extend the tool with your help.