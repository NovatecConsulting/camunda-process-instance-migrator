package info.novatec.camunda.migrator.migration;

public interface PerformMigration {

	/**
	 * Performs a migration of a process instance according to a given migration
	 * plan.
	 * 
	 * @param plan              the plan for the process instance migration
	 * @param processInstanceId the ID of the process instance to be migrated.
	 */
	void forPlanAndProcessInstanceId(CustomMigrationPlan plan, String processInstanceId);
}
