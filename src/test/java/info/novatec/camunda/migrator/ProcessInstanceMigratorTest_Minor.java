package info.novatec.camunda.migrator;

import static info.novatec.camunda.migrator.TestHelper.*;
import static info.novatec.camunda.migrator.assertions.ProcessInstanceListAsserter.assertThat;
import static info.novatec.camunda.migrator.assertions.TaskListAsserter.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ProcessInstanceMigratorTest_Minor {

    @ClassRule
    public static ProcessEngineRule rule = new ProcessEngineRule();

    protected static final String MIGRATEABLE_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
    private static final String MINOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
    private static final String MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_5_1.bpmn";
    private static final String MINOR_INCREASED_WITH_THIRD_TASK_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_7_0.bpmn";

    private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

    private ProcessInstanceMigrator processInstanceMigrator = new ProcessInstanceMigrator(processEngine());

    private ProcessDefinition initialProcessDefinition;
    private ProcessDefinition newestProcessDefinitionAfterRedeployment;
    private ProcessInstance processInstance1;
    private ProcessInstance processInstance2;

    @Before
    public void setUp() {
        deployBPMNFromClasspathResource(MIGRATEABLE_PROCESS_MODEL_PATH, repositoryService());
        // this will refer to the initial process Model
        initialProcessDefinition = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(initialProcessDefinition.getVersionTag()).isEqualTo("1.0.0");

        processInstance1 = startProcessInstance(PROCESS_DEFINITION_KEY, runtimeService());
        processInstance2 = startProcessInstance(PROCESS_DEFINITION_KEY, runtimeService());
    }

    @After
    public void cleanUp() {
        runtimeService().deleteProcessInstance(processInstance1.getId(), "noReason");
        runtimeService().deleteProcessInstance(processInstance2.getId(), "noReason");

        repositoryService()
            .createDeploymentQuery()
            .list()
            .forEach(
                deployment -> repositoryService().deleteDeployment(deployment.getId()));
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_to_higher_minor_version_if_no_migration_plan_was_provided() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateInstancesOfAllProcesses();

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_migrate_to_higher_minor_version_if_migration_plan_was_provided() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveKey("UserTask1")
            .allTasksHaveFormkey(null);

        processInstanceMigrator.setMigrationInstructions(MigrationInstructions.builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150())
						.build());
        processInstanceMigrator.migrateInstancesOfAllProcesses();

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveKey("UserTask2")
            .allTasksHaveFormkey("Formkey2");
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_if_migration_to_higher_minor_version_has_faulty_migration_instructions() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveKey("UserTask1")
            .allTasksHaveFormkey(null);

        processInstanceMigrator.setMigrationInstructions(MigrationInstructions.builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateFaultyMigrationInstructionsFor100To150())
						.build());
        processInstanceMigrator.migrateInstancesOfAllProcesses();

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
	        .allTasksHaveKey("UserTask1")
	        .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_migrate_to_higher_minor_by_adding_up_migration_instructions() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveKey("UserTask1")
            .allTasksHaveFormkey(null);

        processInstanceMigrator.setMigrationInstructions(MigrationInstructions.builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionFor100To130())
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionFor130To150())
						.build());
        processInstanceMigrator.migrateInstancesOfAllProcesses();

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveKey("UserTask2")
            .allTasksHaveFormkey("Formkey2");
    }

    @Test
    public void processInstanceMigrator_should_migrate_to_higher_minor_and_patch_version_using_only_minor_migration_plan() {
    	deployBPMNFromClasspathResource(MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH, repositoryService());
    	newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        complete(task(processInstance1));

	    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
	        .oneTaskHasKey("UserTask1")
	        .oneTaskHasKey("UserTask2")
	        .allTasksHaveFormkey(null);

	    processInstanceMigrator.setMigrationInstructions(MigrationInstructions.builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150())
						.build());
	    processInstanceMigrator.migrateInstancesOfAllProcesses();

	    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

	    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
	        .allTasksHaveKey("UserTask2")
	        .allTasksHaveFormkey("Formkey2");
    }

    @Test
    public void processInstanceMigrator_should_migrate_to_mapped_id_even_if_same_id_still_exists_in_target() {
    	deployBPMNFromClasspathResource(MINOR_INCREASED_WITH_THIRD_TASK_PROCESS_MODEL_PATH, repositoryService());
    	newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.7.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

	    complete(task(processInstance1));

	    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
	        .oneTaskHasKey("UserTask1")
	        .oneTaskHasKey("UserTask2")
	        .allTasksHaveFormkey(null);

	    processInstanceMigrator.setMigrationInstructions(MigrationInstructions.builder()
						.putInstructions(PROCESS_DEFINITION_KEY, Arrays.asList(
								MinorMigrationInstructions.builder()
					        		.sourceMinorVersion(0)
					        		.targetMinorVersion(7)
					        		.migrationInstructions(Arrays.asList(
					        				new MigrationInstructionImpl("UserTask1", "UserTask7"),
					        				new MigrationInstructionImpl("UserTask2", "UserTask7")))
					        		.majorVersion(1)
					        		.build()))
						.build());

	    processInstanceMigrator.migrateInstancesOfAllProcesses();

	    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

	    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
	        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
	        .allTasksHaveKey("UserTask7")
	        .numberOfTasksIs(2);
    }

    private List<MinorMigrationInstructions> generateMigrationInstructionsFor100To150() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(0)
        		.targetMinorVersion(5)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask1", "UserTask2")))
        		.majorVersion(1)
        		.build());
    }

    private List<MinorMigrationInstructions> generateFaultyMigrationInstructionsFor100To150() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(0)
        		.targetMinorVersion(5)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask1", "UserTask6")))
        		.majorVersion(1)
        		.build());
    }

    private List<MinorMigrationInstructions> generateMigrationInstructionFor100To130() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(0)
        		.targetMinorVersion(3)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask1", "UserTask3")))
        		.majorVersion(1)
        		.build());
    }

    private List<MinorMigrationInstructions> generateMigrationInstructionFor130To150() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(3)
        		.targetMinorVersion(5)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask3", "UserTask2")))
        		.majorVersion(1)
        		.build());
    }
}
