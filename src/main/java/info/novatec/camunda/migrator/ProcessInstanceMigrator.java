package info.novatec.camunda.migrator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import info.novatec.camunda.migrator.instances.GetOlderProcessInstancesDefaultImplementation;
import info.novatec.camunda.migrator.instances.GetOlderProcessInstances;
import info.novatec.camunda.migrator.instances.VersionedProcessInstance;
import info.novatec.camunda.migrator.instructions.GetMigrationInstructions;
import info.novatec.camunda.migrator.instructions.MigrationInstructionCombiner;
import info.novatec.camunda.migrator.instructions.MigrationInstructions;
import info.novatec.camunda.migrator.instructions.MigrationInstructionsAdder;
import info.novatec.camunda.migrator.instructions.MinorMigrationInstructions;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplan;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplanDefaultImplementation;
import info.novatec.camunda.migrator.plan.VersionedDefinitionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This migrator will, when called, attempt to migrate all existing process instances that come from a process
 * definition with an older version tag. To enable this, all process models need to be properly versioned:
 * <ul>
 * <li> Increase patch version for simple changes which can be migrated by mapping equal task IDs. Migration of those changes should work out of the box.
 * <li> Increase minor version for changes that need a mapping of some kind for migration to work. Provide these mappings via a {@link MigrationInstructions}-Bean.
 * <li> Increase major version for changes where no migration is possible or wanted.
 * </ul>
 */
@RequiredArgsConstructor
@Slf4j
public class ProcessInstanceMigrator {

    private final ProcessEngine processEngine;
    private final GetOlderProcessInstances getOlderProcessInstances;
    private final CreatePatchMigrationplan createPatchMigrationplan;
    
    private GetMigrationInstructions getMigrationInstructions;
    
    public ProcessInstanceMigrator(ProcessEngine processEngine) {
    	this.processEngine = processEngine;
    	this.getOlderProcessInstances = new GetOlderProcessInstancesDefaultImplementation(processEngine);
    	this.createPatchMigrationplan = new CreatePatchMigrationplanDefaultImplementation(processEngine);
    	this.getMigrationInstructions = MigrationInstructions.builder().build();
    }
    
    public void setMigrationInstructions(GetMigrationInstructions getMigrationInstructions) {
    	this.getMigrationInstructions = getMigrationInstructions;
    }

    public void migrateInstancesOfAllProcesses() {
        processEngine.getRepositoryService().createProcessDefinitionQuery()
            .active()
            .latestVersion()
            .list()
            .forEach(processDefinition -> migrateProcessInstances(processDefinition.getKey()));
    }
    
    //TODO: LOGGER extrahieren

    //TODO: make private
    protected void migrateProcessInstances(String processDefinitionKey) {
        log.info("Starting migration for instances with process definition key {}", processDefinitionKey);
        log.info("Process instances BEFORE migration with process definition key {}", processDefinitionKey);
        logExistingProcessInstanceInfos(processDefinitionKey);

        Optional<VersionedDefinitionId> newestProcessDefinition = getNewestDeployedVersion(processDefinitionKey);
        if (!newestProcessDefinition.isPresent()) {
            log.info("No process definition with key {} deployed. No instances will be migrated", processDefinitionKey);
        } else if (!newestProcessDefinition.get().getProcessVersion().isPresent()) {
        	log.info("No process definitions with a Version Tag deployed deployed. No instances will be migrated");
    	} else {
            ProcessVersion newestProcessVersion = newestProcessDefinition.get().getProcessVersion().get();
            log.info("Newest version for process definition key {} is {}. Attempting migration.", processDefinitionKey, newestProcessVersion.toVersionTag());

			List<VersionedProcessInstance> olderProcessInstances = getOlderProcessInstances
					.getOlderProcessInstances(processDefinitionKey, newestProcessVersion);

            for (VersionedProcessInstance processInstance : olderProcessInstances) {
                MigrationPlan migrationPlan = null;
                if (processInstance.getProcessVersion().isOlderPatchThan(newestProcessVersion)) {
                    migrationPlan = createPatchMigrationplan.migrationPlanByMappingEqualActivityIDs(newestProcessDefinition.get(), processInstance);
                } else if (processInstance.getProcessVersion().isOlderMinorThan(newestProcessVersion)) {
                	migrationPlan = createPatchMigrationplan.migrationPlanByMappingEqualActivityIDs(newestProcessDefinition.get(), processInstance);

					List<MinorMigrationInstructions> applicableMinorMigrationInstructions = getMigrationInstructions
							.getApplicableMinorMigrationInstructions(processDefinitionKey,
									processInstance.getProcessVersion().getMinorVersion(),
									newestProcessVersion.getMinorVersion(), newestProcessVersion.getMajorVersion());

					List<MigrationInstruction> executableMigrationInstructions = MigrationInstructionCombiner.combineMigrationInstructions(
							applicableMinorMigrationInstructions);

					MigrationInstructionsAdder.addInstructions(migrationPlan, executableMigrationInstructions);
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

                    } catch(Exception  e) {
                        log.warn("The process instance with the id {} and businessKey {} could not be migrated from version {} to version {}.\n"
                            + "Source process definition id: {}\n"
                            + "Target process definition id: {}\n",
                                processInstance.getProcessInstanceId(), processInstance.getBusinessKey(),
                                processInstance.getProcessVersion().toVersionTag(), newestProcessVersion.toVersionTag(),
                                processInstance.getProcessDefinitionId(), newestProcessDefinition.get().getProcessDefinitionId(),
                                e);
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

    private Optional<VersionedDefinitionId> getNewestDeployedVersion(String processDefinitionKey) {
        ProcessDefinition latestProcessDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .active()
                .singleResult();

        return Optional.ofNullable(latestProcessDefinition).map(processDefinition ->
                    new VersionedDefinitionId(ProcessVersion.fromString(processDefinition.getVersionTag()), processDefinition.getId()));
    }
	
}
