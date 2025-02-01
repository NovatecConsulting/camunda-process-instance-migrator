package info.novatec.camunda.migrator.migration;

import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.impl.migration.MigrationPlanImpl;
import org.camunda.bpm.engine.migration.MigrationPlan;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PerformMigrationDefaultImpl implements PerformMigration {
	
	private final ProcessEngine processEngine;

	@Override
	public void forPlanAndProcessInstanceId(CustomMigrationPlan plan, String processInstanceId) {
		 processEngine.getRuntimeService()
         	.newMigration(mapToCamunda7MigrationPlan(plan))
         	.processInstanceIds(processInstanceId)
         	.execute();
		
	}

	private MigrationPlan mapToCamunda7MigrationPlan(CustomMigrationPlan plan) {
		MigrationPlanImpl migrationPlan = new MigrationPlanImpl(plan.getSourceProcessDefinitionId(), plan.getTargetProcessDefinitionId());
		migrationPlan.setInstructions(plan.getInstructions().stream()
				.map(instruction -> {
					MigrationInstructionImpl migrationInstructionImpl = new MigrationInstructionImpl(
							instruction.getSourceActivityId(), instruction.getTargetActivityId());
					migrationInstructionImpl.setUpdateEventTrigger(instruction.isUpdateEventTrigger());
					return migrationInstructionImpl;
				})
				.collect(Collectors.toList()));
		return migrationPlan;
	}

}
