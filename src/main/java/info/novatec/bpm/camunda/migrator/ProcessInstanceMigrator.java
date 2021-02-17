package info.novatec.bpm.camunda.migrator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This migrator will, upon deployment, attempt to migrate all existing process instances that come from a process definition with an older version tag.
 *
 * To enable this, all process models need to be properly versioned:
 * - Increase patch version for simple changes which can be migrated by mapping equal task ids. Migration of those changes should be automatical.
 * - Increase minor version for changes that need a mapping of some kind for migration to work. Include this mapping here to make it work.
 * - Increase major version for changes where no migration is possible or wanted.
 */
@RequiredArgsConstructor
@Slf4j
public class ProcessInstanceMigrator {

    private final ProcessEngine processEngine;

    private static final ProcessVersion OLDEST_RELEASED_VERSION = ProcessVersion.fromString("1.0.0");

    public void migrateInstancesOfAllProcesses() {
        processEngine.getRepositoryService().createProcessDefinitionQuery()
            .active()
            .list()
            .forEach(processDefinition -> migrateProcessInstances(processDefinition.getKey()));
    }

    protected void migrateProcessInstances(String processDefinitionKey) {
        log.info("Starting migration for instances with process definition key {}", processDefinitionKey);
        log.info("Process instances BEFORE migration with process definition key {}", processDefinitionKey);
        logExistingProcessInstanceInfos(processDefinitionKey);

        Optional<VersionedDefinitionId> newestProcessDefinition = getNewestDeployedVersion(processDefinitionKey);
        if (!newestProcessDefinition.isPresent()) {
            log.info("No process definition with key {} deployed. No instances will be migrated", processDefinitionKey);
        } else {
            ProcessVersion newestProcessVersion = newestProcessDefinition.get().getProcessVersion();
            log.info("Newest version for process definition key {} is {}. Attempting migration.", processDefinitionKey, newestProcessVersion.toVersionTag());

            List<VersionedProcessInstance> olderProcessInstances = getOlderProcessInstances(processDefinitionKey, newestProcessVersion);

            for (VersionedProcessInstance processInstance : olderProcessInstances) {
                MigrationPlan migrationPlan = null;
                if (processInstance.getProcessVersion().isOlderPatchThan(newestProcessVersion)) {
                    migrationPlan = migrationPlanByMappingEqualActivityIDs(newestProcessDefinition.get(), processInstance);
                }
                if (migrationPlan != null) {
                    try {
                        processEngine.getRuntimeService()
                            .newMigration(migrationPlan)
                            .processInstanceIds(processInstance.getProcessInstanceId())
                            .execute();
                        log.info("Successfully migrated process instance with id {} and businessKey {} from version {} to version {}",
                                processInstance.getProcessInstanceId(), processInstance.getBusinessKey(),
                                processInstance.getProcessVersion().toVersionTag(), newestProcessVersion.toVersionTag());

                    } catch(ProcessEngineException  e) {
                        log.warn("The process instance with the id {} and businessKey {} could not be migrated.",
                                processInstance.getProcessInstanceId(), processInstance.getBusinessKey());
                    }
                } else {
                    log.warn("No Migration plan could be generated to migrate the process instance with the id {} and businessKey {} from version {} to version {}",
                            processInstance.getProcessInstanceId(), processInstance.getBusinessKey(),
                            processInstance.getProcessVersion().toVersionTag(), newestProcessVersion.toVersionTag());
                }
            }

        }
        log.info("Process instances AFTER migration with process definition key {}", processDefinitionKey);
        logExistingProcessInstanceInfos(processDefinitionKey);
    }

    private MigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition, VersionedProcessInstance processInstance) {
        return processEngine.getRuntimeService()
                .createMigrationPlan(processInstance.getProcessDefinitionId(), newestProcessDefinition.getProcessDefinitionId())
                .mapEqualActivities()
                .updateEventTriggers()
                .build();
    }

    private void logExistingProcessInstanceInfos(String processDefinitionKey) {
        processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .orderByBusinessKey()
                .asc()
                .list()
                .stream()
                .collect(Collectors.groupingBy(ProcessInstance::getProcessDefinitionId))
                .forEach((processDefinitionId, instances) -> {
                    ProcessDefinition processDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
                    String businessKeys = instances.stream().map(instance -> instance.getBusinessKey()).collect(Collectors.joining(","));
                    log.info("processDefinitionId: {}, versionTag: {}, count {}, businessKeys: {}", processDefinitionId, processDefinition.getVersionTag(), instances.size(), businessKeys);
        });
    }

    private List<VersionedProcessInstance> getOlderProcessInstances(String processDefinitionKey, ProcessVersion newestVersion){
        return processEngine.getRepositoryService().createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .orderByProcessDefinitionVersion()
            .asc()
            .list()
            .stream()
            .filter(processDefinition -> processDefinition.getVersionTag() != null)
            .filter(processDefinition -> !ProcessVersion.fromString(processDefinition.getVersionTag()).isOlderVersionThan(OLDEST_RELEASED_VERSION))
            .filter(processDefinition -> ProcessVersion.fromString(processDefinition.getVersionTag()).isOlderVersionThan(newestVersion))
            .flatMap(processDefinition -> processEngine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionId(processDefinition.getId())
                .orderByBusinessKey()
                .asc()
                .list()
                .stream()
                .map(processInstance -> new VersionedProcessInstance(
                        processInstance.getId(),
                        processInstance.getBusinessKey(),
                        ProcessVersion.fromString(processDefinition.getVersionTag()),
                        processDefinition.getId()
                ))
            ).collect(Collectors.toList());
    }

    private Optional<VersionedDefinitionId> getNewestDeployedVersion(String processDefinitionKey) {
        ProcessDefinition latestProcessDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .active()
                .singleResult();

        return Optional.ofNullable(latestProcessDefinition).map(processDefinition ->
                    new VersionedDefinitionId(ProcessVersion.fromString(processDefinition.getVersionTag()), processDefinition.getId()));
    }

    @Getter
    @RequiredArgsConstructor
    private static class VersionedDefinitionId {
        private final ProcessVersion processVersion;
        private final String processDefinitionId;
    }

    @Getter
    @RequiredArgsConstructor
    private static class VersionedProcessInstance {
        private final String processInstanceId;
        private final String businessKey;
        private final ProcessVersion processVersion;
        private final String processDefinitionId;
    }
}
