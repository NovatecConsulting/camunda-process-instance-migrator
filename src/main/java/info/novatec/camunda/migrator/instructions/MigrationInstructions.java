package info.novatec.camunda.migrator.instructions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.novatec.camunda.migrator.ProcessInstanceMigrator;
import lombok.Getter;

/**
 * Class containing all minor migration instructions for all process
 * definitions. A Bean of this class should be provided for the
 * {@link ProcessInstanceMigrator}, even if no instructions are specified.
 */
@Getter
public class MigrationInstructions implements GetMigrationInstructions {

	private Map<String, List<MinorMigrationInstructions>> migrationInstructionMap;

	private MigrationInstructions(Map<String, List<MinorMigrationInstructions>> migrationInstructionMap) {
		this.migrationInstructionMap = migrationInstructionMap;
	}

	public static Builder builder() {
	    return new Builder();
	}

	public static class Builder {
		private Map<String, List<MinorMigrationInstructions>> migrationInstructionMap;

		public Builder() {
			this.migrationInstructionMap = new HashMap<>();
		}

		public Builder putInstructions(String processDefinitionKey, List<MinorMigrationInstructions> instructions) {
			if (migrationInstructionMap.containsKey(processDefinitionKey)) {
				migrationInstructionMap.get(processDefinitionKey).addAll(instructions);
			} else {
				//generate new ArrayList to guarantee support for structural modification (i.e. add)
				migrationInstructionMap.put(processDefinitionKey, new ArrayList<>(instructions));
			}
			return this;
		}

		public MigrationInstructions build() {
			return new MigrationInstructions(migrationInstructionMap);
		}
	}

	public List<MinorMigrationInstructions> getApplicableMinorMigrationInstructions(String processDefinitionKey,
			int sourceMinorVersion, int targetMinorVersion, int majorVersion) {
		if (migrationInstructionMap.containsKey(processDefinitionKey))
			return migrationInstructionMap.get(processDefinitionKey).stream()
					.filter(minorMigrationInstructions -> minorMigrationInstructions
							.getTargetMinorVersion() <= targetMinorVersion
							&& minorMigrationInstructions.getSourceMinorVersion() >= sourceMinorVersion
							&& minorMigrationInstructions.getMajorVersion() == majorVersion)
					.collect(Collectors.toList());
		else {
			return Collections.emptyList();
		}
	}
}
