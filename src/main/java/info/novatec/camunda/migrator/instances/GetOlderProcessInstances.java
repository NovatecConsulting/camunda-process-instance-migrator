package info.novatec.camunda.migrator.instances;

import java.util.List;

import info.novatec.camunda.migrator.ProcessVersion;

public interface GetOlderProcessInstances {

    /**
     * Retrieves all process instances of a given processDefinitionKey that are considered 'older' than the specified
     * newest version so they can be subject of migration.
     *
     * @param processDefinitionKey
     *            the process definition key of the process for which process instances are to be looked up.
     * @param newestVersion
     *            the version of the newest deployed process model.
     * @return a list of {@link VersionedProcessInstance} that are considered to run on an 'older' process definition
     *         than the specified newest one.
     */
	public List<VersionedProcessInstance> getOlderProcessInstances(String processDefinitionKey, ProcessVersion newestVersion);
}
