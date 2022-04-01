package info.novatec.camunda.migrator.instructions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigrationInstructionCombiner {

	public static List<MigrationInstruction> combineMigrationInstructions(
			List<MinorMigrationInstructions> applicableMinorMigrationInstructions) {
		List<MigrationInstruction> instructionList = new ArrayList<>();
		applicableMinorMigrationInstructions.stream()
			.sorted(Comparator.comparingInt(MinorMigrationInstructions::getSourceMinorVersion))
			// check every applicable minor-migration
			.forEach(minorMigrationInstructions ->
			    minorMigrationInstructions.getMigrationInstructions().stream()
					// go through all instructions for every migration
					.forEach(migrationInstruction -> {
                        // check if a migration instruction exists, that has that migrationInstructions
                        // source as a target, i.e. instruction 1 goes from activity1 to activity2 and
                        // instruction 2 goes from activity2 to activity3
						boolean migrationInstructionWasAlreadySet = false;
						MigrationInstruction instructionToReplace = null;
						for (MigrationInstruction alreadySetInstruction : instructionList) {
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
							instructionList.add(new MigrationInstructionImpl(
									instructionToReplace.getSourceActivityId(),
									migrationInstruction.getTargetActivityId(), true));
						}
						// if the instruction does not need to be combined, just add it to the list
						else {
							instructionList.add(new MigrationInstructionImpl(
									migrationInstruction.getSourceActivityId(),
									migrationInstruction.getTargetActivityId(), true));
						}
					}));
		return instructionList;
	}
}
