package info.novatec.camunda.migrator.instructions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import info.novatec.camunda.migrator.migration.CustomMigrationInstruction;

public class MigrationInstructionCombiner {

	/**
	 * Combines a list of {@link MinorMigrationInstructions} into a list of
	 * migration-ready {@link CustomMigrationInstruction}, thereby taking into
	 * account possible 'adding' of instructions that span multiple minor versions.
	 *
	 * @param applicableMinorMigrationInstructions the migration instructions that
	 *                                             are to be combined.
	 * @return a list of {@link CustomMigrationInstruction} containing all combined
	 *         instructions from the input.
	 */
	public static List<CustomMigrationInstruction> combineMigrationInstructions(
			List<MinorMigrationInstructions> applicableMinorMigrationInstructions) {
		List<CustomMigrationInstruction> instructionList = new ArrayList<>();
		applicableMinorMigrationInstructions.stream()
				.sorted(Comparator.comparingInt(MinorMigrationInstructions::getSourceMinorVersion))
				// check every applicable minor-migration
				.forEach(minorMigrationInstructions -> minorMigrationInstructions.getMigrationInstructions().stream()
						// go through all instructions for every migration
						.forEach(migrationInstruction -> {
							// check if a migration instruction exists, that has that migrationInstructions
							// source as a target, i.e. instruction 1 goes from activity1 to activity2 and
							// instruction 2 goes from activity2 to activity3
							boolean migrationInstructionWasAlreadySet = false;
							CustomMigrationInstruction instructionToReplace = null;
							for (CustomMigrationInstruction alreadySetInstruction : instructionList) {
								if (alreadySetInstruction.getTargetActivityId() == migrationInstruction
										.getSourceActivityId()) {
									migrationInstructionWasAlreadySet = true;
									instructionToReplace = alreadySetInstruction;
								}
							}
							// if such a migration instruction exists, remove it and replace it with a
							// combined instruction
							if (migrationInstructionWasAlreadySet && instructionToReplace != null) {
								instructionList.remove(instructionToReplace);
								instructionList
										.add(new CustomMigrationInstruction(instructionToReplace.getSourceActivityId(),
												migrationInstruction.getTargetActivityId(), true));
							}
							// if the instruction does not need to be combined, just add it to the list
							else {
								instructionList
										.add(new CustomMigrationInstruction(migrationInstruction.getSourceActivityId(),
												migrationInstruction.getTargetActivityId(), true));
							}
						}));
		return instructionList;
	}
}
