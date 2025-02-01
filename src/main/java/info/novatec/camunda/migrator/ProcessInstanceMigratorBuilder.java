package info.novatec.camunda.migrator;

import org.camunda.bpm.engine.ProcessEngine;

import info.novatec.camunda.migrator.instances.GetOlderProcessInstances;
import info.novatec.camunda.migrator.instances.GetOlderProcessInstancesDefaultImpl;
import info.novatec.camunda.migrator.instructions.GetMigrationInstructions;
import info.novatec.camunda.migrator.instructions.MigrationInstructionsMap;
import info.novatec.camunda.migrator.logging.MigratorLogger;
import info.novatec.camunda.migrator.logging.MigratorLoggerDefaultImpl;
import info.novatec.camunda.migrator.migration.PerformMigration;
import info.novatec.camunda.migrator.migration.PerformMigrationDefaultImpl;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplan;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplanDefaultImpl;
import lombok.NoArgsConstructor;

/**
 * Builder for an instance of ProcessInstanceMigrator. Requires at least one call of
 * {@link #ofProcessEngine(ProcessEngine processEngine) ofProcessEngine}.
 * Will create a set of basic configuration object if no further configuration is specified.
 */
@NoArgsConstructor
public class ProcessInstanceMigratorBuilder {

    private ProcessEngine processEngineToSet;
    private GetOlderProcessInstances getOlderProcessInstancesToSet;
    private CreatePatchMigrationplan createPatchMigrationplanToSet;
    private MigratorLogger migratorLoggerToSet;
    private GetMigrationInstructions getMigrationInstructionsToSet;
    private PerformMigration performMigration;

    public ProcessInstanceMigratorBuilder ofProcessEngine(ProcessEngine processEngine) {
        processEngineToSet = processEngine;
        if (getOlderProcessInstancesToSet == null) {
            this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesDefaultImpl(processEngine);
        }
        if (createPatchMigrationplanToSet == null) {
            this.createPatchMigrationplanToSet = new CreatePatchMigrationplanDefaultImpl(processEngine);
        }
        if (migratorLoggerToSet == null) {
            this.migratorLoggerToSet = new MigratorLoggerDefaultImpl();
        }
        if (getMigrationInstructionsToSet == null) {
            this.getMigrationInstructionsToSet = new MigrationInstructionsMap();
        }
        if (performMigration == null) {
        	this.performMigration = new PerformMigrationDefaultImpl(processEngine);
        }
        return this;
    }

    public ProcessInstanceMigratorBuilder withGetOlderProcessInstances(
        GetOlderProcessInstances getOlderProcessInstances) {
        this.getOlderProcessInstancesToSet = getOlderProcessInstances;
        return this;
    }

    public ProcessInstanceMigratorBuilder withCreatePatchMigrationplanToSet(
        CreatePatchMigrationplan createPatchMigrationplan) {
        this.createPatchMigrationplanToSet = createPatchMigrationplan;
        return this;
    }

    public ProcessInstanceMigratorBuilder withMigratorLogger(MigratorLogger migratorLogger) {
        this.migratorLoggerToSet = migratorLogger;
        return this;
    }

    public ProcessInstanceMigratorBuilder withGetMigrationInstructions(
        GetMigrationInstructions getMigrationInstructions) {
        this.getMigrationInstructionsToSet = getMigrationInstructions;
        return this;
    }

    public ProcessInstanceMigrator build() {
        if (processEngineToSet == null) {
            throw new ProcessInstanceMigratorConfigurationException();
        }
        return new ProcessInstanceMigrator(processEngineToSet, getOlderProcessInstancesToSet,
            createPatchMigrationplanToSet,
            migratorLoggerToSet, getMigrationInstructionsToSet, performMigration);
    }
}
