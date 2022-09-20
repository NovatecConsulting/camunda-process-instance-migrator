package info.novatec.camunda.migrator.instances;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;

import info.novatec.camunda.migrator.ProcessVersion;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link GetOlderProcessInstances} *
 */
@RequiredArgsConstructor
public class GetOlderProcessInstancesDefaultImpl implements GetOlderProcessInstances {

    private static final ProcessVersion OLDEST_RELEASED_VERSION = ProcessVersion.fromString("1.0.0").get();

	private final ProcessEngine processEngine;

	@Override
    public List<VersionedProcessInstance> getOlderProcessInstances(String processDefinitionKey, ProcessVersion newestVersion){
        return processEngine.getRepositoryService().createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .orderByProcessDefinitionVersion()
            .asc()
            .list()
            .stream()
            .filter(processDefinition -> processDefinition.getVersionTag() != null)
            .filter(processDefinition -> ProcessVersion.fromString(processDefinition.getVersionTag()).isPresent())
            .filter(processDefinition -> !ProcessVersion.fromString(processDefinition.getVersionTag()).get().isOlderVersionThan(OLDEST_RELEASED_VERSION))
            .filter(processDefinition -> ProcessVersion.fromString(processDefinition.getVersionTag()).get().isOlderVersionThan(newestVersion))
            .flatMap(processDefinition -> processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionId(processDefinition.getId())
                .orderByBusinessKey()
                .asc()
                .list()
                .stream()
                .map(processInstance -> new VersionedProcessInstance(
                        processInstance.getId(),
                        processInstance.getBusinessKey(),
                        ProcessVersion.fromString(processDefinition.getVersionTag()).get(),
                        processDefinition.getId()
                ))
            ).collect(Collectors.toList());
    }
}
