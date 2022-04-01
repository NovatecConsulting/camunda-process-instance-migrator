package info.novatec.camunda.migrator.logging;

public interface MigratorLogger {

	public void logMigrationStart(String processDefinitionKey);
	
	public void logMessageForInstancesBeforeMigration(String processDefinitionKey);
	
	public void logMessageForInstancesAfterMigration(String processDefinitionKey);
	
	public void logProcessInstancesInfo(String processDefinitionId, String versionTag, int numberOfInstances, String businessKeys);
	
	public void logNoProcessInstancesDeployedWithKey(String processDefinitionKey);
	
	public void logNoProcessInstancesDeployedWithVersionTag();
	
	public void logNewestVersionInfo(String processDefinitionKey, String versionTag);
	
	public void logMigrationSuccessful(String processInstanceId, String businessKey, String versionTagOld, String versionTagNew);
	
	public void logMigrationError(String processInstanceId, String businessKey, String versionTagOld,
			String versionTagNew, String processDefinitionIdOld, String processDefinitionIdNew, Exception e);
	
	public void logMigrationPlanGenerationError(String processInstanceId, String businessKey, String versionTagOld, String versionTagNew);
	
}
