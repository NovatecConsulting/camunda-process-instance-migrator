package info.novatec.camunda.migrator.plan;

import org.camunda.bpm.engine.migration.MigrationPlan;

import info.novatec.camunda.migrator.instances.VersionedProcessInstance;

public interface CreatePatchMigrationplan {
	
	public MigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition, VersionedProcessInstance processInstance);
}
