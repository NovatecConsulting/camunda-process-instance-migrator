package info.novatec.camunda.migrator.instances;

import java.util.List;

import info.novatec.camunda.migrator.ProcessVersion;

public interface GetOlderProcessInstances {

	public List<VersionedProcessInstance> getOlderProcessInstances(String processDefinitionKey, ProcessVersion newestVersion);
}
