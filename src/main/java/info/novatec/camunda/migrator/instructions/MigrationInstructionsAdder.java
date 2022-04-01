package info.novatec.camunda.migrator.instructions;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrationInstructionsAdder {

	public static void addInstructions(MigrationPlan migrationPlan,
			List<MigrationInstruction> executableMigrationInstructions) {
		List<MigrationInstruction> migrationPlanList = migrationPlan.getInstructions();
		List<MigrationInstruction> instructionsToBeAddedInTheEnd = new ArrayList<>();
		//first overwrite default instructions with specified instructions
		for(MigrationInstruction instruction : migrationPlanList) {
			boolean specifiedMigrationWasAdded = false;
			for (MigrationInstruction specifiedInstruction : executableMigrationInstructions) {
				if (instruction.getSourceActivityId().equals(specifiedInstruction.getSourceActivityId())) {
					instructionsToBeAddedInTheEnd.add(specifiedInstruction);
					specifiedMigrationWasAdded = true;
				}
			}
			if (!specifiedMigrationWasAdded) {
				instructionsToBeAddedInTheEnd.add(instruction);
			}
		}
		//then add all instructions for activities that are not handled in the default plan
		for (MigrationInstruction specifiedInstruction : executableMigrationInstructions) {
			boolean specifiedInstructionSourceWasHandledInDefaultPlan = false;
			for(MigrationInstruction instruction : migrationPlanList) {
				if (instruction.getSourceActivityId().equals(specifiedInstruction.getSourceActivityId())) {
					specifiedInstructionSourceWasHandledInDefaultPlan = true;
				}
			}
			if (!specifiedInstructionSourceWasHandledInDefaultPlan && !instructionsToBeAddedInTheEnd.contains(specifiedInstruction)) {
				instructionsToBeAddedInTheEnd.add(specifiedInstruction);
			}
		}
		migrationPlanList.clear();
		migrationPlanList.addAll(instructionsToBeAddedInTheEnd);
	}
}
