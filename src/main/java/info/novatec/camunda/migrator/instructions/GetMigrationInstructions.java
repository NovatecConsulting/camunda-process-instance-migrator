package info.novatec.camunda.migrator.instructions;

import java.util.List;

public interface GetMigrationInstructions {

	public List<MinorMigrationInstructions> getApplicableMinorMigrationInstructions(String processDefinitionKey,
			int sourceMinorVersion, int targetMinorVersion, int majorVersion);
}
