package info.novatec.camunda.migrator.logging;

public interface MigratorLogger {

    /**
     * Creates a log message indicating that migration is starting.
     *
     * @param processDefinitionKey
     *            the process definition key for which migration is starting.
     */
    public void logMigrationStart(String processDefinitionKey);

    /**
     * Creates a log message indicating that what follows will be a list of process instances that will be migrated.
     *
     * @param processDefinitionKey
     *            the process definition key for which migration is about to occur.
     */
    public void logMessageForInstancesBeforeMigration(String processDefinitionKey);

    /**
     * Creates a log message indicating that what follows will be a list of process instances that have been migrated.
     *
     * @param processDefinitionKey
     *            the process definition key for which migration occurred.
     */
    public void logMessageForInstancesAfterMigration(String processDefinitionKey);

    /**
     * Creates a log message containing the information of process instances that are subject of migration.
     *
     * @param processDefinitionId
     *            the process definition id of the process instances
     * @param versionTag
     *            the version tag of the process instances
     * @param numberOfInstances
     *            the number of process instances
     * @param businessKeys
     *            a string containing the combined business keys of the process instances
     */
    public void logProcessInstancesInfo(String processDefinitionId, String versionTag, int numberOfInstances,
        String businessKeys);

    /**
     * Creates a log message indicating that no process instances with a given key were deployed.
     *
     * @param processDefinitionKey
     *            the process definition key for which no process instances were deployed.
     */
    public void logNoProcessInstancesDeployedWithKey(String processDefinitionKey);

    /**
     * Creates a log message indicating that the newest process version of a given process definition key does not have
     * a version tag.
     *
     * @param processDefinitionKey
     *            the process definition key for which the newest version has no version tag.
     */
    public void logNewestDefinitionDoesNotHaveVersionTag(String processDefinitionKey);

    /**
     * Creates a log message containing the information of the newest version for a given process definition.
     *
     * @param processDefinitionKey
     *            the process definition key of the newest process definition
     * @param versionTag
     *            the version tag of the newest process definition
     */
    public void logNewestVersionInfo(String processDefinitionKey, String versionTag);

    /**
     * Creates a log message indicating that migration was successful for a process instance.
     *
     * @param processInstanceId
     *            the ID of the migrated process instance.
     * @param businessKey
     *            the business key of the migrated process instances.
     * @param versionTagOld
     *            the version tag of the process instance before migration.
     * @param versionTagNew
     *            that version tag of the process instance after migration.
     */
    public void logMigrationSuccessful(String processInstanceId, String businessKey, String versionTagOld,
        String versionTagNew);

    /**
     * Creates a log message indicating that migration failed for a process instance.
     *
     * @param processInstanceId
     *            the ID of the process instance for which migration failed.
     * @param businessKey
     *            the business key of the process instance for which migration failed.
     * @param versionTagOld
     *            the version tag of the process instance before migration.
     * @param versionTagNew
     *            the version tag that the process instance should have been migrated to.
     * @param processDefinitionIdOld
     *            the process definition ID of the process instance before migration.
     * @param processDefinitionIdNew
     *            the process definition ID that the process instance should have been migrated to.
     * @param e
     *            the exception that occured during migration.
     */
    public void logMigrationError(String processInstanceId, String businessKey, String versionTagOld,
        String versionTagNew, String processDefinitionIdOld, String processDefinitionIdNew, Exception e);

    /**
     * Creates a log message indicating that creating a migration plan has failed.
     *
     * @param processInstanceId
     *            the ID of the process instance for which no plan could be generated.
     * @param businessKey
     *            the business key of the process instance for which no plan could be generated.
     * @param versionTagOld
     *            the version tag of the process instance before migration.
     * @param versionTagNew
     *            the version tag that the process instance should have been migrated to.
     */
    public void logMigrationPlanGenerationError(String processInstanceId, String businessKey, String versionTagOld,
        String versionTagNew);

}
