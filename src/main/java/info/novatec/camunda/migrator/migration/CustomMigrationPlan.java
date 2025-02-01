package info.novatec.camunda.migrator.migration;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomMigrationPlan {

	private String sourceProcessDefinitionId;
	private String targetProcessDefinitionId;
	private List<CustomMigrationInstruction> instructions;
}
