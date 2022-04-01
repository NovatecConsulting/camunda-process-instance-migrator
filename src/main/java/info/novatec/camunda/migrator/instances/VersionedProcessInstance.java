package info.novatec.camunda.migrator.instances;

import info.novatec.camunda.migrator.ProcessVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VersionedProcessInstance {
    private final String processInstanceId;
    private final String businessKey;
    private final ProcessVersion processVersion;
    private final String processDefinitionId;
}
