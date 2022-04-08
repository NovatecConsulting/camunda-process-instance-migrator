package info.novatec.camunda.migrator;

import org.camunda.bpm.engine.ProcessEngine;

import info.novatec.camunda.migrator.instances.GetOlderProcessInstances;
import info.novatec.camunda.migrator.instances.GetOlderProcessInstancesDefaultImplementation;
import info.novatec.camunda.migrator.instructions.GetMigrationInstructions;
import info.novatec.camunda.migrator.instructions.MigrationInstructionsMap;
import info.novatec.camunda.migrator.logging.MigratorLogger;
import info.novatec.camunda.migrator.logging.MigratorLoggerDefaultImplementation;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplan;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplanDefaultImplementation;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProcessInstanceMigratorBuilder {

    private ProcessEngine processEngineToSet;
    private GetOlderProcessInstances getOlderProcessInstancesToSet;
    private CreatePatchMigrationplan createPatchMigrationplanToSet;
    private MigratorLogger migratorLoggerToSet;
    private GetMigrationInstructions getMigrationInstructionsToSet;

    public ProcessInstanceMigratorBuilder ofProcessEngine(ProcessEngine processEngine) {
        processEngineToSet = processEngine;
        this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesDefaultImplementation(processEngine);
        this.createPatchMigrationplanToSet = new CreatePatchMigrationplanDefaultImplementation(processEngine);
        this.migratorLoggerToSet = new MigratorLoggerDefaultImplementation();
        this.getMigrationInstructionsToSet = new MigrationInstructionsMap();
        return this;
    }

    public ProcessInstanceMigratorBuilder withGetOlderProcessInstances(GetOlderProcessInstances getOlderProcessInstances) {
        this.getOlderProcessInstancesToSet = getOlderProcessInstances;
        return this;
    }

    public ProcessInstanceMigratorBuilder withCreatePatchMigrationplanToSet(CreatePatchMigrationplan createPatchMigrationplan) {
        this.createPatchMigrationplanToSet = createPatchMigrationplan;
        return this;
    }

    public ProcessInstanceMigratorBuilder withMigratorLogger(MigratorLogger migratorLogger) {
        this.migratorLoggerToSet = migratorLogger;
        return this;
    }

    public ProcessInstanceMigratorBuilder withGetMigrationInstructions(GetMigrationInstructions getMigrationInstructions) {
        this.getMigrationInstructionsToSet = getMigrationInstructions;
        return this;
    }

    public ProcessInstanceMigrator build() {
        if (processEngineToSet == null) {
            throw new ProcessInstanceMigratorConfigurationException();
        }
        return new ProcessInstanceMigrator(processEngineToSet, getOlderProcessInstancesToSet, createPatchMigrationplanToSet,
            migratorLoggerToSet, getMigrationInstructionsToSet);
    }
}
