package info.novatec.camunda.migrator.plan;

import info.novatec.camunda.migrator.instances.VersionedProcessInstance;
import info.novatec.camunda.migrator.migration.CustomMigrationPlan;

public interface CreatePatchMigrationplan {

	/**
	 * Creates a basic migration plan for patch migration.
	 *
	 * @param newestProcessDefinition the {@link VersionedDefinitionId} containing
	 *                                information about the newest deployed process
	 *                                definition.
	 * @param processInstance         the process instance for which the migration
	 *                                plan is to be generated.
	 * @return a {@link CustomMigrationPlan} for migration the process instance to
	 *         the newest version.
	 */
	public CustomMigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition,
			VersionedProcessInstance processInstance);
}
