package info.novatec.bpm.camunda.migrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * Class containing all minor migration instructions for all process
 * definitions. A Bean of this class needs to be provided for the
 * {@link ProcessInstanceMigrator}, even if no instructions are specified.
 */
@Getter
public class MigrationInstructions //für alle Prozessdefinitionen: für alle Minor-Migrationen: alle Instruktionen 
{
	    
	private Map<String, List<MinorMigrationInstructions>> migrationInstructionMap;
	
	private MigrationInstructions(Map<String, List<MinorMigrationInstructions>> migrationInstructionMap) {
		this.migrationInstructionMap = migrationInstructionMap;
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
}
