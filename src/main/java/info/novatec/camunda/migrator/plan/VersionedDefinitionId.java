package info.novatec.camunda.migrator.plan;

import java.util.Optional;

import info.novatec.camunda.migrator.ProcessVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VersionedDefinitionId {
    private final Optional<ProcessVersion> processVersion;
    private final String processDefinitionId;
}
