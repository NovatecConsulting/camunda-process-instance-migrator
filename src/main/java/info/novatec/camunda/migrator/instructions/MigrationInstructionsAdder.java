package info.novatec.camunda.migrator.instructions;

import java.util.ArrayList;
import java.util.List;

import info.novatec.camunda.migrator.migration.CustomMigrationInstruction;
import info.novatec.camunda.migrator.migration.CustomMigrationPlan;

public class MigrationInstructionsAdder {

	/**
	 * Modifies a given instance of {@link CustomMigrationPlan} by adding a list of
	 * {@link CustomMigrationInstruction}. Instructions that clash with the original
	 * plan will displace the originals, others will just be added.
	 *
	 * @param migrationPlan                   the plan to be modified
	 * @param executableMigrationInstructions the migration instructions to be added
	 *                                        to the original plan
	 */
	public static void addInstructions(CustomMigrationPlan migrationPlan,
			List<CustomMigrationInstruction> executableMigrationInstructions) {
		List<CustomMigrationInstruction> migrationPlanList = migrationPlan.getInstructions();
		List<CustomMigrationInstruction> instructionsToBeAddedInTheEnd = new ArrayList<>();
		// first overwrite default instructions with specified instructions
		for (CustomMigrationInstruction instruction : migrationPlanList) {
			boolean specifiedMigrationWasAdded = false;
			for (CustomMigrationInstruction specifiedInstruction : executableMigrationInstructions) {
				if (instruction.getSourceActivityId().equals(specifiedInstruction.getSourceActivityId())) {
					instructionsToBeAddedInTheEnd.add(specifiedInstruction);
					specifiedMigrationWasAdded = true;
				}
			}
			if (!specifiedMigrationWasAdded) {
				instructionsToBeAddedInTheEnd.add(instruction);
			}
		}
		// then add all instructions for activities that are not handled in the default
		// plan
		for (CustomMigrationInstruction specifiedInstruction : executableMigrationInstructions) {
			boolean specifiedInstructionSourceWasHandledInDefaultPlan = false;
			for (CustomMigrationInstruction instruction : migrationPlanList) {
				if (instruction.getSourceActivityId().equals(specifiedInstruction.getSourceActivityId())) {
					specifiedInstructionSourceWasHandledInDefaultPlan = true;
				}
			}
			if (!specifiedInstructionSourceWasHandledInDefaultPlan
					&& !instructionsToBeAddedInTheEnd.contains(specifiedInstruction)) {
				instructionsToBeAddedInTheEnd.add(specifiedInstruction);
			}
		}
		migrationPlanList.clear();
		migrationPlanList.addAll(instructionsToBeAddedInTheEnd);
	}
}
