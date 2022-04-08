package info.novatec.camunda.migrator.plan;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.migration.MigrationPlan;

import info.novatec.camunda.migrator.instances.VersionedProcessInstance;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePatchMigrationplanDefaultImpl implements CreatePatchMigrationplan {

	private final ProcessEngine processEngine;

	@Override
    public MigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition, VersionedProcessInstance processInstance) {
        return processEngine.getRuntimeService()
                .createMigrationPlan(processInstance.getProcessDefinitionId(), newestProcessDefinition.getProcessDefinitionId())
                .mapEqualActivities()
                .updateEventTriggers()
                .build();
    }
}
