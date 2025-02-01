package info.novatec.camunda.migrator.plan;

import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.migration.MigrationPlan;

import info.novatec.camunda.migrator.instances.VersionedProcessInstance;
import info.novatec.camunda.migrator.migration.CustomMigrationInstruction;
import info.novatec.camunda.migrator.migration.CustomMigrationPlan;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePatchMigrationplanDefaultImpl implements CreatePatchMigrationplan {

	private final ProcessEngine processEngine;

	@Override
	public CustomMigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition,
			VersionedProcessInstance processInstance) {
		MigrationPlan migrationPlan = processEngine.getRuntimeService()
				.createMigrationPlan(processInstance.getProcessDefinitionId(),
						newestProcessDefinition.getProcessDefinitionId())
				.mapEqualActivities()
				.updateEventTriggers()
				.build();

		return CustomMigrationPlan.builder().sourceProcessDefinitionId(migrationPlan.getSourceProcessDefinitionId())
				.targetProcessDefinitionId(migrationPlan.getTargetProcessDefinitionId())
				.instructions(migrationPlan.getInstructions().stream()
						.map(instruction -> CustomMigrationInstruction.builder()
								.sourceActivityId(instruction.getSourceActivityId())
								.targetActivityId(instruction.getTargetActivityId())
								.updateEventTrigger(instruction.isUpdateEventTrigger()).build())
						.collect(Collectors.toList()))
				.build();
	}
}
