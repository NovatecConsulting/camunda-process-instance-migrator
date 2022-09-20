package info.novatec.camunda.migrator.plan;

import org.camunda.bpm.engine.migration.MigrationPlan;

import info.novatec.camunda.migrator.instances.VersionedProcessInstance;

public interface CreatePatchMigrationplan {

    /**
     * Creates a basic migration plan for patch migration.
     *
     * @param newestProcessDefinition
     *            the {@link VersionedDefinitionId} containing information about the newest deployed process definition.
     * @param processInstance
     *            the process instance for which the migration plan is to be generated.
     * @return a {@link MigrationPlan} for migration the process instance to the newest version.
     */
    public MigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition,
        VersionedProcessInstance processInstance);
}
