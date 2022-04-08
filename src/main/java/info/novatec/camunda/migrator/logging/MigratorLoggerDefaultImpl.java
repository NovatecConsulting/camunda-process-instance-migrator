package info.novatec.camunda.migrator.logging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigratorLoggerDefaultImpl implements MigratorLogger {

    @Override
    public void logMigrationStart(String processDefinitionKey) {
		log.info("Starting migration for instances with process definition key {}", processDefinitionKey);

	}

	@Override
    public void logMessageForInstancesBeforeMigration(String processDefinitionKey) {
		log.info("Process instances BEFORE migration with process definition key {}", processDefinitionKey);

	}

	@Override
	public void logMessageForInstancesAfterMigration(String processDefinitionKey) {
		log.info("Process instances AFTER migration with process definition key {}", processDefinitionKey);

	}

	@Override
	public void logProcessInstancesInfo(String processDefinitionId, String versionTag, int numberOfInstances,
			String businessKeys) {
		log.info("processDefinitionId: {}, versionTag: {}, count {}, businessKeys: {}", processDefinitionId, versionTag,
				numberOfInstances, businessKeys);

	}

	@Override
    public void logNoProcessInstancesDeployedWithKey(String processDefinitionKey) {
		log.info("No process definition with key {} deployed. No instances will be migrated", processDefinitionKey);

	}

	@Override
    public void logNewestDefinitionDoesNotHaveVersionTag(String processDefinitionKey) {
		log.info("Newest process definition for process with key {} has no version tag. No instances will be migrated");

	}

	@Override
	public void logNewestVersionInfo(String processDefinitionKey, String versionTag) {
		 log.info("Newest version for process definition key {} is {}. Attempting migration.", processDefinitionKey, versionTag);

	}

	@Override
	public void logMigrationSuccessful(String processInstanceId, String businessKey, String versionTagOld,
			String versionTagNew) {
		log.info("Successfully migrated process instance with id {} and businessKey {} from version {} to version {}",
				processInstanceId, businessKey, versionTagOld, versionTagNew);

	}

	@Override
	public void logMigrationError(String processInstanceId, String businessKey, String versionTagOld,
			String versionTagNew, String processDefinitionIdOld, String processDefinitionIdNew, Exception e) {
		log.warn("The process instance with the id {} and businessKey {} could not be migrated from version {} to version {}.\n"
                + "Source process definition id: {}\n"
                + "Target process definition id: {}\n",
				processInstanceId, businessKey, versionTagOld, versionTagNew, processDefinitionIdOld,
				processDefinitionIdNew, e);

	}

	@Override
	public void logMigrationPlanGenerationError(String processInstanceId, String businessKey, String versionTagOld,
			String versionTagNew) {
		log.warn("No Migration plan could be generated to migrate the process instance with the id {} and businessKey {} from version {} to version {}",
				processInstanceId, businessKey, versionTagOld, versionTagNew);

	}

}
